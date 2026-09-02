package worldline.testapi;

import java.util.List;
import java.util.Objects;
import worldline.api.BlockState;
import worldline.api.RemoteSignText;

/** Equatable canonical evidence for the native sign subsystem contract. */
public final class SignSubsystemEvidence {
    private final SignSubsystemObservation observation;

    public SignSubsystemEvidence(SignSubsystemObservation observation) {
        this.observation = Objects.requireNonNull(observation, "observation");
    }

    public SignSubsystemObservation observation() { return observation; }

    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.sign-subsystem-evidence.v1\n");
        value.append("subjects=b1.7.3:block/063,b1.7.3:block/068\n");
        value.append("claims=standing:state-domain+gameplay-placement+break-transition")
                .append("+drop-matrix+save-reload+collision-shape+light-behavior")
                .append("+tick-policy+neighbor-response,wall:collision-shape")
                .append("+light-behavior+tick-policy+neighbor-response\n");
        value.append("standing-domain=").append(join(observation.standingMetadata())).append('\n');
        value.append("placed=").append(state(observation.placedStanding())).append('+')
                .append(state(observation.placedWall())).append('\n');
        value.append("inventory=").append(observation.signCountBefore()).append("->")
                .append(observation.signCountAfterFirstPlace()).append('\n');
        value.append("direct-break=").append(state(observation.directBrokenFrom())).append("->")
                .append(state(observation.directBrokenTo())).append("|drop=")
                .append(observation.directDrop()).append('\n');
        value.append("text=").append(text(observation.standingText())).append('+')
                .append(text(observation.wallText())).append('\n');
        value.append("persisted=").append(state(observation.persistedStanding())).append('+')
                .append(state(observation.persistedWall())).append("|text=true\n");
        value.append("collision=").append(join(observation.collisions())).append('\n');
        value.append("light=").append(join(observation.blockLight())).append('|')
                .append(join(observation.skyLight())).append('\n');
        value.append("tick=").append(observation.tickWindow()).append(':')
                .append(state(observation.tickStanding())).append('+')
                .append(state(observation.tickWall())).append('\n');
        value.append("unsupported=").append(state(observation.unsupportedStanding())).append('+')
                .append(state(observation.unsupportedWall())).append('\n');
        return value.append("reload=").append(observation.boundary()).append('x')
                .append(observation.reloads()).append("|final=")
                .append(state(observation.finalStanding())).append('+')
                .append(state(observation.finalWall())).append('\n').toString();
    }

    @Override public boolean equals(Object other) {
        return other instanceof SignSubsystemEvidence
                && observation.equals(((SignSubsystemEvidence) other).observation);
    }
    @Override public int hashCode() { return observation.hashCode(); }

    private static String join(List<?> values) {
        StringBuilder result = new StringBuilder();
        for (Object value : values) {
            if (result.length() > 0) result.append(',');
            result.append(value);
        }
        return result.toString();
    }
    private static String state(BlockState value) {
        return value.legacyId() + ":" + value.metadata();
    }
    private static String text(RemoteSignText value) {
        return value.line(0) + '/' + value.line(1) + '/' + value.line(2) + '/' + value.line(3);
    }
}
