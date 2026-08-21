package worldline.api;

/** Prepared three-wide workbench extended with exact output take and storage. */
public interface WorkbenchOutputSession extends WorkbenchPreparationSession {
    RemoteWorkbenchOutput takeWorkbenchSlabs(int personalSlot);
}
