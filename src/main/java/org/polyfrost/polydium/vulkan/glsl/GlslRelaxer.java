package org.polyfrost.polydium.vulkan.glsl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GlslRelaxer {
    // A backslash before the newline continues a // comment onto the next line.
    private static final Pattern COMMENT = Pattern.compile("/\\*[\\s\\S]*?\\*/|//(?:\\\\\\r?\\n|[^\\n])*");
    private static final Pattern VERSION = Pattern.compile("^[ \\t]*#[ \\t]*version[ \\t]+(\\d+)(?:[ \\t]+(\\w+))?[ \\t]*$", Pattern.MULTILINE);
    private static final Pattern OPAQUE_TYPE = Pattern.compile("(?:[iu]?sampler|[iu]?image|[iu]?texture|atomic_uint|subpassInput)\\w*");
    private static final Pattern LOOSE_UNIFORM = Pattern.compile(
            "(?:\\blayout\\s*\\([^)]*\\)\\s*)?\\buniform\\s+(?:(?:lowp|mediump|highp)\\s+)?(?!" + OPAQUE_TYPE.pattern() + "\\b)(\\w+)\\s+([^;{#]+);"
    );
    private static final Pattern OBJECT_MACRO = Pattern.compile("^[ \\t]*#[ \\t]*define[ \\t]+(\\w+)[ \\t]+(.*?)[ \\t]*$", Pattern.MULTILINE);
    private static final Pattern WORD = Pattern.compile("\\w+");
    private static final Pattern INT_LITERAL = Pattern.compile("(0[xX][0-9a-fA-F]+|\\d+)[uU]?");
    private static final Pattern DECLARATOR = Pattern.compile("\\s*(\\w+)\\s*(?:\\[\\s*([^]]*?)\\s*])?\\s*(?:=\\s*(.*?))?\\s*", Pattern.DOTALL);
    private static final Pattern BUILTIN_TYPE = Pattern.compile("float|double|u?int|bool|[biud]?vec[234]|d?mat[234](?:x[234])?");
    private static final Pattern BLOCK_LAYOUT = Pattern.compile("\\blayout\\s*\\(([^)#]*)\\)(?=\\s*(?:uniform|buffer)\\b)");
    private static final Pattern PACKING = Pattern.compile("(?<=^|,)(\\s*)(?:shared|packed)(?=\\s*(?:,|$))");
    private static final Pattern SPLICE = Pattern.compile("\\\\[ \\t]*\\r?\\n");
    private static final Pattern CONDITIONAL = Pattern.compile("^\\s*#\\s*(?:if|ifdef|ifndef|elif)\\b");
    private static final String MARKER = "POLYDIUM_DEFINED_";
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
    private static final String ALIASES;
    private static final Map<String, String> ALIAS_BODIES = new HashMap<>();
    private static final Pattern ALIAS_MACRO;
    private static final Pattern ALIAS_UNDEF;
    private static final Pattern ALIAS_FUNCTION;
    private static final Pattern ALIAS_NAME;

    static {
        StringBuilder defines = new StringBuilder();
        StringBuilder names = new StringBuilder();
        for (String[] alias : LEGACY_BUILTINS) {
            defines.append("#define ").append(alias[0]).append(alias[1]).append('\n');
            names.append(names.length() == 0 ? "" : "|").append(alias[0]);
            ALIAS_BODIES.put(alias[0], alias[1]);
        }
        ALIASES = defines.toString();
        ALIAS_MACRO = Pattern.compile("#[ \\t]*define[ \\t]+(" + names + ")\\b");
        ALIAS_UNDEF = Pattern.compile("#[ \\t]*undef[ \\t]+(" + names + ")\\b");
        ALIAS_NAME = Pattern.compile("\\b(" + names + ")\\b");
        ALIAS_FUNCTION = Pattern.compile("\\b(?!return\\b|else\\b|do\\b)\\w+\\s+(" + names + ")\\s*\\(");
    }

    private GlslRelaxer() {
    }

    public static String relax(String source) {
        source = stripComments(source);
        Map<String, List<String>> macros = new HashMap<>();
        Matcher define = OBJECT_MACRO.matcher(SPLICE.matcher(source).replaceAll(""));
        while (define.find()) {
            macros.computeIfAbsent(define.group(1), k -> new ArrayList<>()).add(define.group(2));
        }

        source = relaxVersion(source);
        source = replaceOutsideDirectives(source, LOOSE_UNIFORM, m -> toGlobals(m.group(), m.group(1), m.group(2), macros));
        source = replaceOutsideDirectives(source, BLOCK_LAYOUT, m -> "layout(" + PACKING.matcher(m.group(1)).replaceAll("$1std140") + ")");
        return EXTENSION_REQUIRE.matcher(source).replaceAll("$1enable");
    }

    private static String replaceOutsideDirectives(final String source, final Pattern pattern, final Function<MatchResult, String> replacer) {
        return pattern.matcher(source).replaceAll(m -> {
            int lineStart = source.lastIndexOf('\n', m.start() - 1) + 1;
            while (lineStart > 0 && continues(source.substring(source.lastIndexOf('\n', lineStart - 2) + 1, lineStart - 1))) {
                lineStart = source.lastIndexOf('\n', lineStart - 2) + 1;
            }
            boolean directive = source.substring(lineStart, m.start()).trim().startsWith("#");
            return Matcher.quoteReplacement(directive ? m.group() : replacer.apply(m));
        });
    }

    private static boolean continues(final String line) {
        return line.stripTrailing().endsWith("\\");
    }

    // The aliases are always defined up front, so a directive that defines one of those names gets an #undef first
    // and a directive that undefines one restores the alias, matching GL where the name falls back to the builtin.
    // Conditionals test a marker instead of the name, so #ifdef only sees the shader's own definitions. A function
    // with one of those names is renamed, since texture2D and friends are type keywords in Vulkan GLSL.
    private static String undefAliases(final String body, final int firstLine, final int version) {
        String[] lines = body.split("\n", -1);
        List<String> out = new ArrayList<>();
        for (int i = 0, end; i < lines.length; i = end + 1) {
            end = i;
            while (end < lines.length - 1 && continues(lines[end])) {
                end++;
            }

            String logical = String.join("\n", Arrays.copyOfRange(lines, i, end + 1));
            String joined = SPLICE.matcher(logical).replaceAll("");
            List<String> before = new ArrayList<>();
            List<String> after = new ArrayList<>();
            if (joined.trim().startsWith("#")) {
                Matcher m = ALIAS_MACRO.matcher(joined);
                if (m.find()) {
                    before.add("#undef " + m.group(1));
                    after.add("#define " + MARKER + m.group(1));
                }
                m = ALIAS_UNDEF.matcher(joined);
                if (m.find()) {
                    after.add("#undef " + MARKER + m.group(1));
                    after.add("#define " + m.group(1) + ALIAS_BODIES.get(m.group(1)));
                }
                if (CONDITIONAL.matcher(joined).find()) {
                    logical = ALIAS_NAME.matcher(logical).replaceAll(MARKER + "$1");
                }
            } else {
                ALIAS_FUNCTION.matcher(joined).results().map(m -> m.group(1)).distinct().forEach(name -> {
                    before.add("#undef " + name);
                    before.add("#define " + name + " polydium_" + name);
                });
            }

            if (!before.isEmpty()) {
                out.addAll(before);
                out.add("#line " + lineDirective(firstLine + i, version));
            }
            out.add(logical);
            if (!after.isEmpty() && end < lines.length - 1) {
                out.addAll(after);
                out.add("#line " + lineDirective(firstLine + end + 1, version));
            }
        }

        return String.join("\n", out);
    }

    // Before GLSL 330, #line N numbers the following line N + 1.
    private static int lineDirective(final int nextLine, final int version) {
        return version >= 330 ? nextLine : nextLine - 1;
    }

    // The preprocessor sees a comment as one space, so a block comment's newlines move to the end of its line
    // instead of splitting a #define in two. Line count stays the same for everything after it.
    private static String stripComments(final String source) {
        Matcher m = COMMENT.matcher(source);
        StringBuilder out = new StringBuilder();
        int last = 0;
        int pending = 0;
        while (m.find()) {
            pending = appendCode(out, source.substring(last, m.start()), pending);
            String comment = m.group();
            if (comment.startsWith("/*")) {
                out.append(' ');
                pending += (int) comment.chars().filter(c -> c == '\n').count();
            } else {
                pending = appendCode(out, comment.replaceAll("[^\\n]", ""), pending);
            }
            last = m.end();
        }
        appendCode(out, source.substring(last), pending);
        return out.toString();
    }

    private static int appendCode(final StringBuilder out, final String code, final int pending) {
        int newline = code.indexOf('\n');
        if (pending == 0 || newline < 0) {
            out.append(code);
            return pending;
        }
        out.append(code, 0, newline + 1).append("\n".repeat(pending)).append(code, newline + 1, code.length());
        return 0;
    }

    private static String relaxVersion(final String source) {
        Matcher m = VERSION.matcher(source);
        if (!m.find()) {
            return "#version 150\n" + ALIASES + "#line 0\n" + undefAliases(source, 1, 150);
        }

        int version = Math.max(Integer.parseInt(m.group(1)), 150);
        String profile = m.group(2);
        String line = "#version " + version + (profile == null || profile.equals("compatibility") ? "" : " " + profile);
        int versionLine = (int) source.substring(0, m.start()).chars().filter(c -> c == '\n').count() + 1;
        return line + "\n" + ALIASES + "#line " + lineDirective(versionLine + 1, version)
                + undefAliases(source.substring(m.end()), versionLine, version);
    }

    private static String toGlobals(final String original, final String type, final String declarators, final Map<String, List<String>> macros) {
        List<String> types = expand(type, macros);
        if (types.stream().anyMatch(t -> WORD.matcher(t).results().anyMatch(w -> OPAQUE_TYPE.matcher(w.group()).matches()))) {
            return original;
        }

        StringBuilder out = new StringBuilder();
        for (String declarator : splitTopLevel(declarators)) {
            Matcher m = DECLARATOR.matcher(declarator);
            if (!m.matches()) {
                return original;
            }

            String name = m.group(1);
            String size = m.group(2);
            String value = m.group(3) != null ? m.group(3) : zero(type, types, size, macros);
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

    private static String zero(final String type, final List<String> types, final String size, final Map<String, List<String>> macros) {
        if (types.size() != 1 || !BUILTIN_TYPE.matcher(types.get(0)).matches()) {
            return null;
        }
        if (size == null) {
            return type + "(0)";
        }

        List<String> sizes = expand(size, macros);
        Matcher literal = INT_LITERAL.matcher(sizes.get(0));
        if (sizes.size() != 1 || !literal.matches()) {
            return null;
        }

        int count;
        try {
            count = Integer.decode(literal.group(1));
        } catch (NumberFormatException e) {
            return null;
        }
        return type + "[" + size + "](" + String.join(", ", Collections.nCopies(count, type + "(0)")) + ")";
    }

    private static List<String> expand(final String token, final Map<String, List<String>> macros) {
        List<String> out = new ArrayList<>();
        expand(token, macros, out, 0);
        return out;
    }

    private static void expand(final String token, final Map<String, List<String>> macros, final List<String> out, final int depth) {
        List<String> values = macros.get(token);
        if (values == null || depth > 8) {
            out.add(token);
            return;
        }
        for (String value : values) {
            if (WORD.matcher(value).matches()) {
                expand(value, macros, out, depth + 1);
            } else {
                List<String> words = new ArrayList<>();
                WORD.matcher(value).results().forEach(w -> expand(w.group(), macros, words, depth + 1));
                out.add(String.join(" ", words));
            }
        }
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
