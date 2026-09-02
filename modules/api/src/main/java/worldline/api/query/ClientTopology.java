package worldline.api.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Neutral N-client topology: how many sessions share one world and who they are. */
public final class ClientTopology {
    private final List<String> clients;

    private ClientTopology(List<String> clients) {
        this.clients = Collections.unmodifiableList(new ArrayList<String>(clients));
    }

    public static ClientTopology empty() {
        return new ClientTopology(Collections.<String>emptyList());
    }

    public static ClientTopology of(List<String> clients) {
        if (clients == null) throw new NullPointerException("clients");
        for (String client : clients) {
            if (client == null || client.trim().isEmpty())
                throw new IllegalArgumentException("blank client identity");
        }
        return new ClientTopology(clients);
    }

    public static ClientTopology of(String... clients) {
        if (clients == null) throw new NullPointerException("clients");
        List<String> values = new ArrayList<String>();
        for (String client : clients) values.add(client);
        return of(values);
    }

    public List<String> clients() { return clients; }
    public int size() { return clients.size(); }
    public boolean isEmpty() { return clients.isEmpty(); }

    @Override public boolean equals(Object other) {
        if (!(other instanceof ClientTopology)) return false;
        return clients.equals(((ClientTopology) other).clients);
    }

    @Override public int hashCode() { return clients.hashCode(); }
}
