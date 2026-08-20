package worldline.api;

import java.util.Objects;

/** Immutable furnace output paired with observed cook and burn progress. */
public final class RemoteFurnaceSmelt {
    private final RemoteContainerWindow window;
    private final RemoteItemStack output;
    private final int maximumCook, maximumBurn, totalBurn, completionBurn;
    public RemoteFurnaceSmelt(RemoteContainerWindow window, RemoteItemStack output,
            int maximumCook, int maximumBurn, int totalBurn, int completionBurn) {
        if (window == null || window.descriptor().kind() != RemoteWindowKind.FURNACE
                || output == null || !(output.equals(new RemoteItemStack(20, 1, 0))
                    || output.equals(new RemoteItemStack(265, 1, 0)) || output.equals(new RemoteItemStack(266, 1, 0))
                    || output.equals(new RemoteItemStack(320, 1, 0)))
                || !window.inventory().slot(0).empty() || !window.inventory().slot(1).empty()
                || window.inventory().slot(2).empty()
                || !window.inventory().slot(2).item().equals(output)
                || maximumCook != 199 || maximumBurn != 1600 || totalBurn != 1600
                || completionBurn != 1401)
            throw new IllegalArgumentException("invalid furnace smelt evidence");
        this.window = window; this.output = output; this.maximumCook = maximumCook;
        this.maximumBurn = maximumBurn; this.totalBurn = totalBurn; this.completionBurn = completionBurn;
    }
    public RemoteContainerWindow window() { return window; }
    public RemoteItemStack output() { return output; }
    public int maximumCook() { return maximumCook; }
    public int maximumBurn() { return maximumBurn; }
    public int totalBurn() { return totalBurn; }
    public int completionBurn() { return completionBurn; }
    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteFurnaceSmelt)) return false; RemoteFurnaceSmelt value = (RemoteFurnaceSmelt) other;
        return maximumCook == value.maximumCook && maximumBurn == value.maximumBurn
                && totalBurn == value.totalBurn && completionBurn == value.completionBurn
                && window.equals(value.window) && output.equals(value.output); }
    @Override public int hashCode() { return Objects.hash(window, output, maximumCook, maximumBurn, totalBurn, completionBurn); }
}
