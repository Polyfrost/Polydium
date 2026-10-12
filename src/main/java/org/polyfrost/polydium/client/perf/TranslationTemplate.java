package org.polyfrost.polydium.client.perf;

//? if >= 1.21.1 {
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TranslationTemplate {
    public static final Object PERCENT = new Object();

    private static final int MAX_ENTRIES = 4096;
    private static final Object[] INVALID = new Object[0];
    private static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");
    private static final ConcurrentHashMap<String, Object[]> CACHE = new ConcurrentHashMap<>();

    private TranslationTemplate() {
    }

    public static Object[] parts(String template, Object percent, Function<String, Object> literal) {
        Object[] parts = CACHE.get(template);
        if (parts == null) {
            parts = parse(template);
            if (parts == null) {
                parts = INVALID;
            } else {
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i] == PERCENT) {
                        parts[i] = percent;
                    } else if (parts[i] instanceof String text) {
                        parts[i] = literal.apply(text);
                    }
                }
            }
            if (CACHE.size() >= MAX_ENTRIES) {
                CACHE.clear();
            }
            CACHE.put(template, parts);
        }
        return parts == INVALID ? null : parts;
    }

    public static Object[] parse(String template) {
        List<Object> parts = new ArrayList<>();
        Matcher matcher = FORMAT_PATTERN.matcher(template);
        int nextArgument = 0;
        int current = 0;

        while (matcher.find(current)) {
            int start = matcher.start();
            int end = matcher.end();
            if (start > current) {
                String prefix = template.substring(current, start);
                if (prefix.indexOf('%') != -1) {
                    return null;
                }
                parts.add(prefix);
            }

            String type = matcher.group(2);
            if ("%".equals(type) && end - start == 2) {
                parts.add(PERCENT);
            } else if ("s".equals(type)) {
                String explicitIndex = matcher.group(1);
                if (explicitIndex == null) {
                    parts.add(nextArgument++);
                } else {
                    try {
                        parts.add(Integer.parseInt(explicitIndex) - 1);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            } else {
                return null;
            }
            current = end;
        }

        if (current < template.length()) {
            String tail = template.substring(current);
            if (tail.indexOf('%') != -1) {
                return null;
            }
            parts.add(tail);
        }
        return parts.toArray();
    }
}
//?}
