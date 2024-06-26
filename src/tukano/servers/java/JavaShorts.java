package tukano.servers.java;

import Discovery.Discovery;
import tukano.api.Follow;
import tukano.api.Short;
import tukano.api.LikeShort;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Shorts;

import tukano.clients.factories.BlobsClientFactory;
import tukano.clients.factories.UsersClientFactory;
import tukano.persistence.Hibernate;

import java.net.URI;
import java.util.*;
import java.util.logging.Logger;

public class JavaShorts implements Shorts {
    private static Logger Log = Logger.getLogger(JavaShorts.class.getName());

    URI[] userURI;
    URI[] blobURI;

    public JavaShorts () { }

    private Result<User> getUser(String userId) {
        userURI = Discovery.getInstance().knownUrisOf("users",1);
        List<User> result = UsersClientFactory.getClient(userURI[0]).searchUsers(userId).value();
        if (result.isEmpty())
            return Result.error(Result.ErrorCode.NOT_FOUND);

        return Result.ok(result.get(0));
    }

    @Override
    public Result<Short> createShort(String userId, String password) {
        Log.info("create short: user = " + userId);

        if(userId == null || password == null) {
            Log.info("Name or Password null.");
            return Result.error( Result.ErrorCode.BAD_REQUEST);
        }

        userURI = Discovery.getInstance().knownUrisOf("users",1);
        Result<User> result = UsersClientFactory.getClient(userURI[0]).getUser(userId, password);
        if (!result.isOK())
            return Result.error(result.error());

        blobURI = Discovery.getInstance().knownUrisOf("blobs",1);

        String id = UUID.randomUUID().toString();
        Short newShort = new Short(id, userId, getServer(blobURI) + "/" + id);
        Hibernate.getInstance().persist(newShort);
        return Result.ok(newShort);
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        Log.info("delete short: short = " + shortId + "; pwd = " + password);
        userURI = Discovery.getInstance().knownUrisOf("users",1);

        Result<Short> shortToBeDeleted = getShort(shortId);
        if (!shortToBeDeleted.isOK()) return Result.error(shortToBeDeleted.error());

        Result<User> result = UsersClientFactory.getClient(userURI[0]).getUser(shortToBeDeleted.value().getOwnerId(), password);
        if (!result.isOK()) {
            return Result.error(result.error());
        }

        List<LikeShort> shortLikes = Hibernate.getInstance().sql("SELECT * FROM LikeShort l WHERE l.shortLiked LIKE '"
                + shortId + "'", LikeShort.class);
        shortLikes.forEach(like -> Hibernate.getInstance().delete(like));
        blobURI = Discovery.getInstance().knownUrisOf("blobs",1);

        Result<Void> resultBlob = BlobsClientFactory.getClient(getRightServer(blobURI, shortToBeDeleted.value())).deleteBlob(shortId);

        if (!resultBlob.isOK())
            return Result.error(resultBlob.error());

        Hibernate.getInstance().delete(shortToBeDeleted.value());
        return Result.ok();
    }

    @Override
    public Result<Short> getShort(String shortId) {
        Log.info("getting short: short = " + shortId);

        List<Short> result = Hibernate.getInstance().sql("SELECT * FROM Short s WHERE s.shortId LIKE '" + shortId + "'",
                Short.class);
        if (result.isEmpty())
            return Result.error(Result.ErrorCode.NOT_FOUND);

        return Result.ok(result.get(0));
    }

    @Override
    public Result<List<String>> getShorts(String userId) {
        Result<User> result = getUser(userId);
        if (!result.isOK())
            return Result.error(result.error());

        List<String> shorts =
                Hibernate.getInstance().sql("SELECT s.shortId FROM Short s WHERE s.ownerId LIKE '"
                        + userId + "'", String.class);
        return Result.ok(shorts);
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        Log.info("follow : follower = " + userId1 + "; followed = " + userId2);
        if (userId1 == null || userId2 == null || password == null) {
            Log.info("Arguments invalid.");
            return Result.error( Result.ErrorCode.BAD_REQUEST);
        }

        userURI = Discovery.getInstance().knownUrisOf("users",1);
        Result<User> result1 = UsersClientFactory.getClient(userURI[0]).getUser(userId1, password);
        Result<User> result2 = getUser(userId2);
        if (!result1.isOK())
            return Result.error(result1.error());

        if (!result2.isOK())
            return Result.error(result2.error());

        List<Follow> follow = Hibernate.getInstance().sql("SELECT * FROM Follow f WHERE f.followed = '"
                + userId2 + "' AND f.follower = '" + userId1 + "'", Follow.class);

        if (isFollowing && follow.isEmpty()) {
            Follow newFollow = new Follow(UUID.randomUUID().toString(), userId1, userId2);
            Hibernate.getInstance().persist(newFollow);
        }
        else if (!isFollowing && !follow.isEmpty())
            Hibernate.getInstance().delete(follow.get(0));
        else if (!isFollowing)
            return Result.ok();
        else
            return Result.error(Result.ErrorCode.CONFLICT);

        return Result.ok();
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        Log.info("followers : user = " + userId);

        userURI = Discovery.getInstance().knownUrisOf("users",1);
        Result<User> result = UsersClientFactory.getClient(userURI[0]).getUser(userId, password);
        if (!result.isOK())
            return Result.error(result.error());

        List<String> followers = Hibernate.getInstance().sql("SELECT f.follower FROM Follow f WHERE f.followed = '"
                + userId + "'", String.class);

        return Result.ok(followers);
    }

