package worldline.symbolgraph;

import java.io.StringReader;

public final class SymbolGraphTest {
    private SymbolGraphTest() {}

    public static void main(String[] arguments) throws Exception {
        parsesNamespacesAndMembers();
        auditsExactIntermediaryIdentity();
        buildsDeterministicCrosswalk();
        rejectsMalformedDocuments();
        System.out.println("SymbolGraphTest passed");
    }

    private static void parsesNamespacesAndMembers() throws Exception {
        TinyMapping mapping = read("tiny\t2\t0\tintermediary\tnamed\n"
                + "c\tclass_1\tLevel\n"
                + "\tf\tI\tfield_1\tseed\n"
                + "\tm\t()V\tmethod_1\ttick\n"
                + "\t\tp\t0\tparameter_1\tignored\n");
        require(mapping.namespaces().size() == 2, "namespace count");
        require(mapping.count(SymbolKind.CLASS) == 1, "class count");
        require(mapping.count(SymbolKind.FIELD) == 1, "field count");
        require(mapping.count(SymbolKind.METHOD) == 1, "method count");
        SymbolKey field = new SymbolKey(SymbolKind.FIELD, "class_1", "field_1", "I");
        require("seed".equals(mapping.symbols().get(field).name(1)), "named field");
    }

    private static void auditsExactIntermediaryIdentity() throws Exception {
        TinyMapping inventory = read("tiny\t2\t0\tintermediary\tclientOfficial\tserverOfficial\n"
                + "c\tclass_1\ta\tb\n"
                + "\tf\tI\tfield_1\tc\td\n"
                + "\tm\t()V\tmethod_1\te\tf\n");
        TinyMapping named = read("tiny\t2\t0\tintermediary\tnamed\n"
                + "c\tclass_1\tLevel\n"
                + "\tm\t()V\tmethod_1\ttick\n"
                + "\tm\t(I)V\tmethod_extra\thelper\n");
        MappingAudit.Report report = new MappingAudit().compare(inventory, named);
        require(report.difference(SymbolKind.CLASS).missing().isEmpty(), "class match");
        require(report.difference(SymbolKind.FIELD).missing().size() == 1, "field gap");
        require(report.difference(SymbolKind.METHOD).extra().size() == 1, "method extra");
        require(report.render().contains("method\t1\t2\t0\t1"), "stable report");
    }

    private static void rejectsMalformedDocuments() throws Exception {
        failure(() -> read("tiny\t1\t0\ta\tb\n"));
        failure(() -> read("tiny\t2\t0\tintermediary\tnamed\n\tf\tI\tx\ty\n"));
        failure(() -> read("tiny\t2\t0\tintermediary\tintermediary\n"));
    }

    private static void buildsDeterministicCrosswalk() throws Exception {
        TinyMapping inventory = read("tiny\t2\t0\tintermediary\tclientOfficial\tserverOfficial\n"
                + "c\tclass_1\ta\tb\n"
                + "\tf\tI\tfield_1\tc\t\n"
                + "c\tclass_2\td\t\n");
        TinyMapping named = read("tiny\t2\t0\tintermediary\tnamed\n"
                + "c\tclass_1\tLevel\n"
                + "\tf\tI\tfield_1\tseed\n"
                + "\tm\t()V\tmethod_extra\thelper\n");
        SymbolGraph first = new SymbolGraphBuilder().build(inventory, named);
        SymbolGraph second = new SymbolGraphBuilder().build(inventory, named);
        require(first.records().size() == 4, "union size");
        require(first.sha256().equals(second.sha256()), "deterministic graph");
        SymbolRecord shared = first.record(new SymbolKey(SymbolKind.CLASS, "", "class_1", ""));
        require(shared.side() == SymbolSide.SHARED && "Level".equals(shared.nostalgia()), "shared alias");
        SymbolRecord client = first.record(new SymbolKey(SymbolKind.FIELD, "class_1", "field_1", "I"));
        require(client.side() == SymbolSide.CLIENT, "client member");
        SymbolRecord extra = first.record(
                new SymbolKey(SymbolKind.METHOD, "class_1", "method_extra", "()V"));
        require(extra.side() == SymbolSide.UNRESOLVED && !extra.inventoryPresent(), "named-only member");
    }

    private static TinyMapping read(String text) throws Exception {
        return new TinyV2Reader().read(new StringReader(text));
    }

    private static void failure(Checked action) throws Exception {
        try { action.run(); throw new AssertionError("expected failure"); }
        catch (IllegalArgumentException expected) { }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private interface Checked { void run() throws Exception; }
}
