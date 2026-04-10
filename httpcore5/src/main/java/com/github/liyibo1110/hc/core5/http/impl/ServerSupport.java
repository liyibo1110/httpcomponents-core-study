package com.github.liyibo1110.hc.core5.http.impl;

import com.github.liyibo1110.hc.core5.http.EntityDetails;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.HttpResponse;
import com.github.liyibo1110.hc.core5.http.HttpStatus;
import com.github.liyibo1110.hc.core5.http.MethodNotSupportedException;
import com.github.liyibo1110.hc.core5.http.MisdirectedRequestException;
import com.github.liyibo1110.hc.core5.http.NotImplementedException;
import com.github.liyibo1110.hc.core5.http.ProtocolException;
import com.github.liyibo1110.hc.core5.http.RequestHeaderFieldsTooLargeException;
import com.github.liyibo1110.hc.core5.http.UnsupportedHttpVersionException;

/**
 * server side相关辅助方法。
 * @author liyibo
 * @date 2026-04-09 13:07
 */
public final class ServerSupport {

    private ServerSupport() {}

    /**
     * 通过检查HttpResponse的状态码，来判断是否认定为响应异常（目前只有204和304，且entity不为空）。
     *
     */
    public static void validateResponse(final HttpResponse response, final EntityDetails responseEntityDetails)
            throws HttpException {
        final int status = response.getCode();
        switch (status) {
            case HttpStatus.SC_NO_CONTENT:
            case HttpStatus.SC_NOT_MODIFIED:
                if (responseEntityDetails != null)
                    throw new HttpException("Response " + status + " must not enclose an entity");
        }
    }

    /**
     * 根据给定的异常，返回对应的异常字面信息。
     */
    public static String toErrorMessage(final Exception ex) {
        final String message = ex.getMessage();
        return message != null ? message : ex.toString();
    }

    /**
     * 根据给定的异常，返回对应的响应状态码。
     */
    public static int toStatusCode(final Exception ex) {
        final int code;
        if (ex instanceof MethodNotSupportedException)
            code = HttpStatus.SC_NOT_IMPLEMENTED;
        else if (ex instanceof UnsupportedHttpVersionException)
            code = HttpStatus.SC_HTTP_VERSION_NOT_SUPPORTED;
        else if (ex instanceof NotImplementedException)
            code = HttpStatus.SC_NOT_IMPLEMENTED;
        else if (ex instanceof RequestHeaderFieldsTooLargeException)
            code = HttpStatus.SC_REQUEST_HEADER_FIELDS_TOO_LARGE;
        else if (ex instanceof MisdirectedRequestException)
            code = HttpStatus.SC_MISDIRECTED_REQUEST;
        else if (ex instanceof ProtocolException)
            code = HttpStatus.SC_BAD_REQUEST;
        else
            code = HttpStatus.SC_INTERNAL_SERVER_ERROR;

        return code;
    }
}
