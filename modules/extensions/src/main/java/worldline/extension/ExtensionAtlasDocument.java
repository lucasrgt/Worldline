package worldline.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Deterministic Atlas projection generated from every registered surface. */
final class ExtensionAtlasDocument {
    private ExtensionAtlasDocument() {}

    static String render(WorldlineExtensionPlan plan) {
        List<ExtensionAtlasPage> pages = pages(plan);
        StringBuilder text = new StringBuilder("WORLDLINE-EXTENSION-ATLAS/1\n");
        text.append("extension=").append(plan.manifest().id()).append('\n');
        text.append("version=").append(plan.manifest().version()).append('\n');
        text.append("pages=").append(pages.size()).append('\n');
        for (int index = 0; index < pages.size(); index++) append(text, index + 1, pages.get(index));
        return text.toString();
    }

    static List<ExtensionAtlasPage> pages(WorldlineExtensionPlan plan) {
        List<ExtensionAtlasPage> pages = new ArrayList<ExtensionAtlasPage>();
        String provenance = provenance(plan);
        for (ExtensionSubject subject : plan.subjects()) pages.add(ExtensionAtlasPage.builder(
                subjectPage(subject), subject.name()).tag("extension").tag(subject.kind().token())
                .provenance(provenance).build());
        for (ExtensionContract contract : plan.contracts()) pages.add(ExtensionAtlasPage.builder(
                contractPage(plan, contract), contract.id()).tag("extension").tag("contract")
                .relation(subjectPage(subject(plan, contract)))
                .provenance(provenance).build());
        for (ExtensionRuntimeAdapter adapter : plan.adapters()) pages.add(ExtensionAtlasPage.builder(
                "atlas.loader." + plan.manifest().id() + "." + adapter.loaderId(), adapter.loaderId())
                .tag("extension").tag("loader")
                .provenance(provenance).build());
        pages.addAll(plan.atlasPages());
        Collections.sort(pages, new Comparator<ExtensionAtlasPage>() {
            @Override public int compare(ExtensionAtlasPage left, ExtensionAtlasPage right) {
                return left.id().compareTo(right.id());
            }
        });
        for (int index = 1; index < pages.size(); index++) if (pages.get(index - 1).id().equals(
                pages.get(index).id())) throw new IllegalArgumentException("duplicate atlas page");
        return Collections.unmodifiableList(pages);
    }

    private static void append(StringBuilder text, int index, ExtensionAtlasPage page) {
        String prefix = "page." + index + ".";
        text.append(prefix).append("id=").append(page.id()).append('\n');
        text.append(prefix).append("title=").append(page.title()).append('\n');
        text.append(prefix).append("tags=").append(join(page.tags())).append('\n');
        text.append(prefix).append("relations=").append(join(page.relations())).append('\n');
        text.append(prefix).append("provenance=").append(page.provenance()).append('\n');
    }

    private static ExtensionSubject subject(WorldlineExtensionPlan plan, ExtensionContract contract) {
        for (ExtensionSubject subject : plan.subjects()) if (subject.id().equals(contract.subjectId()))
            return subject;
        throw new IllegalArgumentException("unknown contract subject");
    }
    private static String subjectPage(ExtensionSubject subject) {
        return "atlas.api." + subject.id().replace(':', '.').replace('/', '.');
    }
    private static String contractPage(WorldlineExtensionPlan plan, ExtensionContract contract) {
        return "atlas.scenario." + plan.manifest().id() + "." + contract.id();
    }
    private static String provenance(WorldlineExtensionPlan plan) {
        return "extension:" + plan.manifest().id() + "@" + plan.manifest().version();
    }
    private static String join(List<String> values) {
        StringBuilder text = new StringBuilder();
        for (String value : values) { if (text.length() > 0) text.append(','); text.append(value); }
        return text.toString();
    }
}
