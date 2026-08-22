package worldline.symbolgraph;

import java.nio.file.Paths;

public final class MappingAuditMain {
    private MappingAuditMain() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 2) {
            System.err.println("usage: MappingAuditMain <intermediary.jar> <nostalgia.jar>");
            System.exit(2);
        }
        TinyMapping inventory = MappingArchive.read(Paths.get(arguments[0]), "mappings/mappings.tiny");
        TinyMapping named = MappingArchive.read(Paths.get(arguments[1]), "mappings/mappings.tiny");
        System.out.print(new MappingAudit().compare(inventory, named).render());
    }
}
