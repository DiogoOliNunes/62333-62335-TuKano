package tukano.servers.grpc;

import Discovery.Discovery;
import io.grpc.ServerBuilder;
import tukano.api.java.Shorts;

import java.net.InetAddress;
import java.util.logging.Logger;

public class GrpcShortsServer {
    public static final int PORT = 9000;

    private static final String GRPC_CTX = "/gprc";
    private static final String SERVER_BASE_URI = "grpc://%s:%s%s";

    private static Logger Log = Logger.getLogger(GrpcShortsServer.class.getName());

    public static void main(String[] args) throws Exception {

        var stub = new GrpcShortsServerStub();
        var server = ServerBuilder.forPort(PORT).addService(stub).build();
        var serverURI = String.format(SERVER_BASE_URI, InetAddress.getLocalHost().getHostAddress(), PORT, GRPC_CTX);
        Discovery.getInstance().announce(Shorts.NAME, serverURI);

        Log.info(String.format("%s gRPC Server ready @ %s\n", Shorts.NAME, serverURI));
        server.start().awaitTermination();
    }
}
