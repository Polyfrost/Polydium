package org.polyfrost.polydium.vulkan.glsl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GlslRelaxer {
    private static final Pattern COMMENT = Pattern.compile("/\\*[\\s\\S]*?\\*/|//[^\\n]*");
    private static final Pattern VERSION = Pattern.compile("^[ \\t]*#[ \\t]*version[ \\t]+(\\d+)(?:[ \\t]+(\\w+))?[ \\t]*$", Pattern.MULTILINE);
    private static final Pattern LOOSE_UNIFORM = Pattern.compile(
            "(?:\\blayout\\s*\\([^)]*\\)\\s*)?\\buniform\\s+(?:(?:lowp|mediump|highp)\\s+)?(?!(?:[iu]?sampler|[iu]?image|[iu]?texture|atomic_uint|subpassInput)\\w*\\b)(\\w+)\\s+([^;{#]+);"
    );
    private static final Pattern DECLARATOR = Pattern.compile("\\s*(\\w+)\\s*(?:\\[\\s*([^]]*?)\\s*])?\\s*(?:=\\s*(.*?))?\\s*", Pattern.DOTALL);
    private static final Pattern BUILTIN_TYPE = Pattern.compile("float|double|u?int|bool|[biud]?vec[234]|d?mat[234](?:x[234])?");
    private static final Pattern BLOCK_LAYOUT = Pattern.compile("\\blayout\\s*\\(([^)#]*)\\)(?=\\s*(?:uniform|buffer)\\b)");
    private static final Pattern PACKING = Pattern.compile("\\b(?:shared|packed)\\b");
    private static final Pattern EXTENSION_REQUIRE = Pattern.compile("(#[ \\t]*extension[ \\t]+\\w+[ \\t]*:[ \\t]*)require\\b");
    private static final String[][] LEGACY_BUILTINS = {
            {"texture1D", " texture"},
            {"texture2D", " texture"},
            {"texture3D", " texture"},
            {"textureCube", " texture"},
            {"texture1DLod", " textureLod"},
            {"texture2DLod", " textureLod"},
            {"texture3DLod", " textureLod"},
            {"textureCubeLod", " textureLod"},
            {"texture2DProj", " textureProj"},
            {"texture2DProjLod", " textureProjLod"},
            {"shadow2D", "(s, c) vec4(texture(s, c))"},
            {"shadow2DProj", "(s, c) vec4(textureProj(s, c))"},
    };

    private GlslRelaxer() {
    }

    public static String relax(String source) {
        source = relaxVersion(stripComments(source));
        source = replaceOutsideDirectives(source, LOOSE_UNIFORM, m -> toGlobals(m.group(), m.group(1), m.group(2)));
        source = replaceOutsideDirectives(source, BLOCK_LAYOUT, m -> "layout(" + PACKING.matcher(m.group(1)).replaceAll("std140") + ")");
        return EXTENSION_REQUIRE.matcher(source).replaceAll("$1enable");
    }

    private static String replaceOutsideDirectives(final String source, final Pattern pattern, final Function<MatchResult, String> replacer) {
        return pattern.matcher(source).replaceAll(m -> {
            int lineStart = source.lastIndexOf('\n', m.start() - 1) + 1;
            boolean directive = source.substring(lineStart, m.start()).trim().startsWith("#");
            return Matcher.quoteReplacement(directive ? m.group() : replacer.apply(m));
        });
    }

    // Skips aliases the shader already defines as a macro or function, since redefining them fails to compile.
    private static String legacyBuiltins(final String source) {
        StringBuilder out = new StringBuilder();
        for (String[] alias : LEGACY_BUILTINS) {
            String name = alias[0];
            if (!Pattern.compile("#[ \\t]*define[ \\t]+" + name + "\\b|\\b(?!return\\b)\\w+\\s+" + name + "\\s*\\(").matcher(source).find()) {
                out.append("#define ").append(name).append(alias[1]).append('\n');
            }
        }
        return out.toString();
    }

    private static String stripComments(final String source) {
        Matcher m = COMMENT.matcher(source);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            String comment = m.group();
            String blank = comment.startsWith("/*") ? comment.replaceAll("[^\\n]", " ") : "";
            m.appendReplacement(out, Matcher.quoteReplacement(blank));
        }
        m.appendTail(out);
        return out.toString();
    }

    private static String relaxVersion(final String source) {
        Matcher m = VERSION.matcher(source);
        if (!m.find()) {
            return "#version 150\n" + legacyBuiltins(source) + "#line 0\n" + source;
        }

        int version = Math.max(Integer.parseInt(m.group(1)), 150);
        String profile = m.group(2);
        String line = "#version " + version + (profile == null || profile.equals("compatibility") ? "" : " " + profile);
        int versionLine = (int) source.substring(0, m.start()).chars().filter(c -> c == '\n').count() + 1;
        // Before GLSL 330, #line N numbers the following line N + 1.
        int nextLine = version >= 330 ? versionLine + 1 : versionLine;
        return source.substring(0, m.start()) + line + "\n" + legacyBuiltins(source) + "#line " + nextLine + source.substring(m.end());
    }

    private static String toGlobals(final String original, final String type, final String declarators) {
        StringBuilder out = new StringBuilder();
        for (String declarator : splitTopLevel(declarators)) {
            Matcher m = DECLARATOR.matcher(declarator);
            if (!m.matches()) {
                return original;
            }

            String name = m.group(1);
            String size = m.group(2);
            String value = m.group(3) != null ? m.group(3) : zero(type, size);
            out.append(type).append(' ').append(name);
            if (size != null) {
                out.append('[').append(size).append(']');
            }
            if (value != null) {
                out.append(" = ").append(value);
            }
            out.append(';');
        }

        return out.toString();
    }

    private static String zero(final String type, final String size) {
        if (!BUILTIN_TYPE.matcher(type).matches()) {
            return null;
        }
        if (size == null) {
            return type + "(0)";
        }
        if (!size.matches("\\d+")) {
            return null;
        }

        return type + "[" + size + "](" + String.join(", ", Collections.nCopies(Integer.parseInt(size), type + "(0)")) + ")";
    }

    private static List<String> splitTopLevel(final String s) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(' || c == '[') {
                depth++;
            } else if (c == ')' || c == ']') {
                depth--;
            } else if (c == ',' && depth == 0) {
                parts.add(s.substring(start, i));
                start = i + 1;
            }
        }

        parts.add(s.substring(start));
        return parts;
    }
}
