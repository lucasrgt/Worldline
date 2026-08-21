package worldline.itemref.runtime.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldline.itemref.ItemReferenceCarrier;
import worldline.itemref.LogicalItemReference;
import worldline.itemref.runtime.ItemRefs;

/** Controlled ItemStack identity; absent references retain vanilla behavior. */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemReferenceCarrier {
    @Unique private static final String WORLDLINE_KEY = "WorldlineLogicalItemRef";
    @Unique private LogicalItemReference worldline$reference;

    @Override
    public LogicalItemReference worldline$getLogicalItemReference() {
        return worldline$reference;
    }

    @Override
    public void worldline$setLogicalItemReference(LogicalItemReference reference) {
        worldline$reference = reference;
    }

    @Inject(method = "readNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("TAIL"))
    private void readReference(NbtCompound nbt, CallbackInfo callback) {
        worldline$reference = nbt.contains(WORLDLINE_KEY)
                ? LogicalItemReference.parse(nbt.getString(WORLDLINE_KEY)) : null;
    }

    @Inject(method = "writeNbt(Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/nbt/NbtCompound;", at = @At("TAIL"))
    private void writeReference(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> callback) {
        if (worldline$reference != null) nbt.putString(WORLDLINE_KEY, worldline$reference.canonical());
    }

    @Inject(method = "copy()Lnet/minecraft/item/ItemStack;", at = @At("RETURN"))
    private void copyReference(CallbackInfoReturnable<ItemStack> callback) {
        ItemRefs.set(callback.getReturnValue(), worldline$reference);
    }

    @Inject(method = "split(I)Lnet/minecraft/item/ItemStack;", at = @At("RETURN"))
    private void splitReference(int amount, CallbackInfoReturnable<ItemStack> callback) {
        ItemRefs.set(callback.getReturnValue(), worldline$reference);
    }

    @Inject(method = "isItemEqual(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void isolateIdentity(ItemStack other, CallbackInfoReturnable<Boolean> callback) {
        rejectDifferentReference(other, callback);
    }

    @Inject(method = "equals2(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void isolateStackEquality(ItemStack other, CallbackInfoReturnable<Boolean> callback) {
        rejectDifferentReference(other, callback);
    }

    @Inject(method = "equals(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void isolatePublicEquality(ItemStack other, CallbackInfoReturnable<Boolean> callback) {
        rejectDifferentReference(other, callback);
    }

    @Unique
    private void rejectDifferentReference(ItemStack other, CallbackInfoReturnable<Boolean> callback) {
        LogicalItemReference that = ItemRefs.get(other);
        if (worldline$reference == null ? that != null : !worldline$reference.equals(that)) {
            callback.setReturnValue(false);
        }
    }
}
