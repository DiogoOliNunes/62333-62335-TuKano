package tukano.clients.grpc;

import io.grpc.ManagedChannelBuilder;
import tukano.api.Short;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.impl.grpc.generated_java.ShortsGrpc;
import tukano.impl.grpc.generated_java.ShortsProtoBuf;
import tukano.impl.grpc.generated_java.UsersGrpc;
import tukano.impl.grpc.generated_java.UsersProtoBuf;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static utils.DataModelAdaptor.GrpcShort_to_Short;
import static utils.DataModelAdaptor.User_to_GrpcUser;

public class GrpcShortsClient extends GrpcClient implements Shorts {

    //private static final long GRPC_REQUEST_TIMEOUT = 5000;
    final ShortsGrpc.ShortsBlockingStub stub;

    public GrpcShortsClient(URI serverURI) {
        var channel = ManagedChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort()).usePlaintext().build();
        stub = ShortsGrpc.newBlockingStub(channel);
        //stub = ShortsGrpc.newBlockingStub( channel ).withDeadlineAfter(GRPC_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Override
    public Result<Short> createShort(String userId, String password) {
        return toJavaResult(() -> {
            var res = stub.createShort(ShortsProtoBuf.CreateShortArgs.newBuilder()
                    .setUserId(userId)
                    .setPassword(password).build());
            return GrpcShort_to_Short(res.getValue());
        });
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        return toJavaResult(() -> {
            var res = stub.deleteShort(ShortsProtoBuf.DeleteShortArgs.newBuilder()
                    .setShortId(shortId)
                    .setPassword(password)
                    .build());
            return null;
        });
    }

    @Override
    public Result<Short> getShort(String shortId) {
        return toJavaResult(() -> {
            var res = stub.getShort(ShortsProtoBuf.GetShortArgs.newBuilder()
                    .setShortId(shortId)
                    .build());
            return GrpcShort_to_Short(res.getValue());
        });
    }

    @Override
    public Result<List<String>> getShorts(String userId) {
        return toJavaResult(() -> {
            var res = stub.getShorts(ShortsProtoBuf.GetShortsArgs.newBuilder()
                    .setUserId(userId)
                    .build());
            return res.getShortIdList();
        });
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        return toJavaResult(() -> {
            var res = stub.follow(ShortsProtoBuf.FollowArgs.newBuilder()
                    .setUserId1(userId1)
                    .setUserId2(userId2)
                    .setIsFollowing(isFollowing)
                    .setPassword(password)
                    .build());
            return null;
        });
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        return toJavaResult(() -> {
            var res = stub.followers(ShortsProtoBuf.FollowersArgs.newBuilder()
                    .setUserId(userId)
                    .setPassword(password)
                    .build());
            return res.getUserIdList();
        });
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        return toJavaResult(() -> {
            var res = stub.like(ShortsProtoBuf.LikeArgs.newBuilder()
                    .setShortId(shortId)
                    .setUserId(userId)
                    .setIsLiked(isLiked)
                    .setPassword(password)
                    .build());
            return null;
        });
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        return toJavaResult(() -> {
            var res = stub.likes(ShortsProtoBuf.LikesArgs.newBuilder()
                    .setShortId(shortId)
                    .setPassword(password)
                    .build());
            return res.getUserIdList();
        });
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        return toJavaResult(() -> {
            var res = stub.getFeed(ShortsProtoBuf.GetFeedArgs.newBuilder()
                    .setUserId(userId)
                    .setPassword(password)
                    .build());
            return res.getShortIdList();
        });
    }

    @Override
    public Result<Void> deleteLikes(String userId) {
        return toJavaResult(() -> {
            var res = stub.deleteLikes(ShortsProtoBuf.DeleteLikesArgs.newBuilder()
                    .setUserId(userId)
                    .build());
            return null;
        });
    }

    @Override
    public Result<Void> deleteFollowers(String userId) {
        return toJavaResult(() -> {
            var res = stub.deleteFollowers(ShortsProtoBuf.DeleteFollowersArgs.newBuilder()
                    .setUserId(userId)
                    .build());
            return null;
        });
    }
}
