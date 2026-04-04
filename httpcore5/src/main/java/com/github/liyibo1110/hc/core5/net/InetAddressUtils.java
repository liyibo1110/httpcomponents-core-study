package com.github.liyibo1110.hc.core5.net;

import com.github.liyibo1110.hc.core5.util.Args;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * 与InetAddress相关的工具类
 * @author liyibo
 * @date 2026-04-03 15:55
 */
public final class InetAddressUtils {
    private InetAddressUtils() {}

    public static final byte IPV4 = 1;

    public static final byte IPV6 = 4;

    private static final String IPV4_BASIC_PATTERN_STRING =
            "(([1-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){1}" + // initial first field, 1-255
            "(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){2}" + // following 2 fields, 0-255 followed by .
            "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])"; // final field, 0-255

    private static final Pattern IPV4_PATTERN = Pattern.compile("^" + IPV4_BASIC_PATTERN_STRING + "$");

    private static final Pattern IPV4_MAPPED_IPV6_PATTERN = // TODO does not allow for redundant leading zeros
            Pattern.compile("^::[fF]{4}:" + IPV4_BASIC_PATTERN_STRING + "$");

    private static final Pattern IPV6_STD_PATTERN = Pattern.compile("^[0-9a-fA-F]{1,4}(:[0-9a-fA-F]{1,4}){7}$");

    private static final Pattern IPV6_HEX_COMPRESSED_PATTERN =
            Pattern.compile(
                    "^(([0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4}){0,5})?)" + // 0-6 hex fields
                            "::" +
                            "(([0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4}){0,5})?)$"); // 0-6 hex fields

    private static final char COLON_CHAR = ':';

    private static final int MAX_COLON_COUNT = 7;

    /**
     * 检查给定字符串是否为IPv4地址。
     */
    public static boolean isIPv4Address(final String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }

    public static boolean isIPv4MappedIPv64Address(final String input) {
        return IPV4_MAPPED_IPV6_PATTERN.matcher(input).matches();
    }

    static boolean hasValidIPv6ColonCount(final String input) {
        int colonCount = 0;
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == COLON_CHAR)
                colonCount++;
        }
        // IPv6 address must have at least 2 colons and not more than 7 (i.e. 8 fields)
        return colonCount >= 2 && colonCount <= MAX_COLON_COUNT;
    }

    /**
     * 检查给定字符串是否为IPv6地址（STD）。
     */
    public static boolean isIPv6StdAddress(final String input) {
        return hasValidIPv6ColonCount(input) && IPV6_STD_PATTERN.matcher(input).matches();
    }

    /**
     * 检查给定字符串是否为IPv6地址（十六进制）。
     */
    public static boolean isIPv6HexCompressedAddress(final String input) {
        return hasValidIPv6ColonCount(input) && IPV6_HEX_COMPRESSED_PATTERN.matcher(input).matches();
    }

    /**
     * 检查给定字符串是否为IPv6地址（STD或十六进制）。
     */
    public static boolean isIPv6Address(final String input) {
        return isIPv6StdAddress(input) || isIPv6HexCompressedAddress(input);
    }

    public static boolean isIPv6URLBracketedAddress(final String input) {
        return input.startsWith("[") && input.endsWith("]") && isIPv6Address(input.substring(1, input.length() - 1));
    }

    public static void formatAddress(final StringBuilder buffer, final SocketAddress socketAddress) {
        Args.notNull(buffer, "buffer");
        if (socketAddress instanceof InetSocketAddress) {
            final InetSocketAddress socketaddr = (InetSocketAddress) socketAddress;
            final InetAddress inetaddr = socketaddr.getAddress();
            if (inetaddr != null)
                buffer.append(inetaddr.getHostAddress()).append(':').append(socketaddr.getPort());
            else
                buffer.append(socketAddress);
        } else {
            buffer.append(socketAddress);
        }
    }

    /**
     * 返回本地主机的规范名称（完全合格域名）
     */
    public static String getCanonicalLocalHostName() {
        try {
            final InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getCanonicalHostName();
        } catch (final UnknownHostException ex) {
            return "localhost";
        }
    }
}