    @Override
    public Result<Void> deleteFollowers(String userId) {
        Log.info("delete followers : user = " + userId);
        List<Follow> follows = Hibernate.getInstance().sql("SELECT * FROM Follow f WHERE f.followed = '" + userId +
                "' OR f.follower = '" + userId + "'", Follow.class);
        follows.forEach(follow -> Hibernate.getInstance().delete(follow));
        return Result.ok();
    }

    //return all the users that are followed by the userId
    private Result<List<String>> following(String userId, String password) {
        Log.info("following : user = " + userId);

        userURI = Discovery.getInstance().knownUrisOf("users",1);
        Result<User> result = UsersClientFactory.getClient(userURI[0]).getUser(userId, password);
        if (!result.isOK())
            return Result.error(result.error());

        List<String> following = Hibernate.getInstance().sql("SELECT f.followed FROM Follow f WHERE f.follower = '"
                + userId + "'", String.class);

        return Result.ok(following);
    }


    private void changeLikesNr(Short userShort, boolean isLiked) {
        int shortLikes = userShort.getTotalLikes();
        if (isLiked)
            userShort.setTotalLikes(shortLikes+1);
        else
            userShort.setTotalLikes(shortLikes-1);
        Hibernate.getInstance().update(userShort);
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        Log.info("like : short = " + shortId + "; user = " + userId);

        Result<Short> shortResult = getShort(shortId);
        if (!shortResult.isOK()) {
            Log.info("Short does not exist.");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        Short userShort = shortResult.value();

        if (isLiked) {
            LikeShort newLikeShort = new LikeShort(UUID.randomUUID().toString(), shortId, userId);
            Hibernate.getInstance().persist(newLikeShort);
        }
        else {
            List<LikeShort> likeResult =
                    Hibernate.getInstance().sql("SELECT * FROM LikeShort l WHERE l.shortLiked = '" + shortId
                            + "' AND l.user = '" + userId + "'", LikeShort.class);

            if (likeResult.isEmpty()) {
                Log.info("Like does not exist.");
                return Result.error(Result.ErrorCode.NOT_FOUND);
            }
            Hibernate.getInstance().delete(likeResult.get(0));
        }
        changeLikesNr(userShort, isLiked);
        return Result.ok();
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        Log.info("likes : short = " + shortId);

        Result<Short> shortResult = getShort(shortId);
        if (!shortResult.isOK()) {
            Log.info("Short does not exist.");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        userURI = Discovery.getInstance().knownUrisOf("users",1);
        Result<User> ownerResult = UsersClientFactory.getClient(userURI[0]).getUser(shortResult.value().getOwnerId(), password);
        if (!ownerResult.isOK()) {
            Log.info("Password is incorrect.");
            return Result.error( Result.ErrorCode.FORBIDDEN);
        }

        List<String> likes = Hibernate.getInstance().sql("SELECT l.user FROM LikeShort l WHERE l.shortLiked LIKE '"
                + shortId + "'", String.class);

        return Result.ok(likes);
    }

    public Result<Void> deleteLikes(String userId) {
        Log.info("delete likes : user = " + userId);

        List<LikeShort> likes = Hibernate.getInstance().sql("SELECT * FROM LikeShort l WHERE l.user = '"
                + userId + "'", LikeShort.class);
        likes.forEach(userLike -> {
            Hibernate.getInstance().delete(userLike);
            Short shortLiked = getShort(userLike.getShortLiked()).value();
            changeLikesNr(shortLiked, false);
        });

        return Result.ok();
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        Log.info("feed : user = " + userId);

        Result<List<String>> followingResult = following(userId, password);
        if (!followingResult.isOK())
            return Result.error(followingResult.error());

        List<String> feed = getShorts(userId).value();
        List<String> following = followingResult.value();

        if (!following.isEmpty()) {
            feed.addAll(Hibernate.getInstance().sql("SELECT s.shortId FROM Short s JOIN Follow f " +
                    "ON s.ownerId = f.followed WHERE f.follower = '" + userId + "'", String.class));
        }

        feed.sort(Comparator.comparing(shortId -> getShort(shortId.toString()).value().getTimestamp()).reversed());

        return Result.ok(feed);
    }

    public URI getServer(URI[] URIs) {
        Log.info("Getting the best server...");
        return Arrays.stream(URIs).min(Comparator.comparing(this::getBlobSize)).orElse(null);
    }

    private int getBlobSize(URI uri) {
        List<Short> blobShorts = Hibernate.getInstance().sql("SELECT * FROM Short s WHERE s.blobUrl LIKE '"
                + uri.toString() + "%'", Short.class);
        return blobShorts.size();
    }

    private URI getRightServer(URI[] URIs, Short shortEliminated) {
        Log.info("Getting the right server of short " + shortEliminated.getShortId());

        URI rightURI = null;

        if (URIs.length == 0) return null;

        if (shortEliminated != null) {
            for (int i = 0; i < URIs.length; i++) {
                if (shortEliminated.getBlobUrl().contains(URIs[i].toString()))
                    rightURI = URIs[i];
            }
            return rightURI;
        } else {
            Log.info("Short not found.");
            return null;
        }
    }
}