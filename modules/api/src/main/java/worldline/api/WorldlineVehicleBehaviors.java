package worldline.api;

/** Qualified vehicle boundaries kept outside the primary compatibility catalog. */
public final class WorldlineVehicleBehaviors {
    public static final WorldlineBehavior BOAT_CURRENT_PUSH = define("boat-current-push",
            "Boat on flowing water is displaced downstream");
    public static final WorldlineBehavior MINECART_DERAIL = define("minecart-derail",
            "Moving minecart leaves a rail end or unmatched corner");
    public static final WorldlineBehavior POWERED_RAIL_BRAKE = define("powered-rail-brake",
            "Unpowered powered-rail stops a moving minecart");
    public static final WorldlineBehavior FURNACE_CART_PUSH = define("furnace-cart-push",
            "Fueled furnace cart consumes coal and self-propels on rail");

    private WorldlineVehicleBehaviors() {}

    private static WorldlineBehavior define(String token, String subject) {
        return WorldlineBehavior.define(token, WorldlineFamily.VEHICLE, subject);
    }
}
