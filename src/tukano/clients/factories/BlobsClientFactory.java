package tukano.clients.factories;

import tukano.api.java.Blobs;
import tukano.clients.grpc.GrpcBlobsClient;
import tukano.clients.rest.RestBlobsClient;

import Discovery.Discovery;

import java.net.URI;

public class BlobsClientFactory {

    public BlobsClientFactory() {}

    public static Blobs getClient(URI uri) {
        if( uri.toString().endsWith("rest"))
            return new RestBlobsClient( uri );
        else
            return new GrpcBlobsClient( uri );
    }
}
