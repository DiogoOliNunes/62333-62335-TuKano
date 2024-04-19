package tukano.servers.grpc;

import Discovery.Discovery;
import io.grpc.ServerBuilder;
import tukano.api.java.Blobs;

import java.net.InetAddress;
import java.util.logging.Logger;

public class GrpcBlobsServer {
    public static final int PORT = 9000;

    private static final String GRPC_CTX = "/gprc";
    private static final String SERVER_BASE_URI = "grpc://%s:%s%s";

    private static Logger Log = Logger.getLogger(GrpcBlobsServer.class.getName());

    public static void main(String[] args) throws Exception {

        var stub = new GrpcBlobsServerStub();
        var server = ServerBuilder.forPort(PORT).addService(stub).build();
        var serverURI = String.format(SERVER_BASE_URI, InetAddress.getLocalHost().getHostAddress(), PORT, GRPC_CTX);
        Discovery.getInstance().announce(Blobs.NAME, serverURI);

        Log.info(String.format("%s gRPC Server ready @ %s\n", Blobs.NAME, serverURI));
        server.start().awaitTermination();
    }
}
