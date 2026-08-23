import java.util.List;
import java.util.Map;

/** Exercises strict receipt parsing, including escapes and malformed input. */
public final class MiniJsonTest {
    public static void main(String[] arguments) {
        Map<String, Object> root = MiniJson.object(
                "{\"text\":\"quote:\\\" line:\\n A:\\u0041\",\"count\":12,\"ok\":true,"
                        + "\"items\":[{\"head\":\"abc\"}],\"none\":null}");
        require("quote:\" line:\n A:A".equals(MiniJson.string(root, "text")), "escape drift");
        require(MiniJson.integer(root, "count") == 12, "integer drift");
        require(MiniJson.bool(root, "ok"), "boolean drift");
        List<Object> items = MiniJson.array(root, "items");
        require("abc".equals(MiniJson.string(MiniJson.asObject(items.get(0), "item"), "head")),
                "nested object drift");
        rejects("{\"a\":1,\"a\":2}");
        rejects("{\"a\":null,\"a\":null}");
        rejects("{\"a\":1} trailing");
        rejects("{\"a\":\"\\q\"}");
        rejects("[1,]");
        System.out.println("  strict JSON parser self-test: passed");
    }

    private static void rejects(String input) {
        try { MiniJson.object(input); throw new IllegalStateException("invalid JSON was accepted"); }
        catch (IllegalArgumentException expected) { }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
