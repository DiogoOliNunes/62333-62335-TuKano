package tukano.servers.grpc;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;
import tukano.api.User;
import tukano.api.java.Users;
import tukano.impl.grpc.generated_java.UsersGrpc;
import tukano.impl.grpc.generated_java.UsersProtoBuf.CreateUserArgs;
import tukano.impl.grpc.generated_java.UsersProtoBuf.CreateUserResult;
import tukano.impl.grpc.generated_java.UsersProtoBuf.DeleteUserArgs;
import tukano.impl.grpc.generated_java.UsersProtoBuf.DeleteUserResult;
import tukano.impl.grpc.generated_java.UsersProtoBuf.GetUserArgs;
import tukano.impl.grpc.generated_java.UsersProtoBuf.GetUserResult;
import tukano.impl.grpc.generated_java.UsersProtoBuf.GrpcUser;
import tukano.impl.grpc.generated_java.UsersProtoBuf.SearchUserArgs;
import tukano.impl.grpc.generated_java.UsersProtoBuf.UpdateUserArgs;
import tukano.impl.grpc.generated_java.UsersProtoBuf.UpdateUserResult;
import tukano.servers.java.JavaUsers;

import java.util.List;

import static utils.DataModelAdaptor.GrpcUser_to_User;
import static utils.DataModelAdaptor.User_to_GrpcUser;

public class GrpcUsersServerStub extends GrpcServerStub implements UsersGrpc.AsyncService, BindableService {

    Users impl = new JavaUsers();

    @Override
    public final ServerServiceDefinition bindService() {
        return UsersGrpc.bindService(this);
    }

    @Override
    public void createUser(CreateUserArgs request, StreamObserver<CreateUserResult> responseObserver) {
        var res = impl.createUser( GrpcUser_to_User(request.getUser()));
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext( CreateUserResult.newBuilder().setUserId( res.value() ).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getUser(GetUserArgs request, StreamObserver<GetUserResult> responseObserver) {
        var res = impl.getUser(request.getUserId(), request.getPassword());
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext( GetUserResult.newBuilder().setUser(User_to_GrpcUser(res.value())).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void updateUser(UpdateUserArgs request, StreamObserver<UpdateUserResult> responseObserver) {
        var res = impl.updateUser(request.getUserId(), request.getPassword(), GrpcUser_to_User(request.getUser()));
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext( UpdateUserResult.newBuilder().setUser(User_to_GrpcUser(res.value())).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteUser(DeleteUserArgs request, StreamObserver<DeleteUserResult> responseObserver) {
        var res = impl.deleteUser(request.getUserId(), request.getPassword());
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext( DeleteUserResult.newBuilder().setUser(User_to_GrpcUser(res.value())).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void searchUsers(SearchUserArgs request, StreamObserver<GrpcUser> responseObserver) {
        var res = impl.searchUsers(request.getPattern());
        if(!res.isOK())
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            List<User> users = res.value();
            for (User user : users) {
                GrpcUser grpcUser = User_to_GrpcUser(user);
                responseObserver.onNext(grpcUser);
            }
            responseObserver.onCompleted();
        }
    }
}
