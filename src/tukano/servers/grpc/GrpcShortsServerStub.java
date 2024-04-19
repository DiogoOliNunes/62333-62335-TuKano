package tukano.servers.grpc;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.impl.grpc.generated_java.ShortsGrpc;
import tukano.impl.grpc.generated_java.ShortsProtoBuf;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.CreateShortArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.DeleteShortArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.GetShortArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.GetShortsArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.FollowArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.FollowersArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.DeleteFollowersArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.LikeArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.LikesArgs;

import tukano.impl.grpc.generated_java.ShortsProtoBuf.CreateShortResult;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.DeleteShortResult;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.GetShortResult;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.GetShortsResult;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.FollowResult;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.FollowersResult;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.DeleteFollowersResult;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.LikeResult;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.LikesResult;

import tukano.servers.java.JavaShorts;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import static utils.DataModelAdaptor.GrpcShort_to_Short;
import static utils.DataModelAdaptor.Short_to_GrpcShort;


public class GrpcShortsServerStub extends GrpcServerStub implements ShortsGrpc.AsyncService, BindableService {

    Shorts impl = new JavaShorts();

    @Override
    public final ServerServiceDefinition bindService() {
        return ShortsGrpc.bindService(this);
    }

    @Override
    public void createShort(CreateShortArgs request,  StreamObserver<CreateShortResult> responseObserver)  {
        var res = impl.createShort(request.getUserId(), request.getPassword());
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext( CreateShortResult.newBuilder().setValue(Short_to_GrpcShort(res.value())).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteShort(DeleteShortArgs request,  StreamObserver<DeleteShortResult> responseObserver) {
        var res = impl.deleteShort(request.getShortId(), request.getPassword());
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext( DeleteShortResult.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getShort(GetShortArgs request, StreamObserver<GetShortResult> responseObserver) {
        var res = impl.getShort(request.getShortId());
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext( GetShortResult.newBuilder().setValue(Short_to_GrpcShort(res.value())).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getShorts(GetShortsArgs request, StreamObserver<GetShortsResult> responseObserver) {
        var res = impl.getShorts(request.getUserId());
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            List<String> returns = res.value();
            var shortRes = GetShortsResult.newBuilder();
            returns.forEach(shortRes::addShortId);
            responseObserver.onNext(shortRes.build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void follow(FollowArgs request, StreamObserver<FollowResult> responseObserver) {
        var res = impl.follow(request.getUserId1(), request.getUserId2(), request.getIsFollowing(),
                request.getPassword() );
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext(FollowResult.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void followers(FollowersArgs request, StreamObserver<FollowersResult> responseObserver) {
        var res = impl.followers(request.getUserId(), request.getPassword());
        if( !res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            List<String> returns = res.value();
            var shortRes = FollowersResult.newBuilder();
            returns.forEach(shortRes::addUserId);
            responseObserver.onNext(shortRes.build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteFollowers(DeleteFollowersArgs request, StreamObserver<DeleteFollowersResult> responseObserver) {
        var res = impl.deleteFollowers(request.getUserId());
        if( !res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext(DeleteFollowersResult.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void like(LikeArgs request, StreamObserver<LikeResult> responseObserver) {
        var res = impl.like(request.getShortId(), request.getUserId(), request.getIsLiked(), request.getPassword());
        if( !res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext(LikeResult.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void likes(LikesArgs request, StreamObserver<LikesResult> responseObserver) {
        var res = impl.likes(request.getShortId(), request.getPassword());
        if( !res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            List<String> returns = res.value();
            var likeRes = LikesResult.newBuilder();
            returns.forEach(likeRes::addUserId);
            responseObserver.onNext(likeRes.build());
            responseObserver.onCompleted();
        }
    }

    /*
    @Override
    public void deleteLikes(DeleteLikes request, StreamObserver<DeleteLikesResult> responseObserver) {
        var res = impl.likes(request.getShortId(), request.getPassword());
        if( !res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            List<String> returns = res.value();
            var likeRes = LikesResult.newBuilder();
            returns.forEach(likeRes::addUserId);
            responseObserver.onNext(likeRes.build());
            responseObserver.onCompleted();
        }
    }
     */

}
