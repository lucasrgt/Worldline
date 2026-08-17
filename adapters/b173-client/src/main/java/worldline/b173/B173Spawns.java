package worldline.b173;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.SpawnRule;

/** Host-to-entity rules for world-tick and thrown-item creation. */
final class B173Spawns {
    private B173Spawns() {}

    static List<SpawnRule> snapshot() {
        List<SpawnRule> rules = new ArrayList<SpawnRule>();
        animal(rules, "block:2");
        add(rules, "block:8", "minecraft:squid", 4);
        add(rules, "block:9", "minecraft:squid", 4);
        add(rules, "minecraft:slime", "minecraft:slime", 4);
        add(rules, "block:52", "minecraft:zombie", 4);
        add(rules, "block:52", "minecraft:skeleton", 4);
        add(rules, "block:52", "minecraft:creeper", 4);
        add(rules, "block:52", "minecraft:spider", 4);
        add(rules, "block:12", "minecraft:falling-block", 16);
        add(rules, "block:13", "minecraft:falling-block", 16);
        add(rules, "block:46", "minecraft:tnt", 4);
        add(rules, "block:87", "minecraft:ghast", 4);
        add(rules, "block:87", "minecraft:pig-zombie", 4);
        add(rules, "item:262", "minecraft:arrow", 8);
        add(rules, "item:332", "minecraft:snowball", 8);
        add(rules, "item:344", "minecraft:egg", 8);
        add(rules, "item:333", "minecraft:boat", 1);
        add(rules, "item:328", "minecraft:minecart", 1);
        add(rules, "item:321", "minecraft:painting", 1);
        add(rules, "minecraft:egg", "minecraft:chicken", 1);
        add(rules, "minecraft:ghast", "minecraft:fireball", 4);
        add(rules, "minecraft:skeleton", "minecraft:arrow", 8);
        return Collections.unmodifiableList(rules);
    }

    private static void animal(List<SpawnRule> rules, String host) {
        add(rules, host, "minecraft:pig", 4);
        add(rules, host, "minecraft:cow", 4);
        add(rules, host, "minecraft:sheep", 4);
        add(rules, host, "minecraft:chicken", 4);
        add(rules, host, "minecraft:wolf", 4);
    }

    private static void add(List<SpawnRule> rules, String host, String entity, int max) {
        rules.add(new SpawnRule(host, entity, max));
    }
}
