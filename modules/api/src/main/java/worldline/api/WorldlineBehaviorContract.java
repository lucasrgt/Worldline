package worldline.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/** Composable TestKit metadata required of a behavioral milestone. */
public final class WorldlineBehaviorContract {
    public static final String EQUATABLE = "equatable";
    private static final Pattern TOKEN = Pattern.compile("[a-z][a-z0-9-]{0,62}");
    private static final Pattern BINDING = Pattern.compile(
            "[a-z][A-Za-z0-9_.]*[A-Z][A-Za-z0-9_]*#[a-z][A-Za-z0-9_]*");
    private final WorldlineBehavior behavior;
    private final String fixture;
    private final List<String> actions;
    private final List<String> observations;
    private final String binding;

    private WorldlineBehaviorContract(WorldlineBehavior behavior, String fixture,
            List<String> actions, List<String> observations, String binding) {
        if (behavior == null) throw new NullPointerException("behavior");
        this.behavior = behavior;
        this.fixture = token(fixture, "fixture");
        this.actions = tokens(actions, "actions");
        this.observations = tokens(observations, "observations");
        if (binding == null || !BINDING.matcher(binding).matches()) {
            throw new IllegalArgumentException("binding");
        }
        this.binding = binding;
    }

    public static WorldlineBehaviorContract from(Properties properties) {
        if (properties == null) throw new NullPointerException("properties");
        String evidence = required(properties, "testkit.evidence");
        if (!EQUATABLE.equals(evidence)) throw new IllegalArgumentException("testkit.evidence");
        return new WorldlineBehaviorContract(
                WorldlineBehavior.require(required(properties, "behavior")),
                required(properties, "testkit.fixture"),
                split(required(properties, "testkit.actions")),
                split(required(properties, "testkit.observations")),
                required(properties, "testkit.binding"));
    }

    public WorldlineBehavior behavior() { return behavior; }
    public String fixture() { return fixture; }
    public List<String> actions() { return actions; }
    public List<String> observations() { return observations; }
    public String binding() { return binding; }
    public String evidence() { return EQUATABLE; }

    public String canonical() {
        return behavior.atlasId() + "|fixture=" + fixture + "|actions=" + join(actions)
                + "|observations=" + join(observations) + "|binding=" + binding
                + "|evidence=" + EQUATABLE;
    }

    private static List<String> split(String value) {
        List<String> result = new ArrayList<String>();
        for (String item : value.split(",", -1)) result.add(item.trim());
        return result;
    }

    private static List<String> tokens(List<String> values, String label) {
        if (values == null || values.isEmpty()) throw new IllegalArgumentException(label);
        List<String> copy = new ArrayList<String>();
        for (String value : values) {
            String token = token(value, label);
            if (copy.contains(token)) throw new IllegalArgumentException("duplicate " + label);
            copy.add(token);
        }
        return Collections.unmodifiableList(copy);
    }

    private static String token(String value, String label) {
        if (value == null || !TOKEN.matcher(value).matches()) throw new IllegalArgumentException(label);
        return value;
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("missing " + key);
        return value.trim();
    }

    private static String join(List<String> values) {
        StringBuilder text = new StringBuilder();
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) text.append(',');
            text.append(values.get(index));
        }
        return text.toString();
    }
}
