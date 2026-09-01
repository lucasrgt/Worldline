package worldline.extension;

/** Prepares one fresh extension contract attempt. */
public interface ExtensionFixture {
    void prepare(ExtensionContext context) throws Exception;
}
