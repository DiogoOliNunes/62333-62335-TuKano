package tukano.clients.factories;

import tukano.api.java.Shorts;
import tukano.clients.grpc.GrpcShortsClient;
import tukano.clients.rest.RestShortsClient;

import java.net.URI;

public class ShortsClientFactory {
    public static Shorts getClient(URI uri) {
        if( uri.toString().endsWith("rest"))
            return new RestShortsClient( uri );
        else
            return new GrpcShortsClient( uri );
    }
}
