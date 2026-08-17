package worldline.b173;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import worldline.api.CauseDrop;
import worldline.api.ItemCensus;

/** Death-drop maxima for mapped living entities. */
final class B173Mobs {
    private B173Mobs() {}

    static List<CauseDrop> snapshot() {
        List<CauseDrop> drops = new ArrayList<CauseDrop>();
        drops.add(few("minecraft:zombie", Item.feather, 2));
        drops.add(CauseDrop.death("minecraft:skeleton",
                ItemCensus.of(Item.arrow.shiftedIndex, 2).plus(Item.bone.shiftedIndex, 2)));
        drops.add(few("minecraft:creeper", Item.gunpowder, 2));
        drops.add(few("minecraft:spider", Item.silk, 2));
        drops.add(few("minecraft:slime", Item.slimeBall, 2));
        drops.add(CauseDrop.death("minecraft:sheep", ItemCensus.of(Block.cloth.blockID, 1)));
        drops.add(CauseDrop.death("minecraft:pig",
                ItemCensus.of(Item.porkRaw.shiftedIndex, 2).plus(Item.porkCooked.shiftedIndex, 2)));
        drops.add(few("minecraft:cow", Item.leather, 2));
        drops.add(few("minecraft:chicken", Item.feather, 2));
        drops.add(few("minecraft:squid", Item.dyePowder, 3));
        drops.add(few("minecraft:pig-zombie", Item.porkCooked, 2));
        drops.add(few("minecraft:ghast", Item.gunpowder, 2));
        return Collections.unmodifiableList(drops);
    }

    private static CauseDrop few(String type, Item item, int max) {
        return CauseDrop.death(type, ItemCensus.of(item.shiftedIndex, max));
    }
}
