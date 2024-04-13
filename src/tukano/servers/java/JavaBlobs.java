package tukano.servers.java;

import tukano.api.User;
import tukano.api.java.Blobs;
import tukano.api.java.Result;

import java.util.HashMap;
import java.util.Map;

public class JavaBlobs implements Blobs {

    Map<String, User> blobMap;

    public JavaBlobs() { blobMap = new HashMap<>(); }

    public Result<Void> upload(String blobId, byte[] bytes) {
        return null;
    }

    public Result<byte[]> download(String blobId) {
        return null;
    }
}
