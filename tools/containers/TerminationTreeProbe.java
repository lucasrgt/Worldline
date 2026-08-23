import java.nio.file.Path;
import java.util.List;

/** Harmless parent/child process tree used to prove timeout cleanup. */
public final class TerminationTreeProbe {
    public static void main(String[] arguments) throws Exception {
        if (List.of(arguments).equals(List.of("parent"))) {
            new ProcessBuilder(java(), "tools/containers/TerminationTreeProbe.java", "leaf")
                    .inheritIO().start();
            Thread.sleep(60_000L);
        } else if (List.of(arguments).equals(List.of("leaf"))) Thread.sleep(60_000L);
        else throw new IllegalArgumentException("usage: TerminationTreeProbe.java parent|leaf");
    }

    private static String java() {
        return Path.of(System.getProperty("java.home"), "bin", "java").toString();
    }
}
