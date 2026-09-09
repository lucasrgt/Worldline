package worldline.testapi;

import java.time.LocalDate;
import java.util.Locale;

/** One externally evidenced use of a released TestKit artifact by a real mod repository. */
public final class ExternalConsumerUse {
    private final String repository, revision, release, date, proof;
    private final int passedTests;
    private final boolean publicArtifactsOnly;
    public ExternalConsumerUse(String repository, String revision, String release, String date,
            int passedTests, boolean publicArtifactsOnly, String proof) {
        if (repository == null || !repository.matches("https://github[.]com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+")
                || revision == null || !revision.matches("[a-f0-9]{40}") || release == null
                || !release.matches("[0-9]+[.][0-9]+[.][0-9]+") || date == null || passedTests < 1
                || proof == null || proof.trim().isEmpty() || proof.contains("\n") || proof.contains("|")) {
            throw new IllegalArgumentException("invalid external consumer observation");
        }
        LocalDate.parse(date);
        String normalized = repository.toLowerCase(Locale.ROOT);
        this.repository = normalized.endsWith(".git") ? normalized.substring(0, normalized.length() - 4) : normalized;
        this.revision = revision;
        this.release = release; this.date = date; this.passedTests = passedTests;
        this.publicArtifactsOnly = publicArtifactsOnly; this.proof = proof;
    }
    public String repository() { return repository; }
    public String revision() { return revision; }
    public String release() { return release; }
    public LocalDate date() { return LocalDate.parse(date); }
    public int passedTests() { return passedTests; }
    public boolean publicArtifactsOnly() { return publicArtifactsOnly; }
    public String proof() { return proof; }
    public String canonical() {
        return repository + '|' + revision + '|' + release + '|' + date + '|' + passedTests + '|'
                + publicArtifactsOnly + '|' + proof;
    }
}
