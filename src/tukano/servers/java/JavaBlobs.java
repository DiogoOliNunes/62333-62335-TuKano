package tukano.servers.java;

import tukano.api.User;
import tukano.api.java.Blobs;
import tukano.api.java.Result;

import java.util.HashMap;
import java.util.Map;

public class JavaBlobs implements Blobs {

    Map<String, User> blobMap;

    public JavaBlobs() { blobMap = new HashMap<>(); }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        if (blobId == null || bytes == null) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        if (blobMap.containsKey(blobId)) {
            byte[] existingBytes = blobMap.get(blobId);
            if (!java.util.Arrays.equals(existingBytes, bytes)) {
                return Result.error(Result.ErrorCode.CONFLICT);
            }
        }

        blobMap.put(blobId, bytes);
        return Result.ok();
    }

    @Override
    public Result<byte[]> download(String blobId) {
        if (blobId == null) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        if (!blobMap.containsKey(blobId)) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        byte[] bytes = blobMap.get(blobId);
        return Result.ok(bytes);
    }
}
