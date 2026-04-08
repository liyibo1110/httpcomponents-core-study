package com.github.liyibo1110.hc.core5.http.protocol;

/**
 * @author liyibo
 * @date 2026-04-07 17:31
 */
public enum UriPatternType {
    REGEX,
    URI_PATTERN,
    URI_PATTERN_IN_ORDER;

    public static <T> LookupRegistry<T> newMatcher(final UriPatternType type) {
        if (type == null)
            return new UriPatternMatcher<>();
        switch (type) {
            case REGEX:
                return new UriRegexMatcher<>();
            case URI_PATTERN_IN_ORDER:
                return new UriPatternOrderedMatcher<>();
            case URI_PATTERN:
            default:
                return new UriPatternMatcher<>();
        }
    }
}
