package tukano.clients.rest;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import tukano.api.java.Result;
import utils.Sleep;

import java.util.function.Supplier;

public class RestClient {
    protected static final int MAX_RETRIES = 1;
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

    public static Result.ErrorCode getErrorCodeFrom(int status) {
        return switch (status) {
            case 200, 209 -> Result.ErrorCode.OK;
            case 409 -> Result.ErrorCode.CONFLICT;
            case 403 -> Result.ErrorCode.FORBIDDEN;
            case 404 -> Result.ErrorCode.NOT_FOUND;
            case 400 -> Result.ErrorCode.BAD_REQUEST;
            case 500 -> Result.ErrorCode.INTERNAL_ERROR;
            case 501 -> Result.ErrorCode.NOT_IMPLEMENTED;
            default -> Result.ErrorCode.INTERNAL_ERROR;
        };
    }
}
