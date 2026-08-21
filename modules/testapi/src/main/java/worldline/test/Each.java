package worldline.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Table-driven test collector returned by {@code each}. */
public final class Each<T> {
    private final List<T> values;
    Each(Iterable<T> source) {
        if (source == null) throw new NullPointerException("source");
        List<T> copy = new ArrayList<>(); for (T value : source) copy.add(value);
        values = Collections.unmodifiableList(copy);
    }

    public List<TestDefinition> test(String name, EachBody<T> body) {
        if (body == null) throw new NullPointerException("body");
        List<TestDefinition> result = new ArrayList<>(); int index = 0;
        for (T value : values) { final T item = value; final int row = index++;
            result.add(Worldline.test(name.replace("%#", Integer.toString(row))
                    .replace("%s", String.valueOf(item)), context -> body.run(context, item))); }
        return Collections.unmodifiableList(result);
    }

    public List<TestDefinition> it(String name, EachBody<T> body) { return test(name, body); }
}
