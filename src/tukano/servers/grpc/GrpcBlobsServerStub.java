package tukano.servers.grpc;

import com.google.protobuf.ByteString;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;
import tukano.api.java.Blobs;
import tukano.impl.grpc.generated_java.BlobsGrpc;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.UploadArgs;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DownloadArgs;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.UploadResult;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DownloadResult;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DeleteBlobArgs;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DeleteBlobResult;
import tukano.servers.java.JavaBlobs;

import static utils.DataModelAdaptor.GrpcUser_to_User;

public class GrpcBlobsServerStub extends GrpcServerStub implements BlobsGrpc.AsyncService, BindableService {

    Blobs impl = new JavaBlobs();
    @Override
    public ServerServiceDefinition bindService() { return BlobsGrpc.bindService(this); }

    @Override
    public void upload(UploadArgs request, StreamObserver<UploadResult> responseObserver) {
        var res = impl.upload(request.getBlobId(), request.getData().toByteArray());
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext(UploadResult.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void download(DownloadArgs request, StreamObserver<DownloadResult> responseObserver) {
        var res = impl.download(request.getBlobId());
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext(DownloadResult.newBuilder().setChunk(ByteString.copyFrom(res.value())).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteBlob(DeleteBlobArgs request, StreamObserver<DeleteBlobResult> responseObserver) {
        var res = impl.deleteBlob(request.getBlobId());
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext(DeleteBlobResult.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

}
