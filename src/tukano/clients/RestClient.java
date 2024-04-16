package tukano.clients;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import tukano.api.java.Result;
import utils.Sleep;

import java.util.function.Supplier;

public class RestClient {
    protected static final int MAX_RETRIES = 10;
    protected static final int RETRY_SLEEP = 1000;

    protected <T> Result<T> reTry(Supplier<Result<T>> func) {
        for (int i = 0; i < MAX_RETRIES; i++)
            try {
                return func.get();
            } catch (ProcessingException x) {
                Sleep.ms(RETRY_SLEEP);
            } catch (Exception x) {
                x.printStackTrace();
                return Result.error(Result.ErrorCode.INTERNAL_ERROR);
            }
        return Result.error(Result.ErrorCode.TIMEOUT);
    }

    protected <T> Result<T> toJavaResult(Response r, Class<T> entityType) {
        try {
            var status = r.getStatusInfo().toEnum();
            if (status == Response.Status.OK && r.hasEntity())
                return Result.ok(r.readEntity(entityType));
            else if (status == Response.Status.NO_CONTENT) return Result.ok();

            return Result.error(RestUsersClient.getErrorCodeFrom(status.getStatusCode()));
        } finally {
            r.close();
        }
    }
}
