package worldline.itemref;

/** Controlled-runtime bridge implemented by extended physical item stacks. */
public interface ItemReferenceCarrier {
    LogicalItemReference worldline$getLogicalItemReference();

    void worldline$setLogicalItemReference(LogicalItemReference reference);
}
