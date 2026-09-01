package worldline.extension;

/** Performs one named mod-owned operation through neutral boundaries. */
public interface ExtensionAction {
    void perform(ExtensionContext context) throws Exception;
}
