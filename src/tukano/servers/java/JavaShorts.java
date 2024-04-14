package tukano.servers.java;

import tukano.api.Short;
import tukano.api.java.Result;
import tukano.api.java.Shorts;

import java.util.List;
import java.util.logging.Logger;

public class JavaShorts implements Shorts {
    private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

    @Override
    public Result<Short> createShort(String userId, String password) {
        return null;
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        return null;
    }

    @Override
    public Result<Short> getShort(String shortId) {
        return null;
    }

    @Override
    public Result<List<String>> getShorts(String userId) {
        return null;
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        return null;
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        return null;
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        return null;
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        return null;
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        return null;
    }
}
