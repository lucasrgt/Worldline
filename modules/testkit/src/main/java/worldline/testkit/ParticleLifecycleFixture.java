package worldline.testkit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.testapi.ParticleLifecycleObservation;

/** Public acceptance for native construction, live rendering, aging and manager removal. */
public final class ParticleLifecycleFixture {
    private ParticleLifecycleFixture() { }

    public static ParticleLifecycleEvidence verify(List<String> particles,
            List<ParticleLifecycleObservation> observations) {
        if (particles == null || observations == null) throw new NullPointerException("particle lifecycle");
        require(!particles.isEmpty() && observations.size() == particles.size() * 10, "particle census drift");
        Set<String> unique = new HashSet<String>();
        List<ParticleLifecycleObservation> accepted = new ArrayList<ParticleLifecycleObservation>();
        int index = 0;
        for (String particle : particles) {
            require(particle != null && unique.add(particle), "duplicate particle subject");
            for (int tick = 0; tick <= 9; tick++) {
                ParticleLifecycleObservation row = observations.get(index++);
                require(row != null && particle.equals(row.particle()) && row.tick() == tick,
                        "particle identity or tick drift");
                require(row.age() == tick && row.dead() == (tick == 9)
                        && row.count() == (tick == 9 ? 0 : 1), "native particle lifecycle drift");
                require(tick == 0 || tick == 9 ? row.pixels() == 0 : row.pixels() > 0,
                        "particle frame visibility drift");
                accepted.add(row);
            }
        }
        return new ParticleLifecycleEvidence(accepted);
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
