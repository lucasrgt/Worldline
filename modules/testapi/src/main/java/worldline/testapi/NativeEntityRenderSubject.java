package worldline.testapi;

/** One concrete registry entity in a canonical native render scene. */
public final class NativeEntityRenderSubject {
    private final String subject, entityClass, layer;
    private final int legacyId;

    public NativeEntityRenderSubject(String subject, int legacyId, String entityClass, String layer) {
        if (subject == null || !subject.matches("b1\\.7\\.3:entity/[0-9]{3}")
                || legacyId < 1 || legacyId > 999 || legacyId == 48
                || !subject.endsWith(String.format("%03d", legacyId))) {
            throw new IllegalArgumentException("invalid concrete entity render subject");
        }
        if (entityClass == null || !entityClass.matches("Entity[A-Za-z0-9]+")
                || !("ARCHETYPE".equals(layer) || "SINGULAR".equals(layer))) {
            throw new IllegalArgumentException("invalid entity render identity");
        }
        this.subject = subject; this.legacyId = legacyId;
        this.entityClass = entityClass; this.layer = layer;
    }

    public String subject() { return subject; }
    public int legacyId() { return legacyId; }
    public String entityClass() { return entityClass; }
    public String layer() { return layer; }
    public String canonical() { return subject + '|' + entityClass + '|' + layer; }
}
