package com.github.liyibo1110.hc.core5.net;

/**
 * 通过名称（通常是完全合格的域名）和端口标识的端点。
 * @author liyibo
 * @date 2026-04-03 16:00
 */
public interface NamedEndpoint {

    /**
     * 返回host name（IP或DNS name）。
     */
    String getHostName();

    /**
     * 返回主机端口，-1代表未设置。
     */
    int getPort();
}
