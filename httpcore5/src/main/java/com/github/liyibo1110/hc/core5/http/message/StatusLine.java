package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.HttpResponse;
import com.github.liyibo1110.hc.core5.http.HttpVersion;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.Serializable;

/**
 * response响应首行
 * @author liyibo
 * @date 2026-04-06 13:14
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class StatusLine implements Serializable {
    private static final long serialVersionUID = -2443303766890459269L;

    private final ProtocolVersion protoVersion;

    private final int statusCode;

    private final StatusClass statusClass;

    private final String reasonPhrase;

    public StatusLine(final HttpResponse response) {
        super();
        Args.notNull(response, "Response");
        this.protoVersion = response.getVersion() != null ? response.getVersion() : HttpVersion.HTTP_1_1;
        this.statusCode = response.getCode();
        this.statusClass = StatusClass.from(this.statusCode);
        this.reasonPhrase = response.getReasonPhrase();
    }

    public StatusLine(final ProtocolVersion version, final int statusCode, final String reasonPhrase) {
        super();
        this.statusCode = Args.notNegative(statusCode, "Status code");
        this.statusClass = StatusClass.from(this.statusCode);
        this.protoVersion = version != null ? version : HttpVersion.HTTP_1_1;
        this.reasonPhrase = reasonPhrase;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public StatusClass getStatusClass() {
        return this.statusClass;
    }

    public boolean isInformational() {
        return getStatusClass() == StatusClass.INFORMATIONAL;
    }

    public boolean isSuccessful() {
        return getStatusClass() == StatusClass.SUCCESSFUL;
    }

    public boolean isRedirection() {
        return getStatusClass() == StatusClass.REDIRECTION;
    }

    public boolean isClientError() {
        return getStatusClass() == StatusClass.CLIENT_ERROR;
    }

    public boolean isServerError() {
        return getStatusClass() == StatusClass.SERVER_ERROR;
    }

    public boolean isError() {
        return isClientError() || isServerError();
    }

    public ProtocolVersion getProtocolVersion() {
        return this.protoVersion;
    }

    public String getReasonPhrase() {
        return this.reasonPhrase;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append(this.protoVersion).append(" ").append(this.statusCode).append(" ");
        if (this.reasonPhrase != null)
            buf.append(this.reasonPhrase);
        return buf.toString();
    }

    /**
     * HTTP状态码的标准类别，以及用于非标准代码的“其他”类别
     */
    public enum StatusClass {
        INFORMATIONAL,  // 1xx
        SUCCESSFUL, // 2xx
        REDIRECTION,    // 3xx
        CLIENT_ERROR,   // 4xx
        SERVER_ERROR,   // 5xx
        OTHER;  // non-standard status codes

        /**
         * statusCode -> StatusClass
         */
        public static StatusClass from(final int statusCode) {
            final StatusClass statusClass;

            switch (statusCode / 100) {
                case 1:
                    statusClass = INFORMATIONAL;
                    break;
                case 2:
                    statusClass = SUCCESSFUL;
                    break;
                case 3:
                    statusClass = REDIRECTION;
                    break;
                case 4:
                    statusClass = CLIENT_ERROR;
                    break;
                case 5:
                    statusClass = SERVER_ERROR;
                    break;
                default:
                    statusClass = OTHER;
                    break;
            }

            return statusClass;
        }
    }
}
