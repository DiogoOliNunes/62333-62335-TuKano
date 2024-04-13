package tukano.servers.rest;

import jakarta.ws.rs.PathParam;
import tukano.api.java.Blobs;
import tukano.api.rest.RestBlobs;
import tukano.servers.java.JavaBlobs;

public class RestBlobsResource implements RestBlobs {

    final Blobs impl;

    public RestBlobsResource() {
        impl = new JavaBlobs();
    }

    @Override
    public void upload(String blobId, byte[] bytes){

    }

    @Override
    public byte[] download(String blobId) {
        return null;
    }
}
