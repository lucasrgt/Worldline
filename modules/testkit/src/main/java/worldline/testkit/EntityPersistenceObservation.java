package worldline.testkit;

import java.util.Objects;

/** One native Entity-to-NBT-to-EntityList reconstruction observation. */
public final class EntityPersistenceObservation {
    private final String subject, registryName, runtimeType, stateSha256;
    private final boolean reconstructed, typeExact, commonStateExact, nbtExact;

    public EntityPersistenceObservation(String subject, String registryName, String runtimeType,
            boolean reconstructed, boolean typeExact, boolean commonStateExact,
            boolean nbtExact, String stateSha256) {
        if (subject == null || !subject.matches(
                "b[0-9]+\\.[0-9]+\\.[0-9]+:entity/[0-9]{3}"))
            throw new IllegalArgumentException("subject");
        if (registryName == null || !registryName.matches("[A-Za-z]+")
                || runtimeType == null || !runtimeType.matches("Entity[A-Za-z]+")
                || stateSha256 == null || !stateSha256.matches("[0-9a-f]{64}"))
            throw new IllegalArgumentException("native identity");
        this.subject = subject; this.registryName = registryName;
        this.runtimeType = runtimeType; this.reconstructed = reconstructed;
        this.typeExact = typeExact; this.commonStateExact = commonStateExact;
        this.nbtExact = nbtExact; this.stateSha256 = stateSha256;
    }

    public String subject() { return subject; }
    public String registryName() { return registryName; }
    public String runtimeType() { return runtimeType; }
    public boolean reconstructed() { return reconstructed; }
    public boolean typeExact() { return typeExact; }
    public boolean commonStateExact() { return commonStateExact; }
    public boolean nbtExact() { return nbtExact; }
    public String stateSha256() { return stateSha256; }

    String canonical() {
        return "registry-name=" + registryName + "|runtime-type=" + runtimeType
                + "|reconstructed=" + reconstructed + "|type-exact=" + typeExact
                + "|common-state-exact=" + commonStateExact + "|nbt-exact=" + nbtExact
                + "|state-sha256=" + stateSha256;
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof EntityPersistenceObservation)) return false;
        EntityPersistenceObservation value = (EntityPersistenceObservation) other;
        return subject.equals(value.subject) && registryName.equals(value.registryName)
                && runtimeType.equals(value.runtimeType)
                && reconstructed == value.reconstructed && typeExact == value.typeExact
                && commonStateExact == value.commonStateExact && nbtExact == value.nbtExact
                && stateSha256.equals(value.stateSha256);
    }
    @Override public int hashCode() { return Objects.hash(subject, registryName, runtimeType,
            reconstructed, typeExact, commonStateExact, nbtExact, stateSha256); }
}
