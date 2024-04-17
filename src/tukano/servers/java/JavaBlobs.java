package tukano.servers.java;

import Discovery.Discovery;
import tukano.api.User;
import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.clients.RestShortsClient;
import tukano.clients.RestUsersClient;
import tukano.persistence.Hibernate;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class JavaBlobs implements Blobs {
    private static Logger Log = Logger.getLogger(JavaBlobs.class.getName());

    private Map<String, Path> blobFilePaths;
    private static final String BLOB_COLLECTION = "./blobs/";// blob collection, blob media center?
    Hibernate datastore;
    RestShortsClient client;
    URI[] uri;
    public JavaBlobs() {
        blobFilePaths = new HashMap<>();
        createBlobsDirectory();

        datastore = Hibernate.getInstance();
        uri = Discovery.getInstance().knownUrisOf("shorts",1);
        client = new RestShortsClient(uri[0]);
    }

    private void createBlobsDirectory() {
        try {
            Files.createDirectories(Path.of(BLOB_COLLECTION));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        //client.getShort(short.blobId??????????)
        //o blob está inicializado a null la em cada short, é preciso mudar e aqui é preciso comparar para ter certeza da existencia
        Path filePath = Path.of(BLOB_COLLECTION, blobId);
        try {
            Files.write(filePath, bytes);
            blobFilePaths.put(blobId, filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
        return Result.ok();
    }


    @Override
    public Result<byte[]> download(String blobId) {
        Path filePath = blobFilePaths.get(blobId);

        if (filePath == null || !Files.exists(filePath)) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        try {
            byte[] bytes = Files.readAllBytes(filePath);
            return Result.ok(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }
}
