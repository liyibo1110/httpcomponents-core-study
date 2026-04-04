package com.github.liyibo1110.hc.core5.http;

import com.github.liyibo1110.hc.core5.util.Args;

/**
 * 由消息头和消息体组成的通用消息。
 * @author liyibo
 * @date 2026-04-03 14:22
 */
public final class Message<H extends MessageHeaders, B> {
    private final H head;
    private final B body;

    public Message(final H head, final B body) {
        this.head = Args.notNull(head, "Message head");
        this.body = body;
    }

    public H getHead() {
        return head;
    }

    public B getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "[" +
                "head=" + head +
                ", body=" + body +
                ']';
    }
}
