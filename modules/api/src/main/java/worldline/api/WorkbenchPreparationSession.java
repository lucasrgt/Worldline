package worldline.api;

/** Typed workbench read extended with one bounded three-wide matrix preparation. */
public interface WorkbenchPreparationSession extends WorkbenchSession {
    RemoteWorkbenchPreparation prepareWorkbenchSlabs(int personalSlot);
}
