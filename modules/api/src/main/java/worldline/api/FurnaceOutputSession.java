package worldline.api;

/** Furnace smelting extended with one exact output-to-personal extraction. */
public interface FurnaceOutputSession extends FurnaceSession {
    RemoteFurnaceExtraction takeFurnaceOutput(int personalSlot);
}
