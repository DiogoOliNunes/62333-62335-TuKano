package tukano.clients.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import tukano.api.Short;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.api.rest.RestShorts;
import tukano.api.rest.RestUsers;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public class RestShortsClient extends RestClient implements Shorts {

    private static Logger Log = Logger.getLogger(RestShortsClient.class.getName());

    protected static final int READ_TIMEOUT = 10000;
    protected static final int CONNECT_TIMEOUT = 10000;

    protected static final int MAX_RETRIES = 3;
    protected static final int RETRY_SLEEP = 1000;

    final URI serverURI;
    final Client client;
    final ClientConfig config;
    final WebTarget target;

    public RestShortsClient(URI serverURI) {
        this.serverURI = serverURI;
        this.config = new ClientConfig();
        config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
        this.client = ClientBuilder.newClient(config);
        target = client.target(serverURI).path(RestShorts.PATH);
    }

    @Override
    public Result<Short> createShort(String userId, String password) {
        return super.reTry( () -> clt_createShort(userId, password));
    }

    private Result<Short> clt_createShort(String userId, String password) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.request()
                        .accept(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(null, MediaType.APPLICATION_JSON));

                var status = r.getStatus();
                if (status != Status.OK.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
                else
                    return Result.ok(r.readEntity(Short.class));
            } catch (ProcessingException x) {
                Log.info(x.getMessage());
                utils.Sleep.ms(RETRY_SLEEP);
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return Result.error(Result.ErrorCode.TIMEOUT);
    }

    @Override
    public Result<Short> getShort(String shortId) {
        return super.reTry( () -> clt_getShort(shortId));
    }

    private Result<Short> clt_getShort(String shortId) {
        Response r = target.path(shortId)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        var status = r.getStatus();
        if (status != Status.OK.getStatusCode())
            return Result.error(getErrorCodeFrom(status));
        else
            return Result.ok(r.readEntity(Short.class));
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        return super.reTry( () -> clt_deleteShort(shortId, password));
    }

    private Result<Void> clt_deleteShort(String shortId, String password) {
        Response r = target.path(shortId)
                .queryParam(RestUsers.PWD, password)
                .request()
                .delete();

        var status = r.getStatus();
        if (status != Status.OK.getStatusCode())
            return Result.error(getErrorCodeFrom(status));
        else
            return Result.ok();
    }

    @Override
    public Result<List<String>> getShorts(String userId) {
        return super.reTry( () -> clt_getShorts(userId));
    }

    private Result<List<String>> clt_getShorts(String userId) {
        try {
            Response response = target
                    .path(userId)
                    .path("shorts")
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .get();

            int status = response.getStatus();
            if (status == Status.OK.getStatusCode()) {
                List<String> shorts = response.readEntity(new GenericType<List<String>>() {});
                return Result.ok(shorts);
            } else {
                return Result.error(getErrorCodeFrom(status));
            }
        } catch (ProcessingException e) {
            Log.info(e.getMessage());
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        return super.reTry( () -> clt_follow(userId1, userId2, isFollowing, password));
    }

    private Result<Void> clt_follow(String userId1, String userId2, boolean isFollowing, String password) {
        String action = isFollowing ? "follow" : "unfollow";
        Response response = target
                .path(userId1)
                .queryParam("followerId", userId2)
                .queryParam("action", action)
                .queryParam("password", password)
                .request()
                .post(Entity.text(""));

        int status = response.getStatus();
        if (status == Status.OK.getStatusCode()) {
            return Result.ok(null);
        } else {
            return Result.error(getErrorCodeFrom(status));
        }
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        return super.reTry( () -> clt_followers(userId, password));
    }

    private Result<List<String>> clt_followers(String userId, String password) {
        Response response = target
                .path(userId)
                .queryParam("password", password)
                .request()
                .get();

        int status = response.getStatus();
        if (status == Status.OK.getStatusCode()) {
            List<String> followers = response.readEntity(new GenericType<List<String>>() {});
            return Result.ok(followers);
        } else {
            return Result.error(getErrorCodeFrom(status));
        }
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        return super.reTry( () -> clt_like(shortId, userId, isLiked, password));
    }

    private Result<Void> clt_like(String shortId, String userId, boolean isLiked, String password) {
        Response response = target
                .path(shortId)
                .queryParam("userId", userId)
                .queryParam("isLiked", isLiked)
                .queryParam("password", password)
                .request()
                .put(Entity.text(""));

        int status = response.getStatus();
        if (status == Status.OK.getStatusCode()) {
            return Result.ok();
        } else {
            return Result.error(getErrorCodeFrom(status));
        }
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        return super.reTry( () -> clt_like(shortId, password));
    }

    private Result<List<String>> clt_like(String shortId, String password) {
        Response response = target
                .path(shortId)
                .queryParam("password", password)
                .request()
                .get();

        int status = response.getStatus();
        if (status == Status.OK.getStatusCode()) {
            List<String> likes = response.readEntity(new GenericType<List<String>>() {});
            return Result.ok(likes);
        } else {
            return Result.error(getErrorCodeFrom(status));
        }
    }

    @Override
    public Result<Void> deleteLikes(String userId) {
        return super.reTry( () -> clt_deleteLikes(userId));
    }

    private Result<Void> clt_deleteLikes(String userId) {
        Response r = target.path(userId)
                .path(RestShorts.DELETING)
                .request()
                .delete();

        int status = r.getStatus();
        if (status == Status.OK.getStatusCode()) {
            return Result.ok();
        } else {
            return Result.error(getErrorCodeFrom(status));
        }
    }

    @Override
    public Result<Void> deleteFollowers(String userId) {
        return super.reTry( () -> clt_deleteFollowers(userId));
    }

    private Result<Void> clt_deleteFollowers(String userId) {
        Response r = target.path(userId)
                .path(RestShorts.DELETE_FOLLOWERS)
                .request()
                .delete();

        int status = r.getStatus();
        if (status == Status.OK.getStatusCode()) {
            return Result.ok();
        } else {
            return Result.error(getErrorCodeFrom(status));
        }
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        return super.reTry( () -> clt_getFeed(userId, password));
    }

    private Result<List<String>> clt_getFeed(String userId, String password) {
        Response response = target
                .path("feed")
                .queryParam("userId", userId)
                .queryParam("password", password)
                .request()
                .get();

        int status = response.getStatus();
        if (status == Status.OK.getStatusCode()) {
            List<String> feed = response.readEntity(new GenericType<List<String>>() {});
            return Result.ok(feed);
        } else {
            return Result.error(getErrorCodeFrom(status));
        }
    }
}
