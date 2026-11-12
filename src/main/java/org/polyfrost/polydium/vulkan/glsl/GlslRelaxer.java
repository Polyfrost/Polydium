package org.polyfrost.polydium.vulkan.glsl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GlslRelaxer {
    private static final Pattern VERSION = Pattern.compile("^[ \\t]*#[ \\t]*version[ \\t]+(\\d+)(?:[ \\t]+(\\w+))?[ \\t]*$", Pattern.MULTILINE);
    private static final Pattern LOOSE_UNIFORM = Pattern.compile(
            "(?:\\blayout\\s*\\([^)]*\\)\\s*)?\\buniform\\s+(?:(?:lowp|mediump|highp)\\s+)?(?!(?:[iu]?sampler|[iu]?image|[iu]?texture|atomic_uint|subpassInput)\\w*\\b)(\\w+)\\s+([^;{]+);"
    );
    private static final Pattern DECLARATOR = Pattern.compile("\\s*(\\w+)\\s*(?:\\[\\s*([^]]*?)\\s*])?\\s*(?:=\\s*(.*?))?\\s*", Pattern.DOTALL);
    private static final Pattern BUILTIN_TYPE = Pattern.compile("float|double|u?int|bool|[biud]?vec[234]|d?mat[234](?:x[234])?");
    private static final Pattern BLOCK_PACKING = Pattern.compile("\\b(?:shared|packed)\\b(?=[^)]*\\)\\s*(?:uniform|buffer)\\b)");
    private static final Pattern EXTENSION_REQUIRE = Pattern.compile("(#[ \\t]*extension[ \\t]+\\w+[ \\t]*:[ \\t]*)require\\b");
    private static final String LEGACY_BUILTINS = String.join("\n",
            "#define texture1D texture",
            "#define texture2D texture",
            "#define texture3D texture",
            "#define textureCube texture",
            "#define texture1DLod textureLod",
            "#define texture2DLod textureLod",
            "#define texture3DLod textureLod",
            "#define textureCubeLod textureLod",
            "#define texture2DProj textureProj",
            "#define texture2DProjLod textureProjLod",
            "#define shadow2D(s, c) vec4(texture(s, c))",
            "#define shadow2DProj(s, c) vec4(textureProj(s, c))"
    );

    private GlslRelaxer() {
    }

    public static String relax(String source) {
        source = relaxVersion(source);
        source = LOOSE_UNIFORM.matcher(source).replaceAll(m -> Matcher.quoteReplacement(toGlobals(m.group(), m.group(1), m.group(2))));
        source = BLOCK_PACKING.matcher(source).replaceAll("std140");
        return EXTENSION_REQUIRE.matcher(source).replaceAll("$1enable");
    }

    private static String relaxVersion(final String source) {
        Matcher m = VERSION.matcher(source);
        if (!m.find()) {
            return "#version 150\n" + LEGACY_BUILTINS + "\n#line 0\n" + source;
        }

        int version = Math.max(Integer.parseInt(m.group(1)), 150);
        String profile = m.group(2);
        String line = "#version " + version + (profile == null || profile.equals("compatibility") ? "" : " " + profile);
        int versionLine = (int) source.substring(0, m.start()).chars().filter(c -> c == '\n').count() + 1;
        // Before GLSL 330, #line N numbers the following line N + 1.
        int nextLine = version >= 330 ? versionLine + 1 : versionLine;
        return source.substring(0, m.start()) + line + "\n" + LEGACY_BUILTINS + "\n#line " + nextLine + source.substring(m.end());
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
