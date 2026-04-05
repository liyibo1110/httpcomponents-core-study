package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.util.Tokenizer;

/**
 * 表示解析操作的上下文：
 * 1、解析操作预计开始的当前位置。
 * 2、限定解析操作作用域的边界。
 * @author liyibo
 * @date 2026-04-05 14:31
 */
public class ParserCursor extends Tokenizer.Cursor {
    public ParserCursor(final int lowerBound, final int upperBound) {
        super(lowerBound, upperBound);
    }
}
