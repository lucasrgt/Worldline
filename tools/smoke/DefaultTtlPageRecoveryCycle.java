import java.io.*;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.*;

/** Runs two fresh same-plan full-page depletion/recovery replicas. */
public final class DefaultTtlPageRecoveryCycle {
  private static final String ID = "m94-default-ttl-page-recovery";
  private final Path root = Paths.get("").toAbsolutePath().normalize(),
                     smoke = root.resolve("smokes").resolve(ID),
                     build = root.resolve(".worldline/smokes").resolve(ID);
  private final Properties config = new Properties();
  public static void main(String[] a) {
    if (!Arrays.equals(a, new String[] {ID})) {
      System.err.println("usage: java tools/smoke/DefaultTtlPageRecoveryCycle.java " + ID);
      System.exit(2);
    }
    try {
      new DefaultTtlPageRecoveryCycle().execute();
    } catch (Exception e) {
      System.err.println("Full-page depletion recovery failed: " + e.getMessage());
      System.exit(1);
    }
  }
  private void execute() throws Exception {
    load(smoke.resolve("smoke.properties"), config);
    require(value("replicas").equals("2") && value("cycles").equals("6")
            && value("warmup.frames").equals("300") && value("warmup.millis").equals("5000")
            && value("minimum.frames").equals("720") && value("minimum.millis").equals("12000")
            && value("change.after.records").equals("300")
            && value("restore.after.records").equals("30")
            && value("aero.rebuilds.per.frame").equals("8")
            && value("aero.page.ttl.mode").equals("normal-default-600")
            && value("fps.limit").equals("0") && value("aero.frame.pacing").equals("false"),
        "M94 design drift");
    Path checkout = root.resolve(value("aero.path")).normalize();
    verifyCheckout(checkout);
    verifyBoundary();
    recreate(build);
    buildAero(checkout);
    boolean diagnostic = Boolean.getBoolean("worldline.m94.diagnostic");
    int runs = diagnostic ? 1 : 2, nonce = 9409401;
    List<Arm> arms = new ArrayList<>();
    Plan plan = null;
    for (int i = 0; i < runs; i++) {
      verifyCheckout(checkout);
      Arm arm = run(checkout, build.resolve("replica-" + (i + 1)), i, nonce, plan);
      arms.add(arm);
      if (plan == null)
        plan = new Plan(arm.x, arm.y, arm.z);
      verifyCheckout(checkout);
    }
    if (diagnostic) {
      System.out.println("M94 diagnostic replica passed; qualification not attempted");
      System.out.println("  " + arms.get(0).summary());
      return;
    }
    Arm first = arms.get(0), second = arms.get(1);
    require(first.nonce == second.nonce && first.x == second.x && first.y == second.y
            && first.z == second.z && first.raw == second.raw,
        "replica fixture drift");
    String trace =
        "v1|design=2-fresh-same-plan-nonce-default-ttl-empty-page-eviction-recovery-replicas|fixture=constant16-synced+client-marker+exact-camera|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|page-ttl=normal-default600+property-absent-runtime-gated|warm-path=enqueue16+flush2+cached4+pageCalls4+direct0+render0/list0+rebuild0|sequence=remove-indices1-2-3-5-6-7+restore7-6-5-3-2-1-with-ordinal+operation+index-bound-ack/state|spacing=request1-after300+removals30-after-event+restore1-30-after-expiry+remaining-restores30-after-event|membership=16to15to14to13to12to11to10to11to12to13to14to15to16|page-count-6to2-and-2to6=pageCalls4+direct0+rebuild1|page-count-1=pageCalls3+direct1+rebuild0|page-count-0=pageCalls3+direct0+rebuild0|window=M74-min720intervals+12s|cache-expiry=empty-page+default600+sweep128+cached4to3+expiredDelta1+restore2-cached3to4|capture=M78-primitive-spans+page-counters+post-seal-184-byte-recovery-sidecar|per-record=state16/maskffff|stats=descriptive-transition-dynamic|other-pages-concurrency-high-memory-or-explicit-ttl-max-cache-eviction-uninstrumented-cost-causality-regression-historical-lag=not-claimed|shutdown=clean";
    String signature = sha256(trace);
    require(signature.equals(value("expected.signature")), "M94 signature drift: " + signature);
    Files.writeString(build.resolve("evidence.txt"),
        "id=" + ID + "\nreplicas=2\nserver.jvm=2\nclient.jvm=2\nfirst=" + first.summary()
            + "\nsecond=" + second.summary() + "\ntrace=" + trace + "\nsignature=" + signature
            + "\n",
        StandardCharsets.UTF_8);
    System.out.println("M94 default-TTL page recovery passed");
    System.out.println("  first: " + first.summary());
    System.out.println("  second: " + second.summary());
    System.out.println("  signature: " + signature);
  }
  private Arm run(Path checkout, Path workspace, int replica, int nonce, Plan plan)
      throws Exception {
    Files.createDirectories(workspace);
    int port = freePort(), timeout = Integer.parseInt(value("timeout.seconds"));
    Path base = root.resolve(".worldline/worktrees/m94-" + ProcessHandle.current().pid() + "-"
             + replica + "-" + System.nanoTime()),
         serverTree = base.resolve("server"), clientTree = base.resolve("client");
    Captured server = null;
    try {
      addWorktree(checkout, serverTree);
      addWorktree(checkout, clientTree);
      Path init = root.resolve(value("runner")), serverGame = workspace.resolve("server"),
           clientGame = workspace.resolve("client"), censusFile = clientGame.resolve("census.bin"),
           pagedFile = clientGame.resolve("paged.bin"),
           recoveryFile = clientGame.resolve("recovery.bin");
      server = startServer(serverTree.resolve("stationapi/test-bare"),
          command(serverTree, init, "server", serverGame, port, nonce, null, null, null, plan),
          timeout);
      String client = runGradle(clientTree.resolve("stationapi/test-bare"),
          command(clientTree, init, "client", clientGame, port, nonce, censusFile, pagedFile,
              recoveryFile, null),
          timeout);
      Files.writeString(workspace.resolve("client-output.txt"), client);
      require(client.contains("Loading 46 mods:") && client.contains("- aero-model-lib 3.0.0")
              && client.contains("- worldline-m74-content 1.0.0")
              && client.contains("BUILD SUCCESSFUL") && client.contains("[WorldlineCensus] packet1")
              && client.contains("[WorldlineCensus] packet13") && !client.contains("[Aero_"),
          "client boundary drift\n" + diagnostic(client));
      require(count(client, "[WorldlinePaged] armed ") == 1
              && unique(client, "[WorldlinePaged] armed ")
                  .equals("marker=true fpsLimit=0 aeroFramePacing=false")
              && count(client, "[WorldlineCensus] plan-ready") == 1
              && count(client, "[WorldlineCensus] census-start ") == 1
              && count(client, "[WorldlineCensus] complete ") == 1
              && count(client, "[WorldlinePaged] complete ") == 1
              && count(client, "[WorldlineRecovery] complete ") == 1,
          "client lifecycle drift");
      Recovery change = parseRecovery(recoveryFile, nonce);
      Census census = parseCensus(censusFile, change, nonce);
      Paged paged = parsePaged(pagedFile, census, change, nonce);
      String complete = unique(client, "[WorldlinePaged] complete "),
             changed = unique(client, "[WorldlineRecovery] complete ");
      require(marker(complete, "samples") == paged.count
              && marker(complete, "bytes") == Files.size(pagedFile)
              && token(complete, "sha256").equals(sha256(Files.readAllBytes(pagedFile)))
              && token(changed, "requests").equals(change.requests())
              && token(changed, "events").equals(change.events())
              && marker(changed, "expiry") == change.expiry
              && token(changed, "expired").equals(change.expiredBefore + "->" + change.expiredAfter)
              && marker(changed, "bytes") == Files.size(recoveryFile)
              && token(changed, "sha256").equals(sha256(Files.readAllBytes(recoveryFile))),
          "artifact marker drift");
      server.write("save-all\nstop\n");
      server.finish(45);
      String serverText = server.output();
      Files.writeString(workspace.resolve("server-output.txt"), serverText);
      server = null;
      require((serverText.contains("Loading 39 mods:") || serverText.contains("Loading 40 mods:"))
              && serverText.contains("- worldline-m74-content 1.0.0")
              && !serverText.contains("- aero-model-lib ")
              && serverText.contains("BUILD SUCCESSFUL"),
          "server boundary drift\n" + diagnostic(serverText));
      String scene = unique(serverText, "[WorldlineCensus] scene "),
             r1 = unique(serverText, "[WorldlineRecovery] removed ordinal=1 "),
             r2 = unique(serverText, "[WorldlineRecovery] removed ordinal=2 "),
             r3 = unique(serverText, "[WorldlineRecovery] removed ordinal=3 "),
             r5 = unique(serverText, "[WorldlineRecovery] removed ordinal=4 "),
             r6 = unique(serverText, "[WorldlineRecovery] removed ordinal=5 "),
             r7 = unique(serverText, "[WorldlineRecovery] removed ordinal=6 "),
             a7 = unique(serverText, "[WorldlineRecovery] restored ordinal=7 "),
             a6 = unique(serverText, "[WorldlineRecovery] restored ordinal=8 "),
             a5 = unique(serverText, "[WorldlineRecovery] restored ordinal=9 "),
             a3 = unique(serverText, "[WorldlineRecovery] restored ordinal=10 "),
             a2 = unique(serverText, "[WorldlineRecovery] restored ordinal=11 "),
             a1 = unique(serverText, "[WorldlineRecovery] restored ordinal=12 ");
      require(token(scene, "mode").equals("present") && marker(scene, "planned") == 16
              && marker(scene, "placed") == 16 && marker(scene, "yaw") == -90
              && marker(scene, "pitch") == 0 && marker(scene, "nonce") == nonce,
          "scene drift: " + scene);
      int x = marker(scene, "x"), y = marker(scene, "baseY"), z = marker(scene, "baseZ"),
          raw = marker(scene, "raw");
      require(sameCell(r1, x, y, z, nonce, 1) && sameCell(r2, x, y, z, nonce, 2)
              && sameCell(r3, x, y, z, nonce, 3) && sameCell(r5, x, y, z, nonce, 5)
              && sameCell(r6, x, y, z, nonce, 6) && sameCell(r7, x, y, z, nonce, 7)
              && sameCell(a7, x, y, z, nonce, 7) && sameCell(a6, x, y, z, nonce, 6)
              && sameCell(a5, x, y, z, nonce, 5) && sameCell(a3, x, y, z, nonce, 3)
              && sameCell(a2, x, y, z, nonce, 2) && sameCell(a1, x, y, z, nonce, 1) && change.x == x
              && change.y == y && change.z == z
              && (plan == null || plan.x == x && plan.y == y && plan.z == z) && census.x == x
              && census.y == y && census.z == z,
          "recovery/plan drift");
      require(ordered(serverText,
                  new String[] {"removed ordinal=1 ", "removed ordinal=2 ", "removed ordinal=3 ",
                      "removed ordinal=4 ", "removed ordinal=5 ", "removed ordinal=6 ",
                      "restored ordinal=7 ", "restored ordinal=8 ", "restored ordinal=9 ",
                      "restored ordinal=10 ", "restored ordinal=11 ", "restored ordinal=12 "})
              && serverText.contains(value("username") + " lost connection")
              && serverText.contains("Stopping server"),
          "server lifecycle drift");
      verifyWorktree(serverTree);
      verifyWorktree(clientTree);
      return new Arm(nonce, x, y, z, raw, census, paged, change,
          sha256(Files.readAllBytes(censusFile)), sha256(Files.readAllBytes(pagedFile)),
          sha256(Files.readAllBytes(recoveryFile)));
    } finally {
      if (server != null) {
        try {
          server.write("stop\n");
          server.finish(20);
        } catch (Exception e) {
          server.kill();
        } finally {
          Files.writeString(workspace.resolve("server-output.txt"), server.output());
        }
      }
      removeWorktree(checkout, clientTree);
      removeWorktree(checkout, serverTree);
    }
  }
  private Recovery parseRecovery(Path file, int nonce) throws Exception {
    byte[] b = Files.readAllBytes(file);
    try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(b))) {
      require(b.length == 184 && in.readInt() == 0x574c3934 && in.readInt() == 1
              && in.readInt() == 184 && in.readInt() == nonce,
          "recovery schema drift");
      int x = in.readInt(), y = in.readInt(), z = in.readInt(), expiry = in.readInt(),
          expiredBefore = in.readInt(), expiredAfter = in.readInt();
      int[] requests = new int[12], events = new int[12];
      for (int i = 0; i < 12; i++) {
        requests[i] = in.readInt();
        events[i] = in.readInt();
        int index = in.readInt();
        require(index == new int[] {1, 2, 3, 5, 6, 7, 7, 6, 5, 3, 2, 1}[i],
            "recovery cell index drift " + i);
      }
      for (int i = 0; i < 12; i++)
        require(requests[i] >= (i == 0 ? 300
                        : i == 6       ? expiry + 1 + 30
                                       : events[i - 1] + 30)
                && events[i] >= requests[i],
            "recovery event drift " + i);
      require(expiry >= events[5] && expiry < requests[6] && expiredBefore == 0 && expiredAfter == 1
              && in.read() == -1,
          "recovery expiry/trailing drift");
      return new Recovery(x, y, z, expiry, expiredBefore, expiredAfter, requests, events);
    }
  }
  private Census parseCensus(Path file, Recovery change, int nonce) throws Exception {
    byte[] bytes = Files.readAllBytes(file);
    try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
      require(in.readInt() == 0x574c3734 && in.readInt() == 1 && in.readInt() == 28
              && in.readInt() == 16 && in.readInt() == nonce,
          "census schema drift");
      int x = in.readInt(), y = in.readInt(), z = in.readInt();
      require(in.readInt() == 720 && in.readLong() == 12_000_000_000L, "census window drift");
      int count = in.readInt();
      long elapsed = in.readLong();
      require(x == change.x && y == change.y && z == change.z && change.events[11] < count
              && count >= 720 && count <= 65536 && bytes.length == 56L + count * 28L,
          "census length/identity drift");
      long sum = 0;
      long[] frames = new long[count];
      for (int i = 0; i < count; i++) {
        long delta = in.readLong();
        int renders = in.readInt(), lists = in.readInt(), visible = in.readInt(),
            calls = in.readInt(), state = in.readUnsignedShort(), mask = in.readUnsignedShort(),
            expected = change.members(i), direct = change.direct(i);
        require(delta > 0 && renders == direct && lists == direct && visible > 0
                && calls == expected && state == 0x1010 && mask == 0xffff,
            "recovery census record drift at " + i + " values=" + renders + "/" + lists + "/"
                + calls);
        frames[i] = delta;
        sum = Math.addExact(sum, delta);
      }
      require(in.read() == -1 && sum == elapsed && elapsed >= 12_000_000_000L,
          "census aggregate drift");
      return new Census(x, y, z, count, elapsed, frames);
    }
  }
  private Paged parsePaged(Path file, Census census, Recovery change, int nonce) throws Exception {
    byte[] bytes = Files.readAllBytes(file);
    try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
      require(in.readInt() == 0x574c3738 && in.readInt() == 1 && in.readInt() == 44
              && in.readInt() == 52 && in.readInt() == nonce,
          "paged schema drift");
      int x = in.readInt(), y = in.readInt(), z = in.readInt(), count = in.readInt();
      long elapsed = in.readLong();
      require(x == census.x && y == census.y && z == census.z && count == census.count
              && elapsed == census.elapsed && bytes.length == 44L + count * 52L,
          "paged/census identity drift");
      long[] renderer = new long[count], queue = new long[count], flush = new long[count];
      int zeroFlush = 0;
      long flushSum = 0;
      for (int i = 0; i < count; i++) {
        renderer[i] = in.readLong();
        queue[i] = in.readLong();
        flush[i] = in.readLong();
        int rc = in.readUnsignedShort(), qc = in.readUnsignedShort(), fc = in.readUnsignedShort(),
            reserved = in.readUnsignedShort(), queued = in.readInt(), pageCalls = in.readInt(),
            direct = in.readInt(), rebuilds = in.readInt(), cached = in.readInt(),
            members = change.members(i), expectedDirect = change.direct(i),
            pages = i >= change.events[4] && i < change.events[7] ? 3 : 4,
            expectedRebuild = change.rebuild(i),
            expectedCached = i >= change.expiry && i < change.events[7] ? 3 : 4;
        require(renderer[i] > 0 && queue[i] > 0 && flush[i] >= 0 && renderer[i] >= queue[i]
                && rc == members && qc == members && fc == 2 && reserved == 0 && queued == members
                && pageCalls == pages && direct == expectedDirect && rebuilds == expectedRebuild
                && cached == expectedCached,
            "recovery paged record drift at " + i + " calls=" + rc + "/" + qc + "/" + fc
                + " state=" + queued + "/" + pageCalls + "/" + direct + "/" + rebuilds + "/"
                + cached + " expiry=" + change.expiry);
        if (flush[i] == 0)
          zeroFlush++;
        flushSum = Math.addExact(flushSum, flush[i]);
      }
      require(in.read() == -1 && flushSum > 0, "paged aggregate drift");
      return new Paged(count, renderer, queue, flush, zeroFlush, change.events);
    }
  }
  private List<String> command(Path tree, Path init, String role, Path game, int port, int nonce,
      Path census, Path paged, Path recovery, Plan plan) {
    String wrapper =
        System.getProperty("os.name").startsWith("Windows") ? "gradlew.bat" : "gradlew";
    List<String> r = new ArrayList<>(
        Arrays.asList(tree.resolve("stationapi/test-bare").resolve(wrapper).toString(),
            "--no-daemon", "--init-script", init.toString(),
            role.equals("server") ? "runServer" : "runClient", "-PworldlineRole=" + role,
            "-PworldlineRunDir=" + game, "-PworldlinePort=" + port, "-PworldlineNonce=" + nonce));
    if (plan != null)
      r.addAll(Arrays.asList("-PworldlinePlanX=" + plan.x, "-PworldlinePlanY=" + plan.y,
          "-PworldlinePlanZ=" + plan.z));
    if (census != null)
      r.addAll(Arrays.asList("-PworldlineArtifact=" + census, "-PworldlinePagedArtifact=" + paged,
          "-PworldlineRecoveryArtifact=" + recovery,
          "-PworldlineChangeAfter=" + value("change.after.records"),
          "-PworldlineRestoreAfter=" + value("restore.after.records"),
          "-PworldlineRebuildsPerFrame=" + value("aero.rebuilds.per.frame"),
          "-PworldlineUsername=" + value("username"),
          "-PworldlineWarmupFrames=" + value("warmup.frames"),
          "-PworldlineWarmupMillis=" + value("warmup.millis"),
          "-PworldlineMinimumFrames=" + value("minimum.frames"),
          "-PworldlineMinimumMillis=" + value("minimum.millis"),
          "-PworldlineFpsLimit=" + value("fps.limit"),
          "-PworldlineAeroFramePacing=" + value("aero.frame.pacing")));
    return r;
  }
  private void buildAero(Path checkout) throws Exception {
    String prebuilt = System.getenv("WORLDLINE_AERO_PREBUILT");
    if (prebuilt != null && !prebuilt.isBlank()) {
      Path jar = Path.of(prebuilt).toAbsolutePath().normalize();
      require(Files.isRegularFile(jar), "missing prebuilt Aero");
      Files.copy(
          jar, build.resolve("aero-model-lib-3.0.0.jar"), StandardCopyOption.REPLACE_EXISTING);
      return;
    }
    Path tree = root.resolve(".worldline/worktrees/m94-build-" + System.nanoTime());
    try {
      addWorktree(checkout, tree);
      Path stationapi = tree.resolve("stationapi");
      String wrapper =
                 System.getProperty("os.name").startsWith("Windows") ? "gradlew.bat" : "gradlew",
             output = runGradle(stationapi,
                 Arrays.asList(stationapi.resolve(wrapper).toString(), "--no-daemon", "remapJar"),
                 Integer.parseInt(value("timeout.seconds")));
      Path jar = stationapi.resolve("build/libs/aero-model-lib-3.0.0.jar");
      require(output.contains("BUILD SUCCESSFUL") && Files.isRegularFile(jar), "Aero build failed");
      Files.copy(
          jar, build.resolve("aero-model-lib-3.0.0.jar"), StandardCopyOption.REPLACE_EXISTING);
      verifyWorktree(tree);
    } finally {
      removeWorktree(checkout, tree);
    }
  }
  private Captured startServer(Path d, List<String> c, int t) throws Exception {
    Captured s = Captured.start(d, c);
    try {
      s.awaitText("Done (", Math.addExact(timeout(t), 60));
      return s;
    } catch (Exception e) {
      s.kill();
      throw e;
    }
  }
  private String runGradle(Path d, List<String> c, int t) throws Exception {
    return Captured.run(d, c, timeout(t));
  }
  private int timeout(int t) {
    String extra = System.getenv("WORLDLINE_RUNTIME_TIMEOUT_EXTRA");
    return extra == null ? t : Math.addExact(t, Integer.parseInt(extra));
  }
  private void verifyBoundary() throws IOException {
    for (Path base :
        Arrays.asList(root.resolve("smokes/m74-complete-aero-census/runtime-src/worldline/m74"),
            smoke.resolve("runtime-src/worldline/m74"), smoke.resolve("runtime-src/worldline/m94")))
      try (Stream<Path> p = Files.walk(base)) {
        for (Path f : p.filter(x -> x.toString().endsWith(".java"))
                 .filter(x -> !x.toString().contains(File.separator + "client" + File.separator))
                 .filter(x -> !x.toString().contains(File.separator + "mixin" + File.separator))
                 .collect(Collectors.toList())) {
          String s = Files.readString(f);
          require(!s.contains("aero.modellib") && !s.contains("net.minecraft.client")
                  && !s.contains("org.lwjgl"),
              "server closure imports client code");
        }
      }
  }
  private void verifyCheckout(Path c) throws Exception {
    require(git(c, "remote", "get-url", "origin").trim().equals(value("aero.repository"))
            && git(c, "rev-parse", "HEAD").trim().equals(value("aero.revision"))
            && git(c, "status", "--porcelain", "--untracked-files=all").trim().isEmpty(),
        "Aero checkout drift");
  }
  private void addWorktree(Path c, Path t) throws Exception {
    Files.createDirectories(t.getParent());
    git(c, "worktree", "add", "--detach", t.toString(), value("aero.revision"));
    verifyWorktree(t);
  }
  private void verifyWorktree(Path t) throws Exception {
    require(git(t, "rev-parse", "HEAD").trim().equals(value("aero.revision"))
            && git(t, "status", "--porcelain", "--untracked-files=all").trim().isEmpty(),
        "worktree drift");
  }
  private void removeWorktree(Path c, Path t) {
    try {
      if (registered(c, t))
        try {
          git(c, "worktree", "remove", "--force", t.toString());
        } catch (Exception e) {
          if (registered(c, t))
            throw e;
        }
      if (Files.exists(t)) {
        Path allowed = root.resolve(".worldline/worktrees").normalize(),
             exact = t.toAbsolutePath().normalize();
        require(exact.startsWith(allowed) && !exact.equals(allowed), "unsafe remainder");
        try (Stream<Path> p = Files.walk(exact)) {
          for (Path f : p.sorted(Comparator.reverseOrder()).collect(Collectors.toList()))
            Files.deleteIfExists(f);
        }
      }
      Path parent = t.toAbsolutePath().normalize().getParent(),
           allowedParent = root.resolve(".worldline/worktrees").normalize();
      if (parent != null && parent.startsWith(allowedParent) && !parent.equals(allowedParent)
          && Files.isDirectory(parent)) {
        boolean empty;
        try (Stream<Path> p = Files.list(parent)) {
          empty = p.findAny().isEmpty();
        }
        if (empty)
          Files.deleteIfExists(parent);
      }
    } catch (Exception e) {
      throw new IllegalStateException("M94 cleanup failed " + t, e);
    }
  }
  private boolean registered(Path c, Path t) throws Exception {
    return Arrays.stream(git(c, "worktree", "list", "--porcelain").split("\\R"))
        .anyMatch(x
            -> x.startsWith("worktree ")
                && Paths.get(x.substring(9))
                    .toAbsolutePath()
                    .normalize()
                    .equals(t.toAbsolutePath().normalize()));
  }
  private String git(Path d, String... a) throws Exception {
    List<String> c = new ArrayList<>();
    c.add("git");
    c.add("-C");
    c.add(d.toString());
    c.addAll(Arrays.asList(a));
    return Captured.run(root, c, 60);
  }
  private String unique(String s, String p) {
    List<String> r = s.lines().filter(x -> x.startsWith(p)).collect(Collectors.toList());
    require(r.size() == 1, "marker drift " + p);
    return r.get(0).substring(p.length());
  }
  private int marker(String r, String n) {
    return Integer.parseInt(token(r, n));
  }
  private boolean sameCell(String r, int x, int y, int z, int nonce, int index) {
    return marker(r, "index") == index && marker(r, "x") == x && marker(r, "y") == y + (index & 3)
        && marker(r, "z") == z + (index >> 2) && marker(r, "nonce") == nonce;
  }
  private boolean ordered(String text, String[] markers) {
    int at = text.indexOf("[WorldlineCensus] scene ");
    for (String marker : markers) {
      int next = text.indexOf("[WorldlineRecovery] " + marker);
      if (next <= at)
        return false;
      at = next;
    }
    return true;
  }
  private String token(String r, String n) {
    for (String t : r.split(" +"))
      if (t.startsWith(n + "="))
        return t.substring(n.length() + 1);
    throw new IllegalStateException("missing " + n);
  }
  private long count(String s, String p) {
    return s.lines().filter(x -> x.startsWith(p)).count();
  }
  private int freePort() throws IOException {
    return SmokeSupport.freePort();
  }
  private void recreate(Path t) throws IOException {
    if (Files.exists(t))
      try (Stream<Path> p = Files.walk(t)) {
        for (Path f : p.sorted(Comparator.reverseOrder()).collect(Collectors.toList()))
          Files.delete(f);
      }
    Files.createDirectories(t);
  }
  private void load(Path p, Properties v) throws IOException {
    try (Reader r = Files.newBufferedReader(p)) {
      v.load(r);
    }
  }
  private String value(String k) {
    String r = config.getProperty(k);
    require(r != null && !r.trim().isEmpty(), "missing " + k);
    return r.trim();
  }
  private String diagnostic(String s) {
    return s.lines()
        .filter(x
            -> x.contains("WorldlineCensus") || x.contains("WorldlinePaged")
                || x.contains("WorldlineRecovery") || x.contains("Loading ")
                || x.contains("Exception") || x.contains("BUILD "))
        .collect(Collectors.joining("\n"));
  }
  private static String sha256(String v) throws Exception {
    return sha256(v.getBytes(StandardCharsets.UTF_8));
  }
  private static String sha256(byte[] v) throws Exception {
    return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(v));
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
  private static long q(long[] v, double q) {
    long[] c = v.clone();
    Arrays.sort(c);
    return c[(int) Math.ceil(c.length * q) - 1];
  }
  private static final class Plan {
    final int x, y, z;
    Plan(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }
  }
  private static final class Census {
    final int x, y, z, count;
    final long elapsed;
    final long[] frames;
    Census(int x, int y, int z, int n, long e, long[] f) {
      this.x = x;
      this.y = y;
      this.z = z;
      count = n;
      elapsed = e;
      frames = f;
    }
  }
  private static final class Recovery {
    final int x, y, z, expiry, expiredBefore, expiredAfter;
    final int[] requests, events;
    Recovery(int x, int y, int z, int expiry, int before, int after, int[] r, int[] e) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.expiry = expiry;
      expiredBefore = before;
      expiredAfter = after;
      requests = r;
      events = e;
    }
    int members(int i) {
      for (int at = 0; at < events.length; at++)
        if (i < events[at])
          return at < 6 ? 16 - at : 10 + at - 6;
      return 16;
    }
    int direct(int i) {
      return i >= events[4] && i < events[5] || i >= events[6] && i < events[7] ? 1 : 0;
    }
    int rebuild(int i) {
      for (int at : new int[] {0, 1, 2, 3, 7, 8, 9, 10, 11})
        if (i == events[at])
          return 1;
      return 0;
    }
    String requests() {
      return join(requests);
    }
    String events() {
      return join(events);
    }
    private String join(int[] v) {
      return Arrays.stream(v).mapToObj(String::valueOf).collect(Collectors.joining("/"));
    }
  }
  private static long[] before(long[] v, int index) {
    return Arrays.copyOf(v, index);
  }
  private static final class Paged {
    final int count, zeroFlush;
    final int[] events;
    final long[] renderer, queue, flush;
    Paged(int n, long[] r, long[] q, long[] f, int zero, int[] events) {
      count = n;
      renderer = r;
      queue = q;
      flush = f;
      zeroFlush = zero;
      this.events = events;
    }
    String summary() {
      long[] wr = before(renderer, events[0]), wq = before(queue, events[0]),
             wf = before(flush, events[0]);
      return "preRendererNs=" + q(wr, .5) + "/" + q(wr, .95) + "/" + q(wr, .99) + ",preEnqueueNs="
          + q(wq, .5) + "/" + q(wq, .95) + "/" + q(wq, .99) + ",preFlushNs=" + q(wf, .5) + "/"
          + q(wf, .95) + "/" + q(wf, .99) + ",eventNs=" + renderer[events[0]] + "/"
          + renderer[events[1]] + "/" + renderer[events[2]] + "/" + renderer[events[3]] + "/"
          + renderer[events[4]] + "/" + renderer[events[5]] + "/" + renderer[events[6]] + "/"
          + renderer[events[7]] + "/" + renderer[events[8]] + "/" + renderer[events[9]] + "/"
          + renderer[events[10]] + "/" + renderer[events[11]] + ",flushZero=" + zeroFlush;
    }
  }
  private static final class Arm {
    final int nonce, x, y, z, raw;
    final Census census;
    final Paged paged;
    final Recovery change;
    final String censusHash, pagedHash, changeHash;
    Arm(int n, int x, int y, int z, int raw, Census c, Paged p, Recovery m, String ch, String ph,
        String mh) {
      nonce = n;
      this.x = x;
      this.y = y;
      this.z = z;
      this.raw = raw;
      census = c;
      paged = p;
      change = m;
      censusHash = ch;
      pagedHash = ph;
      changeHash = mh;
    }
    String summary() {
      return "samples=" + census.count + ",requests=" + change.requests() + ",events="
          + change.events() + "," + paged.summary() + ",census=" + censusHash.substring(0, 12)
          + ",paged=" + pagedHash.substring(0, 12) + ",recovery=" + changeHash.substring(0, 12);
    }
  }
  private static final class Captured {
    final Process p;
    final String scope;
    final StringBuilder text = new StringBuilder();
    final Thread reader;
    int exit = -1;
    private Captured(Process p, Path d) {
      this.p = p;
      String a = d.toAbsolutePath().normalize().toString();
      scope =
          a.contains(File.separator + ".worldline" + File.separator + "worktrees" + File.separator)
          ? a.toLowerCase(Locale.ROOT)
          : "";
      reader = new Thread(() -> {
        try (BufferedReader in = new BufferedReader(
                 new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
          String l;
          while ((l = in.readLine()) != null)
            synchronized (text) {
              text.append(l).append('\n');
              text.notifyAll();
            }
        } catch (IOException e) {
        }
      });
      reader.setDaemon(true);
      reader.start();
    }
    static Captured start(Path d, List<String> c) throws IOException {
      return new Captured(
          new ProcessBuilder(c).directory(d.toFile()).redirectErrorStream(true).start(), d);
    }
    static String run(Path d, List<String> c, int t) throws Exception {
      Captured v = start(d, c);
      v.finish(t);
      require(v.exit == 0, "process failed\n" + v.output());
      return v.output();
    }
    void write(String v) throws IOException {
      p.getOutputStream().write(v.getBytes(StandardCharsets.UTF_8));
      p.getOutputStream().flush();
    }
    void awaitText(String v, int t) throws Exception {
      long end = System.currentTimeMillis() + t * 1000L;
      synchronized (text) {
        while (!text.toString().contains(v) && p.isAlive() && System.currentTimeMillis() < end)
          text.wait(100);
      }
      require(output().contains(v), "missing " + v + "\n" + output());
    }
    void finish(int t) throws Exception {
      if (!p.waitFor(t, TimeUnit.SECONDS)) {
        kill();
        throw new IllegalStateException("process timeout\n" + output());
      }
      exit = p.exitValue();
      reader.join(5000);
      require(exit == 0, "process exit " + exit + "\n" + output());
      if (!scope.isEmpty())
        kill();
    }
    void kill() {
      long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
      try {
        while (System.nanoTime() < end) {
          Set<Long> descendants =
              p.descendants().map(ProcessHandle::pid).collect(Collectors.toSet());
          List<ProcessHandle> victims = ProcessHandle.allProcesses()
                                            .filter(h -> descendants.contains(h.pid()) || scoped(h))
                                            .collect(Collectors.toList());
          victims.forEach(ProcessHandle::destroyForcibly);
          p.destroyForcibly();
          if (!p.isAlive() && victims.stream().noneMatch(ProcessHandle::isAlive))
            return;
          Thread.sleep(100);
        }
        throw new IllegalStateException("process tree kill timeout");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("process kill interrupted", e);
      }
    }
    private boolean scoped(ProcessHandle h) {
      return !scope.isEmpty()
          && h.info()
                 .commandLine()
                 .map(x -> x.toLowerCase(Locale.ROOT).contains(scope))
                 .orElse(false);
    }
    String output() {
      synchronized (text) {
        return text.toString();
      }
    }
  }
}
