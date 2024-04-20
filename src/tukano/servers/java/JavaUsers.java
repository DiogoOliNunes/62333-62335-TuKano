package tukano.servers.java;

import Discovery.Discovery;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Users;
import tukano.clients.factories.ShortsClientFactory;
import tukano.persistence.Hibernate;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

public class JavaUsers implements Users {

    private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

    URI[] shortURI = Discovery.getInstance().knownUrisOf("shorts",1);

    public JavaUsers() { }

    @Override
    public Result<String> createUser(User user) {
        Log.info("createUser : " + user);

        // Check if user data is valid
        if(user.userId() == null || user.pwd() == null || user.displayName() == null || user.email() == null) {
            Log.info("User object invalid.");
            return Result.error( Result.ErrorCode.BAD_REQUEST);
        }
        List<User> result = Hibernate.getInstance().sql("SELECT * FROM User u WHERE u.userId LIKE '" + user.userId() + "'",
                User.class);
        // Insert user, checking if name already exists
        if( !result.isEmpty() ) {
            Log.info("User already exists.");
            return Result.error( Result.ErrorCode.CONFLICT);
        }
        Hibernate.getInstance().persist(user);
        return Result.ok( user.userId() );
    }

    @Override
    public Result<User> getUser(String userId, String pwd) {
        Log.info("getUser : user = " + userId + "; pwd = " + pwd);

        // Check if user is valid
        if(userId == null || pwd == null) {
            Log.info("Name or Password null.");
            return Result.error( Result.ErrorCode.BAD_REQUEST);
        }

        List<User> result = Hibernate.getInstance().sql("SELECT * FROM User u WHERE u.userId LIKE '" + userId + "'",
                User.class);
        // Check if user exists
        if(result.isEmpty()) {
            Log.info("User does not exist.");
            return Result.error( Result.ErrorCode.NOT_FOUND);
        }
        User user = result.get(0);

        if( !user.pwd().equals( pwd)) {
            Log.info("Password is incorrect.");
            return Result.error( Result.ErrorCode.FORBIDDEN);
        }

        return Result.ok(user);
    }

    @Override
    public Result<User> updateUser(String userId, String pwd, User user) {
        Log.info("updateUser : user = " + userId + "; pwd = " + pwd + "\n" + "newUser = " + user);


        if (!userId.equals(user.userId()) && user.userId() != null)
            return Result.error(Result.ErrorCode.BAD_REQUEST);

        var result = getUser(userId, pwd);
        if (!result.isOK())
            return result;
        User updatedUser = changeAttributes(result.value(), user);
        return Result.ok(updatedUser);
    }

    private User changeAttributes(User oldUser, User updatedUser) {
        if (updatedUser.pwd() != null)
            oldUser.setPwd(updatedUser.pwd());
        if (updatedUser.email() != null)
            oldUser.setEmail(updatedUser.email());
        if (updatedUser.displayName() != null)
            oldUser.setDisplayName(updatedUser.displayName());

        Hibernate.getInstance().update(oldUser);
        return oldUser;
    }

    @Override
    public Result<User> deleteUser(String userId, String pwd) {
        Log.info("deleteUser : user = " + userId + "; pwd = " + pwd);

        var result = getUser(userId, pwd);
        if (!result.isOK())
            return result;

        List<String> userShorts = ShortsClientFactory.getClient(shortURI[0]).getShorts(userId).value();
        userShorts.forEach(userShort -> ShortsClientFactory.getClient(shortURI[0]).deleteShort(userShort, pwd));

        ShortsClientFactory.getClient(shortURI[0]).deleteFollowers(userId);
        ShortsClientFactory.getClient(shortURI[0]).deleteLikes(userId);

        User userToBeRemoved = result.value();
        Hibernate.getInstance().delete(userToBeRemoved);

        return Result.ok(userToBeRemoved);
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        Log.info("searchUsers : pattern = " + pattern);

        List<User> users = Hibernate.getInstance().sql("SELECT * FROM User", User.class);
        if (pattern != null) users.removeIf(user -> !user.userId().toLowerCase().contains(pattern.toLowerCase()));
        else return Result.error(Result.ErrorCode.BAD_REQUEST);

        users.forEach(user -> user.setPwd(""));
        return Result.ok(users);
    }
}