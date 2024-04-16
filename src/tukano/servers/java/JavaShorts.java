package tukano.servers.java;

import Discovery.Discovery;
import tukano.api.Follow;
import tukano.api.Short;
import tukano.api.Like;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Shorts;

import tukano.clients.RestUsersClient;
import tukano.persistence.Hibernate;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class JavaShorts implements Shorts {
    private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

    Hibernate datastore;
    RestUsersClient client;
    URI[] uri;

    public JavaShorts () {
        datastore = Hibernate.getInstance();
        uri = Discovery.getInstance().knownUrisOf("users",1);
        client = new RestUsersClient(uri[0]);
    }

    private Result<User> getUser(String userId) {
        List<User> result = client.searchUsers(userId).value();
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

        Result<User> result = client.getUser(userId, password);
        if (!result.isOK())
            return Result.error(result.error());


        //TODO: meter o blobURL, para ja vou meter a null
        Short newShort = new Short(UUID.randomUUID().toString(), userId, null);
        datastore.persist(newShort);
        return Result.ok(newShort);
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        Log.info("delete short: short = " + shortId + "; pwd = " + password);

        Short shortToBeDeleted = getShort(shortId).value();
        Result<User> result = client.getUser(shortToBeDeleted.getOwnerId(), password);
        if (!result.isOK())
            return Result.error(result.error());

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
        Result<User> result1 = client.getUser(userId1, password);
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
        } else if (!isFollowing && !follow.isEmpty()) {
            datastore.delete(follow);
        } else
            return Result.error(Result.ErrorCode.CONFLICT);

        return Result.ok();
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        Log.info("followers : user = " + userId);

        Result<User> result = client.getUser(userId, password);
        if (!result.isOK())
            return Result.error(result.error());

        List<String> followers = datastore.sql("SELECT f.follower FROM Follow f WHERE f.followed = '"
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
        Result<User> ownerResult = client.getUser(shortResult.value().getOwnerId(), password);
        if (!ownerResult.isOK()) {
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

        Result<List<String>> followersResult = followers(userId, password);
        if (!followersResult.isOK()) {
            return Result.error(followersResult.error());
        }

        List<String> feed = getShorts(userId).value();
        List<String> followers = followersResult.value();

        if (!followers.isEmpty()) {



            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            Log.info("OS FOLLOWERS NAO SAO EMPTYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
            feed.addAll(datastore.sql("SELECT s.shortId FROM Short s JOIN Follow f " +
                    "ON s.ownerId = f.followed WHERE f.follower = '" + userId + "'", String.class));

            if (userId.equals("digna.hilll")){
            Log.info("FEED: "+ feed);
            Log.info("FEED: "+ feed);
            Log.info("FEED: "+ feed);
            Log.info("FEED: "+ feed);
            Log.info("FEED: "+ feed);
            Log.info("FEED: "+ feed);
            Log.info("FEED: "+ feed);
            Log.info("FEED: "+ feed);
            Log.info("FEED: "+ feed);
            Log.info("FEED: "+ feed);
            Log.info("FEED: "+ feed);
            Log.info("FEED: "+ feed);
            Log.info("FEED: "+ feed);
            Log.info("FEED: "+ feed);
            Log.info("FEED: "+ feed);
            Log.info("FEED: "+ feed);
            Log.info("FEED: "+ feed);
            Log.info("FEED: "+ feed);
            Log.info("FEED: "+ feed);
            Log.info("FEED: "+ feed);
            Log.info("FEED: "+ feed);
            Log.info("FEED: "+ feed);
            Log.info("FEED: "+ feed);
            Log.info("FEED: "+ feed);
        }}

        feed.sort(Comparator.comparing(shortId -> getShort((String) shortId).value().getTimestamp()).reversed());

        return Result.ok(feed);
    }

}
