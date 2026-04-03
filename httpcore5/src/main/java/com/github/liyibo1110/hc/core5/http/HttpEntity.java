package com.github.liyibo1110.hc.core5.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Supplier;

/**
 * 可通过HTTP消息发送或接收的实体，根据内容的来源，HttpCore中有三种不同的实体类型：
 * 1、streamed：内容来自流，或是在运行时动态生成的。特别是，此类别包括从连接接收的实体。流式实体通常不可重复。
 * 2、self-contained：内容位于内存中，或通过独立于连接或其他实体的途径获取。自包含型实体通常是可重复的。
 * 3、wrapping：内容来自另一个实体。
 *
 * 这种区分对于处理传入实体的连接管理至关重要。对于由应用程序创建且仅通过HTTP组件框架发送的实体，流式与自包含之间的区别意义不大。
 * 在这种情况下，建议将不可重复的实体视为流式实体，而将那些（无需付出巨大努力即可）重复的实体视为自包含实体。
 *
 * @author liyibo
 * @date 2026-04-03 10:13
 */
public interface HttpEntity extends EntityDetails, Closeable {

    /**
     * 表示该实体是否能够多次生成其数据。
     * 可重复实体的getContent()和writeTo(OutputStream)方法可以被调用多次，而非可重复实体的则不能。
     */
    boolean isRepeatable();

    /**
     * 返回该entity的内容流。可重复entity应在此方法每次调用时创建一个新的InputStream实例，因此可以被多次读取。
     * 不可重复entity应返回相同的InputStream实例，因此不能被读取超过一次。
     *
     * 如果该entity属于一个传入的HTTP消息，则对返回的InputStream调用InputStream.close()时，系统将尝试读取完整的entity内容以保持连接处于活动状态。
     * 若不希望如此（例如仅需内容的一小部分，而处理完整entity内容效率过低），则应仅关闭获取该entity的HTTP消息（如果支持）。
     *
     * 重要提示：请注意，所有entity实现都必须确保在调用InputStream.close()方法后，正确释放所有已分配的资源。
     */
    InputStream getContent() throws IOException, UnsupportedOperationException;

    /**
     * 将entity内容写入输出流。
     * 重要提示：请注意，所有entity实现都必须确保在该方法返回时，已正确释放所有已分配的资源。
     */
    void writeTo(OutputStream outStream) throws IOException;

    /**
     * 指示该entity是否依赖于底层流。直接从socket读取数据的流式entity应返回true。
     * 自包含entity应返回false。封装entity应将此调用委托给被封装的entity。
     */
    boolean isStreaming(); // don't expect an exception here

    /**
     * 返回消息尾部（位于消息正文之后发送的头部）的提供者。
     * 如果尾部不可用，则可能返回 null
     */
    Supplier<List<? extends Header>> getTrailers();
}
