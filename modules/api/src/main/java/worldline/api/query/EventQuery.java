package worldline.api.query;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import worldline.api.RemoteWorldEvent;

/** Neutral query over world events, independent of one-scenario evidence types. */
public final class EventQuery {
    private final int effectId;
    private final List<RemoteWorldEvent> matches;

    private EventQuery(int effectId, List<RemoteWorldEvent> matches) {
        this.effectId = effectId;
        this.matches = Collections.unmodifiableList(matches);
    }

    public static EventQuery ofEffect(int effectId) {
        if (effectId < 0) throw new IllegalArgumentException("invalid world-event effect");
        return new EventQuery(effectId, Collections.<RemoteWorldEvent>emptyList());
    }

    public static EventQuery matching(int effectId, List<RemoteWorldEvent> matches) {
        if (matches == null) throw new NullPointerException("matches");
        EventQuery query = ofEffect(effectId);
        for (RemoteWorldEvent event : matches) {
            if (event == null) throw new NullPointerException("event");
            if (event.effectId() != effectId)
                throw new IllegalArgumentException("world-event effect drifted");
        }
        return new EventQuery(query.effectId, matches);
    }

    public int effectId() { return effectId; }
    public List<RemoteWorldEvent> matches() { return matches; }
    public int size() { return matches.size(); }

    @Override public boolean equals(Object other) {
        if (!(other instanceof EventQuery)) return false;
        EventQuery value = (EventQuery) other;
        return effectId == value.effectId && matches.equals(value.matches);
    }

    @Override public int hashCode() { return Objects.hash(effectId, matches); }
}
