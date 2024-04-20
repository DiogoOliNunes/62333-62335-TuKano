package tukano.clients.factories;

import tukano.api.java.Users;
import tukano.clients.grpc.GrpcUsersClient;
import tukano.clients.rest.RestUsersClient;

import java.net.URI;

public class UsersClientFactory {
    public static Users getClient(URI uri) {
        if( uri.toString().endsWith("rest"))
            return new RestUsersClient( uri );
        else
            return new GrpcUsersClient( uri );
    }
}
