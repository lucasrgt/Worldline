import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;

/** Hydrates one index-authorized module bundle through the strict Rust CAS client. */
final class ModuleCacheHydrator {
    private static final String PROTOCOL = "{\"schema\":\"worldline.cas-transport-protocol.v1\","
            + "\"permits_schema\":\"worldline.cas-transport-permits.v1\","
            + "\"transport\":\"http-loopback-v1\"}\n";
    private static final String MAXIMUM = "8388608";
    private final Path root, binary, permits, cas;
    private final String address;
    private final ModuleCacheIndex index;

    private ModuleCacheHydrator(Path root, Path binary, String address, Path permits,
            Path cas, ModuleCacheIndex index) {
        this.root = root; this.binary = binary; this.address = address;
        this.permits = permits; this.cas = cas; this.index = index;
    }

    static ModuleCacheHydrator connect(Path root, Path binary, String address, Path permits,
            Path indexPath, Path trustPath, String publicKey, String channel, Path cas)
            throws Exception {
        Path executable = binary.toAbsolutePath().normalize();
        require(Files.isRegularFile(executable, LinkOption.NOFOLLOW_LINKS)
                && !Files.isSymbolicLink(executable), "invalid module CAS binary");
        require(address.matches("127\\.0\\.0\\.1:[1-9][0-9]{0,4}"),
                "module CAS address is not loopback");
        int port = Integer.parseInt(address.substring(address.lastIndexOf(':') + 1));
        require(port <= 65_535, "invalid module CAS port");
        require(Files.isRegularFile(permits, LinkOption.NOFOLLOW_LINKS)
                && !Files.isSymbolicLink(permits), "invalid module CAS permits");
        ModuleCacheIndexTrust.verify(indexPath, trustPath, publicKey, channel);
        require(PROTOCOL.equals(ProcessCapture.require(root,
                List.of(executable.toString(), "protocol"), 30)),
                "unsupported module CAS transport protocol");
        return new ModuleCacheHydrator(root, executable, address, permits.toAbsolutePath().normalize(),
                cas.toAbsolutePath().normalize(), ModuleCacheIndex.read(indexPath));
    }

    boolean hydrate(String module, String inputSha256, Path target) throws Exception {
        ModuleCacheIndex.Entry entry = index.lookup(
                module, inputSha256, System.getProperty("java.version"));
        if (entry == null) return false;
        Files.createDirectories(cas); Path downloads = cas.resolveSibling("module-downloads");
        Files.createDirectories(downloads); Path bundle = downloads.resolve(module + "-"
                + ProcessHandle.current().pid() + "-" + System.nanoTime() + ".wmb");
        require(!Files.exists(bundle), "module CAS download target already exists");
        try {
            String expected = entry.blobSha256() + " " + entry.blobSize() + "\n";
            require(expected.equals(command("hydrate", cas, address, permits,
                    entry.blobSha256(), MAXIMUM)), "module CAS hydration identity drifted");
            command("get", cas, entry.blobSha256(), bundle);
            ModuleCacheBundle.Identity identity = ModuleCacheBundle.validate(bundle);
            require(identity.module().equals(module) && identity.input().equals(inputSha256)
                    && identity.javaVersion().equals(System.getProperty("java.version"))
                    && identity.sha256().equals(entry.blobSha256())
                    && identity.size() == entry.blobSize(), "hydrated module bundle identity drifted");
            ModuleCacheBundle.extract(bundle, module, inputSha256, target); return true;
        } finally { Files.deleteIfExists(bundle); }
    }

    private String command(Object... arguments) throws Exception {
        java.util.ArrayList<String> command = new java.util.ArrayList<>();
        command.add(binary.toString());
        for (Object argument : arguments) command.add(argument.toString());
        return ProcessCapture.require(root, command, 60);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
