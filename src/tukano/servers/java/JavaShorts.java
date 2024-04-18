package tukano.servers.java;

import Discovery.Discovery;
import tukano.api.Follow;
import tukano.api.Short;
import tukano.api.LikeShort;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Shorts;

import tukano.clients.RestBlobsClient;
import tukano.clients.RestUsersClient;
import tukano.persistence.Hibernate;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class JavaShorts implements Shorts {
    private static Logger Log = Logger.getLogger(JavaShorts.class.getName());

    Hibernate datastore;
    RestUsersClient userClient;
    RestBlobsClient blobClient;
    URI[] userURI;
    URI[] blobURI;

    public JavaShorts () {
        datastore = Hibernate.getInstance();
        userURI = Discovery.getInstance().knownUrisOf("users",1);
        blobURI = Discovery.getInstance().knownUrisOf("blobs",1);
        userClient = new RestUsersClient(userURI[0]);
        blobClient = new RestBlobsClient(blobURI[0]);
    }
    //TODO: buscar o server menos carregado de blobs
    private Result<User> getUser(String userId) {
        List<User> result = userClient.searchUsers(userId).value();
        if (result.isEmpty())
            return Result.error(Result.ErrorCode.NOT_FOUND);

        return Result.ok(result.get(0));
    }

    @Override
    public Result<Short> createShort(String userId, String password) throws MalformedURLException {
        Log.info("create short: user = " + userId);

        if(userId == null || password == null) {
            Log.info("Name or Password null.");
            return Result.error( Result.ErrorCode.BAD_REQUEST);
        }

        Result<User> result = userClient.getUser(userId, password);
        if (!result.isOK())
            return Result.error(result.error());

        String id = UUID.randomUUID().toString();
        Short newShort = new Short(id, userId, blobURI[0].toURL() + "/" + id);
        datastore.persist(newShort);
        return Result.ok(newShort);
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        Log.info("delete short: short = " + shortId + "; pwd = " + password);

        Short shortToBeDeleted = getShort(shortId).value();
        Result<User> result = userClient.getUser(shortToBeDeleted.getOwnerId(), password);
        if (!result.isOK())
            return Result.error(result.error());

        datastore.delete(shortToBeDeleted);

        List<LikeShort> shortLikes = datastore.sql("SELECT * FROM LikeShort l WHERE l.shortLiked LIKE '"
                + shortId + "'", LikeShort.class);
        shortLikes.forEach(like -> datastore.delete(like));

        Result<Void> resultBlob = blobClient.deleteBlob(shortId);
        if (!resultBlob.isOK()) return Result.error(resultBlob.error());

        return Result.ok();
    }

    @Override
    public Result<Short> getShort(String shortId) {
        Log.info("getting short: short = " + shortId);

        List<Short> result = datastore.sql("SELECT * FROM Short s WHERE s.shortId LIKE '" + shortId + "'",
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
                datastore.sql("SELECT s.shortId FROM Short s WHERE s.ownerId LIKE '"
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
        Result<User> result1 = userClient.getUser(userId1, password);
        Result<User> result2 = getUser(userId2);
        if (!result1.isOK())
            return Result.error(result1.error());

        if (!result2.isOK())
            return Result.error(result2.error());

        List<Follow> follow = datastore.sql("SELECT * FROM Follow f WHERE f.followed = '"
                + userId2 + "' AND f.follower = '" + userId1 + "'", Follow.class);

        if (isFollowing && follow.isEmpty()) {
            Follow newFollow = new Follow(UUID.randomUUID().toString(), userId1, userId2);
            datastore.persist(newFollow);
        }
        else if (!isFollowing && !follow.isEmpty())
            datastore.delete(follow.get(0));
        else if (!isFollowing)
            return Result.ok();
        else
            return Result.error(Result.ErrorCode.CONFLICT);

        return Result.ok();
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        Log.info("followers : user = " + userId);

        Result<User> result = userClient.getUser(userId, password);
        if (!result.isOK())
            return Result.error(result.error());

        List<String> followers = datastore.sql("SELECT f.follower FROM Follow f WHERE f.followed = '"
                + userId + "'", String.class);

        return Result.ok(followers);
    }

    @Override
    public Result<Void> deleteFollowers(String userId) {
        Log.info("delete followers : user = " + userId);
        List<Follow> follows = datastore.sql("SELECT * FROM Follow f WHERE f.followed = '" + userId +
                "' OR f.follower = '" + userId + "'", Follow.class);
        follows.forEach(follow -> datastore.delete(follow));
        return Result.ok();
    }

    //return all the users that are followed by the userId
    private Result<List<String>> following(String userId, String password) {
        Log.info("following : user = " + userId);

        Result<User> result = userClient.getUser(userId, password);
        if (!result.isOK())
            return Result.error(result.error());

        List<String> following = datastore.sql("SELECT f.followed FROM Follow f WHERE f.follower = '"
                + userId + "'", String.class);

        return Result.ok(following);
    }


    private void changeLikesNr(Short userShort, boolean isLiked) {
        int shortLikes = userShort.getTotalLikes();
        if (isLiked) {
            userShort.setTotalLikes(shortLikes+1);
        }
        else {
            userShort.setTotalLikes(shortLikes-1);
        }
        datastore.update(userShort);
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

            datastore.persist(newLikeShort);
        }
        else {
            List<LikeShort> likeResult =
                    datastore.sql("SELECT * FROM LikeShort l WHERE l.shortLiked = '" + shortId
                            + "' AND l.user = '" + userId + "'", LikeShort.class);

            if (likeResult.isEmpty()) {
                Log.info("Like does not exist.");
                return Result.error(Result.ErrorCode.NOT_FOUND);
            }
            datastore.delete(likeResult.get(0));
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
        Result<User> ownerResult = userClient.getUser(shortResult.value().getOwnerId(), password);
        if (!ownerResult.isOK()) {
            Log.info("Password is incorrect.");
            return Result.error( Result.ErrorCode.FORBIDDEN);
        }

        List<String> likes = datastore.sql("SELECT l.user FROM LikeShort l WHERE l.shortLiked LIKE '"
                + shortId + "'", String.class);

        return Result.ok(likes);
    }

    public Result<Void> deleteLikes(String userId) {
        Log.info("delete likes : user = " + userId);

        List<LikeShort> likes = datastore.sql("SELECT * FROM LikeShort l WHERE l.user = '"
                + userId + "'", LikeShort.class);
        likes.forEach(userLike -> {
            datastore.delete(userLike);
            Short shortLiked = getShort(userLike.getShortLiked()).value();
            changeLikesNr(shortLiked, false);
        });

        return Result.ok();
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        Log.info("feed : user = " + userId);

        Result<List<String>> followingResult = following(userId, password);
        if (!followingResult.isOK()) {
            return Result.error(followingResult.error());
        }

        List<String> feed = getShorts(userId).value();
        List<String> following = followingResult.value();

        if (!following.isEmpty()) {
            feed.addAll(datastore.sql("SELECT s.shortId FROM Short s JOIN Follow f " +
                    "ON s.ownerId = f.followed WHERE f.follower = '" + userId + "'", String.class));
        }

        feed.sort(Comparator.comparing(shortId -> getShort(shortId.toString()).value().getTimestamp()).reversed());

        return Result.ok(feed);
    }

}
