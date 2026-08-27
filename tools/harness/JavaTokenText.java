import java.nio.charset.StandardCharsets;

/** Canonicalizes Java source into a comment- and whitespace-insensitive token stream. */
final class JavaTokenText {
    private static final String VERSION = "worldline-java-tokens-v1";
    private JavaTokenText() { }

    /**
     * One token per line: identifiers, verbatim literals, and adjacent punctuation runs.
     * Comments vanish and whitespace only separates tokens, so formatting-only edits keep
     * the stream stable while any character or adjacency change alters it.
     */
    static byte[] canonical(byte[] portable) {
        String text = new String(portable, StandardCharsets.UTF_8);
        StringBuilder output = new StringBuilder(VERSION).append('\n');
        int index = 0, length = text.length();
        while (index < length) {
            char current = text.charAt(index);
            if (Character.isWhitespace(current)) { index++; continue; }
            char next = index + 1 < length ? text.charAt(index + 1) : '\0';
            if (current == '/' && next == '/') { index = lineEnd(text, index); continue; }
            if (current == '/' && next == '*') { index = blockEnd(text, index); continue; }
            int end;
            if (current == '"' || current == '\'') end = literalEnd(text, index);
            else if (Character.isJavaIdentifierPart(current)) end = identifierEnd(text, index);
            else end = punctuationEnd(text, index);
            output.append(text, index, end).append('\n');
            index = end;
        }
        return output.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static int lineEnd(String text, int start) {
        int index = text.indexOf('\n', start);
        return index < 0 ? text.length() : index;
    }

    private static int blockEnd(String text, int start) {
        int index = text.indexOf("*/", start + 2);
        return index < 0 ? text.length() : index + 2;
    }

    /** Strings, chars, and text blocks stay verbatim; escapes never terminate a literal. */
    private static int literalEnd(String text, int start) {
        char quote = text.charAt(start);
        int length = text.length();
        boolean block = quote == '"' && start + 2 < length
                && text.charAt(start + 1) == '"' && text.charAt(start + 2) == '"';
        int index = start + (block ? 3 : 1);
        while (index < length) {
            char current = text.charAt(index);
            if (current == '\\') { index += 2; continue; }
            if (block) {
                if (current == '"' && index + 2 < length
                        && text.charAt(index + 1) == '"' && text.charAt(index + 2) == '"')
                    return index + 3;
                index++;
            } else {
                if (current == quote) return index + 1;
                if (current == '\n') return index;
                index++;
            }
        }
        return length;
    }

    private static int identifierEnd(String text, int start) {
        int index = start;
        while (index < text.length() && Character.isJavaIdentifierPart(text.charAt(index))) index++;
        return index;
    }

    /** Adjacent punctuation is one token, so spacing inside operator pairs stays visible. */
    private static int punctuationEnd(String text, int start) {
        int index = start, length = text.length();
        while (index < length) {
            char current = text.charAt(index);
            if (Character.isWhitespace(current) || Character.isJavaIdentifierPart(current)
                    || current == '"' || current == '\'') break;
            if (current == '/' && index > start && index + 1 < length
                    && (text.charAt(index + 1) == '/' || text.charAt(index + 1) == '*')) break;
            index++;
        }
        return Math.max(index, start + 1);
    }

    static void selfTest() {
        requireEqual("int a = 1; // note", "int a=1;", "comment and spacing insensitivity");
        requireEqual("/** doc */ final class A { }", "final\n\tclass  A { }",
                "javadoc and whitespace insensitivity");
        requireDiffer("class A { }", "class A {}", "punctuation adjacency stays visible");
        requireEqual("foo( a , b )", "foo(a,b)", "argument spacing insensitivity");
        requireDiffer("x = a-- - b;", "x = a - --b;", "operator adjacency");
        requireDiffer("String s = \"// keep\";", "String s = \"//keep\";", "string contents");
        requireDiffer("call(foo)", "call(fooo)", "identifier change");
        requireEqual("String s = \"a\\\"b\"; // x", "String s = \"a\\\"b\";", "escaped quote");
        requireEqual("char c = '\\''; // q", "char c = '\\'';", "escaped char literal");
        requireDiffer("s = \"\"\"\n a \"b\"\n \"\"\";", "s = \"\"\"\n a \"c\"\n \"\"\";",
                "text block contents");
        requireDiffer("if (a > b) { }", "if (a >= b) { }", "operator change");
        System.out.println("  java token text self-test: passed");
    }

    private static void requireEqual(String left, String right, String message) {
        require(stream(left).equals(stream(right)), "token streams must match: " + message);
    }

    private static void requireDiffer(String left, String right, String message) {
        require(!stream(left).equals(stream(right)), "token streams must differ: " + message);
    }

    private static String stream(String source) {
        return new String(canonical(source.getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
