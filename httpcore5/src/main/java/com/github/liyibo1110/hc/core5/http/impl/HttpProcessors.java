package com.github.liyibo1110.hc.core5.http.impl;

import com.github.liyibo1110.hc.core5.http.protocol.HttpProcessor;
import com.github.liyibo1110.hc.core5.http.protocol.HttpProcessorBuilder;
import com.github.liyibo1110.hc.core5.http.protocol.RequestConnControl;
import com.github.liyibo1110.hc.core5.http.protocol.RequestContent;
import com.github.liyibo1110.hc.core5.http.protocol.RequestExpectContinue;
import com.github.liyibo1110.hc.core5.http.protocol.RequestTargetHost;
import com.github.liyibo1110.hc.core5.http.protocol.RequestUserAgent;
import com.github.liyibo1110.hc.core5.http.protocol.RequestValidateHost;
import com.github.liyibo1110.hc.core5.http.protocol.ResponseConnControl;
import com.github.liyibo1110.hc.core5.http.protocol.ResponseContent;
import com.github.liyibo1110.hc.core5.http.protocol.ResponseDate;
import com.github.liyibo1110.hc.core5.http.protocol.ResponseServer;
import com.github.liyibo1110.hc.core5.util.TextUtils;
import com.github.liyibo1110.hc.core5.util.VersionInfo;

/**
 * 生成DefaultHttpProcessor对象的工厂类。
 * @author liyibo
 * @date 2026-04-09 13:17
 */
public class HttpProcessors {

    private final static String SOFTWARE = "Apache-HttpCore";

    /**
     * 创建默认版本的HttpProcessorBuilder对象（server端）。
     */
    public static HttpProcessorBuilder customServer(final String serverInfo) {
        return HttpProcessorBuilder.create()
                .addAll(new ResponseDate(),
                        new ResponseServer(!TextUtils.isBlank(serverInfo)
                                ? serverInfo
                                : VersionInfo.getSoftwareInfo(SOFTWARE, "org.apache.hc.core5", HttpProcessors.class)),
                        new ResponseContent(),
                        new ResponseConnControl())
                .addAll(new RequestValidateHost());
    }

    /**
     * 创建DefaultHttpProcessor对象（server端）。
     */
    public static HttpProcessor server(final String serverInfo) {
        return customServer(serverInfo).build();
    }

    public static HttpProcessor server() {
        return customServer(null).build();
    }

    /**
     * 创建默认版本的HttpProcessorBuilder对象（client端）。
     */
    public static HttpProcessorBuilder customClient(final String agentInfo) {
        return HttpProcessorBuilder.create()
                .addAll(RequestContent.INSTANCE,
                        RequestTargetHost.INSTANCE,
                        RequestConnControl.INSTANCE,
                        new RequestUserAgent(!TextUtils.isBlank(agentInfo)
                                ? agentInfo
                                : VersionInfo.getSoftwareInfo(SOFTWARE, "org.apache.hc.core5", HttpProcessors.class)),
                        RequestExpectContinue.INSTANCE);
    }

    /**
     * 创建DefaultHttpProcessor对象（client端）。
     */
    public static HttpProcessor client(final String agentInfo) {
        return customClient(agentInfo).build();
    }

    public static HttpProcessor client() {
        return customClient(null).build();
    }
}
