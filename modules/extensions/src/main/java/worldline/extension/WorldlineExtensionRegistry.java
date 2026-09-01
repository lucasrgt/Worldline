package worldline.extension;

/** Only surface through which an extension contributes executable contracts. */
public interface WorldlineExtensionRegistry {
    void subject(ExtensionSubject subject);
    void fixture(String id, ExtensionFixture fixture);
    void action(String id, ExtensionAction action);
    void observation(String id, ExtensionObservation observation);
    void oracle(String id, ExtensionOracle oracle);
    void contract(ExtensionContract contract);
    void adapter(ExtensionRuntimeAdapter adapter);
    void atlas(ExtensionAtlasPage page);
}
