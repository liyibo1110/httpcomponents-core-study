package com.github.liyibo1110.hc.core5.net;

import com.github.liyibo1110.hc.core5.util.Args;

/**
 * port相关工具类
 * @author liyibo
 * @date 2026-04-03 16:03
 */
public final class Ports {

    private Ports() {}

    /** 默认端口号（未设置端口） */
    public final static int SCHEME_DEFAULT = -1;

    /** 最小端口号 */
    public final static int MIN_VALUE = 0;

    /** 最大端口号 */
    public final static int MAX_VALUE = 65535;

    /**
     * 检查给定的端口号是否在有效数值内（-1也算有效）。
     */
    public static int checkWithDefault(final int port) {
        return Args.checkRange(port, SCHEME_DEFAULT, MAX_VALUE, "Port number(Use -1 to specify the scheme default port)");
    }

    /**
     * 检查给定的端口号是否在有效数值内。
     */
    public static int check(final int port) {
        return Args.checkRange(port, MIN_VALUE, MAX_VALUE, "Port number");
    }
}
