package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ConnectionReuseStrategy;
import com.github.liyibo1110.hc.core5.http.io.HttpServerRequestHandler;
import com.github.liyibo1110.hc.core5.http.protocol.HttpProcessor;

/**
 * HttpService是一个基于经典（阻塞）I/O模型的服务器端HTTP协议处理器。
 * HttpService依赖于HttpProcessor来为所有发出的消息生成必需的协议头，并对所有传入和传出的消息应用通用的、横切的消息转换，
 * 而各个HttpRequestHandler则负责实现应用程序特有的内容生成和处理。
 * HttpService使用HttpRequestMapper来为传入HTTP请求的特定请求 URI 映射匹配的请求处理程序。
 *
 * 首先要注意一个巨大的认知误区：这个core项目是包含server side组件的，这个HttpService就算一个，平时开发人员在项目中使用的基本都是client项目，
 * core项目既提供了client所需要的底层接口和实现，又提供了server端的相关接口和实现。
 *
 * HttpService可以看作是：一次HTTP连接请求，负责classic server主干流程的调度总控制，大概流程是：
 * 1、读入请求
 * 2、跑协议处理器
 * 3、找到业务处理器
 * 4、生成响应
 * 5、写回响应
 * 6、判断是否 keep-alive
 * @author liyibo
 * @date 2026-04-08 17:52
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class HttpService {

    /** 负责请求和响应的通用协议处理与横切处理 */
    private final HttpProcessor processor;

    /** 负责最终业务内容的生成 */
    private final HttpServerRequestHandler requestHandler;

    private final ConnectionReuseStrategy connReuseStrategy;

    private final Http1StreamListener streamListener;
}
