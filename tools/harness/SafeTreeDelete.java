import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/** Deletes a tree without traversing symbolic links or Windows reparse points. */
final class SafeTreeDelete {
    private SafeTreeDelete() { }

    static void delete(Path target) throws IOException {
        if (!Files.exists(target)) return;
        BasicFileAttributes attributes = Files.readAttributes(target,
                BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        if (attributes.isDirectory() && !attributes.isOther()) {
            try (var children = Files.newDirectoryStream(target)) {
                for (Path child : children) delete(child);
            }
        }
        Files.deleteIfExists(target);
    }
}
