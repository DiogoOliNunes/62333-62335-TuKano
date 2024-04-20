package tukano.clients.rest;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tukano.api.java.Blobs;
import tukano.api.java.Result;

import java.net.URI;
import java.util.logging.Logger;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response.Status;
import tukano.api.rest.RestBlobs;


public class RestBlobsClient  extends RestClient implements Blobs {

    private static Logger Log = Logger.getLogger(RestBlobsClient.class.getName());

    protected static final int READ_TIMEOUT = 10000;
    protected static final int CONNECT_TIMEOUT = 10000;

    protected static final int MAX_RETRIES = 3;
    protected static final int RETRY_SLEEP = 1000;

    final URI serverURI;
    final Client client;
    final ClientConfig config;
    final WebTarget target;

    public RestBlobsClient(URI serverURI) {
        this.serverURI = serverURI;
        this.config = new ClientConfig();

        config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

        this.client = ClientBuilder.newClient(config);
        target = client.target(serverURI).path(RestBlobs.PATH);
    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        return super.reTry( () -> clt_upload(blobId, bytes));
    }

    private Result<Void> clt_upload(String blobId, byte[] bytes) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response response = target.path(blobId)
                        .request()
                        .put(Entity.entity(bytes, MediaType.APPLICATION_JSON));

                if (response.getStatus() == Status.OK.getStatusCode()) {
                    return Result.ok();
                } else {
                    return Result.error(getErrorCodeFrom(response.getStatus()));
                }
            } catch (ProcessingException e) {
                Log.info(e.getMessage());
                utils.Sleep.ms(RETRY_SLEEP);
            }
        }
        return Result.error(Result.ErrorCode.TIMEOUT);
    }

    @Override
    public Result<byte[]> download(String blobId) {
        return super.reTry( () -> clt_download(blobId));
    }

    private Result<byte[]> clt_download(String blobId) {
        Response response = target.path(blobId)
                .request(MediaType.APPLICATION_JSON)
                .get();

        if (response.getStatus() == Status.OK.getStatusCode()) {
            byte[] bytes = response.readEntity(byte[].class);
            return Result.ok(bytes);
        } else {
            return Result.error(getErrorCodeFrom(response.getStatus()));
        }
    }

    @Override
    public Result<Void> deleteBlob(String blobId) {
        return super.reTry( () -> clt_deleteBlob(blobId));
    }

    private Result<Void> clt_deleteBlob(String blobId) {
        Response r = target
                .path(blobId)
                .request()
                .delete();

        var status = r.getStatus();
        if (status != Status.NO_CONTENT.getStatusCode())
            return Result.error(getErrorCodeFrom(status));
        else
            return Result.ok();
    }
}
