package worldline.symbolgraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/** Classifies namespace coverage without inferring behavioral semantics. */
public final class NamespaceAudit {
    public Report inspect(SymbolGraph graph) {
        Map<NamespaceIssue, List<SymbolKey>> findings =
                new EnumMap<NamespaceIssue, List<SymbolKey>>(NamespaceIssue.class);
        for (NamespaceIssue issue : NamespaceIssue.values()) {
            findings.put(issue, new ArrayList<SymbolKey>());
        }
        for (SymbolRecord record : graph.records()) {
            EnumSet<NamespaceIssue> issues = classify(record);
            for (NamespaceIssue issue : issues) findings.get(issue).add(record.key());
        }
        return new Report(findings);
    }

    private static EnumSet<NamespaceIssue> classify(SymbolRecord record) {
        EnumSet<NamespaceIssue> issues = EnumSet.noneOf(NamespaceIssue.class);
        if (!record.inventoryPresent()) {
            issues.add(NamespaceIssue.WORLDLINE_MISSING);
            return issues;
        }
        if (!record.nostalgiaPresent()) issues.add(NamespaceIssue.NOSTALGIA_MISSING);
        if (record.retroMcpClient().isEmpty() && record.retroMcpServer().isEmpty()) {
            issues.add(NamespaceIssue.RETROMCP_MISSING);
        }
        if ((record.side() == SymbolSide.CLIENT && !record.retroMcpServer().isEmpty())
                || (record.side() == SymbolSide.SERVER && !record.retroMcpClient().isEmpty())) {
            issues.add(NamespaceIssue.SIDE_CONFLICT);
        }
        List<String> aliases = aliases(record);
        boolean different = false;
        for (int index = 1; index < aliases.size(); index++) {
            if (!aliases.get(0).equals(aliases.get(index))) different = true;
        }
        if (different) issues.add(NamespaceIssue.NAME_DIFFERENCE);
        if (issues.isEmpty()) issues.add(NamespaceIssue.MATCH);
        return issues;
    }

    private static List<String> aliases(SymbolRecord record) {
        List<String> aliases = new ArrayList<String>();
        add(aliases, record.nostalgia());
        add(aliases, record.retroMcpClient());
        add(aliases, record.retroMcpServer());
        return aliases;
    }

    private static void add(List<String> aliases, String alias) {
        if (!alias.isEmpty() && !aliases.contains(alias)) aliases.add(alias);
    }

    public static final class Report {
        private final Map<NamespaceIssue, List<SymbolKey>> findings;

        Report(Map<NamespaceIssue, List<SymbolKey>> findings) {
            Map<NamespaceIssue, List<SymbolKey>> copy =
                    new EnumMap<NamespaceIssue, List<SymbolKey>>(NamespaceIssue.class);
            for (Map.Entry<NamespaceIssue, List<SymbolKey>> entry : findings.entrySet()) {
                copy.put(entry.getKey(), Collections.unmodifiableList(
                        new ArrayList<SymbolKey>(entry.getValue())));
            }
            this.findings = Collections.unmodifiableMap(copy);
        }

        public List<SymbolKey> findings(NamespaceIssue issue) { return findings.get(issue); }

        public String renderSummary() {
            StringBuilder text = new StringBuilder("classification\tcount\n");
            for (NamespaceIssue issue : NamespaceIssue.values()) {
                text.append(issue.name()).append('\t').append(findings(issue).size()).append('\n');
            }
            return text.toString();
        }
    }
}
