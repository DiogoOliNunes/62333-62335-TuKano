package tukano.servers.java;

import tukano.api.Follow;
import tukano.api.Short;
import tukano.api.Like;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Shorts;

import tukano.persistence.Hibernate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class JavaShorts implements Shorts {
    private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

    Hibernate datastore;

    public JavaShorts () {
        datastore = Hibernate.getInstance();
    }

    private Result<User> getOwner(String userId) {
        List<User> result = datastore.sql("SELECT * FROM User u WHERE u.userId LIKE '" + userId + "'",
                User.class);
        if(result.isEmpty()) {
            Log.info("User does not exist.");
            return Result.error( Result.ErrorCode.NOT_FOUND);
        }
        User user = result.get(0);
        return Result.ok(user);
    }
    @Override
    public Result<Short> createShort(String userId, String password) {
        Log.info("create short: user = " + userId + "; pwd = " + password);

        if(userId == null || password == null) {
            Log.info("Name or Password null.");
            return Result.error( Result.ErrorCode.BAD_REQUEST);
        }
        Result<User> result = getOwner(userId);
        if (!result.isOK()) {
            Log.info("User does not exist.");
            return Result.error( Result.ErrorCode.NOT_FOUND);
        }
        if( !result.value().pwd().equals(password)) {
            Log.info("Password is incorrect.");
            return Result.error( Result.ErrorCode.FORBIDDEN);
        }
        //TODO: meter o blobURL, para ja vou meter a null
        Short newShort = new Short(UUID.randomUUID().toString(), userId, null);
        datastore.persist(newShort);
        return Result.ok(newShort);
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        Log.info("delete short: short = " + shortId + "; pwd = " + password);

        Short shortToBeDeleted = getShort(shortId).value();
        Result<User> result = getOwner(shortToBeDeleted.getOwnerId());

        if (!result.isOK()) {
            Log.info("User does not exist.");
            return Result.error( Result.ErrorCode.NOT_FOUND);
        }
        if (!result.value().pwd().equals(password)) {
            Log.info("Password is incorrect.");
            return Result.error( Result.ErrorCode.FORBIDDEN);
        }
        datastore.delete(shortToBeDeleted);
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
        Result<User> result = getOwner(userId);
        if (!result.isOK()) {
            Log.info("User does not exist.");
            return Result.error( Result.ErrorCode.NOT_FOUND);
        }
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
        Result<User> result1 = getOwner(userId1);
        Result<User> result2 = getOwner(userId2);
        if (!result1.isOK() || !result2.isOK())
            return Result.error( Result.ErrorCode.NOT_FOUND);
        if (!result1.value().pwd().equals(password)) {
            Log.info("Password is incorrect.");
            return Result.error( Result.ErrorCode.FORBIDDEN);
        }
        if (isFollowing) {
            Follow newFollow = new Follow(UUID.randomUUID().toString(), userId1, userId2);
            datastore.persist(newFollow);
        } else {
            List<String> followId = datastore.sql("SELECT f.followId FROM Follow f WHERE f.followed LIKE '"
                    + userId2 + "' AND f.follower LIKE '" + userId1 + "'", String.class);
            datastore.delete(followId);
        }
        return Result.ok();
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        Log.info("followers : user = " + userId);
        Result<User> result = getOwner(userId);
        if (!result.isOK()) {
            Log.info("User does not exist.");
            return Result.error( Result.ErrorCode.NOT_FOUND);
        }
        if (!result.value().pwd().equals(password)) {
            Log.info("Password is incorrect.");
            return Result.error( Result.ErrorCode.FORBIDDEN);
        }
        List<String> followers = datastore.sql("SELECT f.follower FROM Follow f WHERE f.followed LIKE '"
                + userId + "'", String.class);

        return Result.ok(followers);
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        Log.info("like : short = " + shortId + "; user = " + userId);

        Result<Short> shortResult = getShort(shortId);
        if (!shortResult.isOK()) {
            Log.info("Short does not exist.");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        if (isLiked) {
            Like newLike = new Like(UUID.randomUUID().toString(), shortId, userId);
            datastore.persist(newLike);
        } else {
            List<String> likeResult =
                    datastore.sql("SELECT l.likeId FROM Like l WHERE l.shortLiked LIKE '" + shortId
                            + "' AND l.user LIKE '" + userId + "'", String.class);
            if (likeResult.isEmpty()) {
                Log.info("Like does not exist.");
                return Result.error(Result.ErrorCode.NOT_FOUND);
            }
            datastore.delete(likeResult);
        }
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
        Result<User> ownerResult = getOwner(shortResult.value().getOwnerId());
        /*
        if (!ownerResult.isOK()) {
            Log.info("User does not exist.");
            return Result.error( Result.ErrorCode.NOT_FOUND);
        }
        */
        if (!ownerResult.value().pwd().equals(password)) {
            Log.info("Password is incorrect.");
            return Result.error( Result.ErrorCode.FORBIDDEN);
        }

        List<String> likes = datastore.sql("SELECT l.likeId FROM Like l WHERE l.shortLiked LIKE '"
                + shortId + "'", String.class);

        return Result.ok(likes);
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        Log.info("feed : user = " + userId);

        Result<User> result = getOwner(userId);
        if (!result.isOK()) {
            Log.info("User does not exist.");
            return Result.error( Result.ErrorCode.NOT_FOUND);
        }
        if (!result.value().pwd().equals(password)) {
            Log.info("Password is incorrect.");
            return Result.error( Result.ErrorCode.FORBIDDEN);
        }
        List<String> feed = new ArrayList<>();
        List<String> followers = followers(userId, password).value();
        followers.forEach(follower -> {feed.addAll(getShorts(follower).value());});
        feed.sort(Comparator.comparing(Short::getTimestamp));
        return null;
    }
}
