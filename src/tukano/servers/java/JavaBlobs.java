package tukano.servers.java;

import Discovery.Discovery;
import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.clients.factories.ShortsClientFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Logger;

public class JavaBlobs implements Blobs {
    private static Logger Log = Logger.getLogger(JavaBlobs.class.getName());

    private static final String BLOB_COLLECTION = "./blobs/";

    URI[] uri = Discovery.getInstance().knownUrisOf("shorts",1);

    public JavaBlobs() { }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        Log.info("Upload of blob: " + blobId);

        if (!ShortsClientFactory.getClient(uri[0]).getShort(blobId).value().getShortId().equals(blobId)) {
            Log.info("Blob ID invalid.");
            return Result.error(Result.ErrorCode.FORBIDDEN);
        }

        try {
            Path dir = Paths.get(BLOB_COLLECTION);
            if (!Files.exists(dir))
                Files.createDirectories(dir);

            Path blob = Path.of(BLOB_COLLECTION, blobId);
            if (!Files.exists(blob)) {
                Files.write(blob, bytes);
                return Result.ok();
            }

            byte[] blobContent = Files.readAllBytes(blob);
            if (Arrays.equals(blobContent,bytes)) return Result.ok();

            return Result.error(Result.ErrorCode.CONFLICT);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<byte[]> download(String blobId) {
        Log.info("Download of blob: " + blobId);
        try {
            Path blobPath = Path.of(BLOB_COLLECTION, blobId);

            if (!Files.exists(blobPath))
                return Result.error(Result.ErrorCode.NOT_FOUND);


            byte[] blobContent = Files.readAllBytes(blobPath);
            return Result.ok(blobContent);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Void> deleteBlob(String blobId) {
        Log.info("Delete of blob: " + blobId);

        try {
            Path dir = Paths.get(BLOB_COLLECTION);
            if (!Files.isDirectory(dir)) return Result.ok();
            Path blobPath = Path.of(BLOB_COLLECTION, blobId);

            if (!Files.exists(blobPath))
                return Result.error(Result.ErrorCode.NOT_FOUND);

            Files.delete(blobPath);
            return Result.ok();
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }
}