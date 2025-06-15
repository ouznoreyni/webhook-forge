package sn.noreyni.common.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import sn.noreyni.common.response.ApiResponse;

@Provider
@Slf4j
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        //log.error("Exception occurred: ", exception);

        if (exception instanceof ApiException apiException) {
            return Response
                    .status(apiException.getStatusCode())
                    .entity(ApiResponse.error(apiException.getMessage()))
                    .build();
        }
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Internal server error"))
                .build();
    }
}