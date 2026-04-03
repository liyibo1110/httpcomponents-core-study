package com.github.liyibo1110.hc.core5.http;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.Serializable;

/**
 * @author liyibo
 * @date 2026-04-02 17:02
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class ProtocolVersion implements Serializable {

    private static final long serialVersionUID = 8950662842175091068L;

    /** 协议name */
    private final String protocol;

    /** 最大版本号 */
    private final int major;

    /** 最小版本号 */
    private final int minor;

    public ProtocolVersion(final String protocol, final int major, final int minor) {
        this.protocol = Args.notNull(protocol, "Protocol name");
        this.major = Args.notNegative(major, "Protocol minor version");
        this.minor = Args.notNegative(minor, "Protocol minor version");
    }

    public final String getProtocol() {
        return protocol;
    }

    public final int getMajor() {
        return major;
    }

    public final int getMinor() {
        return minor;
    }

    @Override
    public final int hashCode() {
        return this.protocol.hashCode() ^ (this.major * 100000) ^ this.minor;
    }

    public final boolean equals(final int major, final int minor) {
        return this.major == major && this.minor == minor;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ProtocolVersion))
            return false;
        final ProtocolVersion that = (ProtocolVersion) obj;
        return (this.protocol.equals(that.protocol) &&
                (this.major == that.major) &&
                (this.minor == that.minor));
    }

    /**
     * 返回格式形如：HTTP/1.1
     */
    public String format() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(this.protocol);
        buffer.append('/');
        buffer.append(this.major);
        buffer.append('.');
        buffer.append(this.minor);
        return buffer.toString();
    }

    /**
     * 检查此协议是否可与另一个协议进行比较。仅协议名称相同的协议版本才能进行比较。
     */
    public boolean isComparable(final ProtocolVersion that) {
        return (that != null) && this.protocol.equals(that.protocol);
    }

    /**
     * 将此协议版本与另一个版本进行比较。仅可比较协议名称相同的协议版本。
     * 该方法未定义全序关系，而这对于Comparable接口是必需的。
     * 返回值：
     * 1、负数：该版本小于给定的参数版本。
     * 2、正数：该版本大于给定的参数版本。
     * 3、零：该版本等于给定的参数版本。
     */
    public int compareToVersion(final ProtocolVersion that) {
        Args.notNull(that, "Protocol version");
        Args.check(this.protocol.equals(that.protocol), "Versions for different protocols cannot be compared: %s %s", this, that);
        int delta = getMajor() - that.getMajor();
        if (delta == 0)
            delta = getMinor() - that.getMinor();
        return delta;
    }

    /**
     * 该版本是否大于或等于给定的参数版本。
     */
    public final boolean greaterEquals(final ProtocolVersion version) {
        return isComparable(version) && (compareToVersion(version) >= 0);
    }

    /**
     * 该版本是否小于或等于给定的参数版本。
     */
    public final boolean lessEquals(final ProtocolVersion version) {
        return isComparable(version) && (compareToVersion(version) <= 0);
    }

    @Override
    public String toString() {
        return format();
    }
}
