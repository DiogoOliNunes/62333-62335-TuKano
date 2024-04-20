package tukano.clients.grpc;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannelBuilder;
import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.impl.grpc.generated_java.BlobsGrpc;
import tukano.impl.grpc.generated_java.BlobsProtoBuf;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class GrpcBlobsClient extends GrpcClient implements Blobs {
    //private static final long GRPC_REQUEST_TIMEOUT = 5000;
    final BlobsGrpc.BlobsBlockingStub stub;

    public GrpcBlobsClient(URI serverURI) {
        var channel = ManagedChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort()).usePlaintext().build();
        //stub = BlobsGrpc.newBlockingStub( channel ).withDeadlineAfter(GRPC_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        stub = BlobsGrpc.newBlockingStub(channel);
    }
    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        return toJavaResult(() -> {
            var res = stub.upload(BlobsProtoBuf.UploadArgs.newBuilder()
                    .setBlobId(blobId)
                    .setData(ByteString.copyFrom(bytes)).build());
            return null;
        });
    }

    @Override
    public Result<byte[]> download(String blobId) {
        return toJavaResult(() -> {
            var res = stub.download(BlobsProtoBuf.DownloadArgs.newBuilder()
                    .setBlobId(blobId).build());
            return res.next().toByteArray();
        });
    }

    @Override
    public Result<Void> deleteBlob(String blobId) {
        return toJavaResult(() -> {
            var res = stub.deleteBlob(BlobsProtoBuf.DeleteBlobArgs.newBuilder()
                    .setBlobId(blobId)
                    .build());
            return null;
        });
    }
}
