package worldline.b173server;

import java.lang.reflect.Array;
import java.util.List;
import worldline.testapi.RandomOutcomeObservation;

/** Native water, weather, angler and hook; controlled inputs never assign the catch counter. */
final class B173FishingScene implements AutoCloseable {
    final B173NativeServerSymbols names;
    final Object world, player;
    private final B173NativePlayerFactory players;
    private Object hook;
    B173FishingScene(B173NativeServerSymbols names, int depth, boolean rain) throws Exception {
        this.names = names; world = new B173NativeMemoryWorld(names).create();
        for (int x = 5; x <= 11; x++) for (int z = 5; z <= 11; z++)
            for (int y = 65; y <= 70; y++) names.call(world, "World", "setBlockAndMetadata",
                    "(IIIII)Z", x, y, z, y > 70 - depth ? 9 : 1, 0);
        Object climate = names.type("WorldChunkManagerHell").getConstructor(names.type("BiomeGenBase"),
                double.class, double.class).newInstance(names.constant("BiomeGenBase", "plains"), 0.8D, 0.8D);
        names.set(names.get(world, "worldProvider"), "worldChunkMgr", climate);
        names.set(world, "field_27078_C", rain ? 1F : 0F);
        names.set(world, "field_27079_B", rain ? 1F : 0F);
        boolean exposed = (Boolean) names.call(world, "World", "canLightningStrikeAt", "(III)Z", 8, 71, 8);
        B173NativeEntityScene.require(exposed == rain, "native weather precondition differs");
        players = new B173NativePlayerFactory(names); player = players.create(world);
        names.call(player, "Entity", "setPosition", "(DDD)V", 8.5D, 71D, 10.5D);
        Object inventory = names.getAs(player, "EntityPlayer", "inventory");
        Object slots = names.get(inventory, "mainInventory");
        Array.set(slots, 0, names.type("ItemStack").getConstructor(int.class, int.class, int.class)
                .newInstance(346, 1, 0));
        names.set(inventory, "currentItem", 0);
    }
    void fresh() throws Exception {
        entities().clear();
        hook = names.type("EntityFish").getConstructor(names.type("World")).newInstance(world);
        names.set(hook, "angler", player); names.setAs(player, "EntityPlayer", "fishEntity", hook);
        anchor();
    }
    private void anchor() throws Exception {
        names.call(hook, "Entity", "setPosition", "(DDD)V", 8.5D, 70.5D, 8.5D);
        for (String field : new String[] {"motionX", "motionY", "motionZ"}) names.set(hook, field, 0D);
    }
    RandomOutcomeObservation tick(String key, int[] bounds, int[] draws, boolean reel) throws Exception {
        anchor(); // Each stimulus starts with the same submerged pose, preserving the native counter.
        B173FiniteDraws random = new B173FiniteDraws(bounds, draws); names.set(hook, "rand", random);
        names.call(hook, "Entity", "onUpdate", "()V");
        return observe(key, random.trace(), reel);
    }
    RandomOutcomeObservation observe(String key, String draws, boolean reel) throws Exception {
        int counter = (Integer) names.get(hook, "ticksCatchable");
        int result = reel ? (Integer) names.call(hook, "EntityFish", "catchFish", "()I") : 0;
        int item = 0, count = 0;
        for (Object entity : entities()) if (names.type("EntityItem").isInstance(entity)) {
            Object stack = names.get(entity, "item");
            int id = (Integer) names.get(stack, "itemID");
            if (item != 0 && item != id) throw new IllegalStateException("mixed native fishing loot");
            item = id; count += (Integer) names.get(stack, "stackSize");
        }
        return new RandomOutcomeObservation(key, draws, counter, result, item, count,
                (Boolean) names.get(hook, "isDead"));
    }
    private List<?> entities() throws Exception { return (List<?>) names.get(world, "loadedEntityList"); }
    @Override public void close() throws java.io.IOException { players.close(); }
}
