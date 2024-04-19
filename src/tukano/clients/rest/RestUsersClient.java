package tukano.clients.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Users;
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


public class RestUsersClient extends RestClient implements Users {

    private static Logger Log = Logger.getLogger(RestUsersClient.class.getName());

    protected static final int READ_TIMEOUT = 5000;
    protected static final int CONNECT_TIMEOUT = 5000;

    protected static final int MAX_RETRIES = 10;
    protected static final int RETRY_SLEEP = 5000;


    final URI serverURI;
    final Client client;
    final ClientConfig config;

    final WebTarget target;

    public RestUsersClient( URI serverURI ) {
        this.serverURI = serverURI;

        this.config = new ClientConfig();

        config.property( ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property( ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);


        this.client = ClientBuilder.newClient(config);

        target = client.target( serverURI ).path( RestUsers.PATH );
    }

    @Override
    public Result<String> createUser(User user) {

        for(int i = 0; i < MAX_RETRIES ; i++) {
            try {
                Response r = target.request()
                        .accept( MediaType.APPLICATION_JSON)
                        .post(Entity.entity(user, MediaType.APPLICATION_JSON));


                var status = r.getStatus();
                if( status != Status.OK.getStatusCode() )
                    return Result.error( getErrorCodeFrom(status));
                else
                    return Result.ok( r.readEntity( String.class ));

            } catch( ProcessingException x ) {
                Log.info(x.getMessage());
                utils.Sleep.ms( RETRY_SLEEP );
            }
            catch( Exception x ) {
                x.printStackTrace();
            }
        }
        return Result.error(Result.ErrorCode.TIMEOUT);
    }

    @Override
    public Result<User> getUser(String userId, String pwd) {
        Response r = target.path( userId )
                .queryParam(RestUsers.PWD, pwd).request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        var status = r.getStatus();
        if( status != Status.OK.getStatusCode() )
            return Result.error( getErrorCodeFrom(status));
        else
            return Result.ok( r.readEntity( User.class ));
    }

    @Override
    public Result<User> updateUser(String userId, String password, User user) {
        Response r = target.path( userId )
                .queryParam(RestUsers.PWD, password).request()
                .put(Entity.entity(user, MediaType.APPLICATION_JSON));

        var status = r.getStatus();
        if( status != Status.OK.getStatusCode() )
            return Result.error( getErrorCodeFrom(status));
        else
            return Result.ok( r.readEntity( User.class ));
    }

    @Override
    public Result<User> deleteUser(String userId, String password) {
        Response r = target.path( userId )
                .queryParam(RestUsers.PWD, password).request()
                .delete();

        var status = r.getStatus();
        if( status != Status.OK.getStatusCode() )
            return Result.error( getErrorCodeFrom(status));
        else
            return Result.ok( r.readEntity( User.class ));
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        Response r = target.queryParam(RestUsers.QUERY, pattern)
                .request()
                .get();

        var status = r.getStatus();
        if( status != Status.OK.getStatusCode() )
            return Result.error( getErrorCodeFrom(status));
        else
            return Result.ok( r.readEntity( new GenericType<List<User>>() {}));
    }

    public static Result.ErrorCode getErrorCodeFrom(int status) {
        return switch (status) {
            case 200, 209 -> Result.ErrorCode.OK;
            case 409 -> Result.ErrorCode.CONFLICT;
            case 403 -> Result.ErrorCode.FORBIDDEN;
            case 404 -> Result.ErrorCode.NOT_FOUND;
            case 400 -> Result.ErrorCode.BAD_REQUEST;
            case 500 -> Result.ErrorCode.INTERNAL_ERROR;
            case 501 -> Result.ErrorCode.NOT_IMPLEMENTED;
            default -> Result.ErrorCode.INTERNAL_ERROR;
        };
    }

}
