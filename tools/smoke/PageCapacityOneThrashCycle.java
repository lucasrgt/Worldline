import java.io.*;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.*;

/** Runs two fresh, same-plan real Aero cell-page timing replicas. */
public final class PageCapacityOneThrashCycle {
  private static final String ID = "m97-page-capacity-one-thrash";
  private final Path root = Paths.get("").toAbsolutePath().normalize(),
                     smoke = root.resolve("smokes").resolve(ID),
                     build = root.resolve(".worldline/smokes").resolve(ID);
  private final Properties config = new Properties();
  public static void main(String[] a) {
    if (!Arrays.equals(a, new String[] {ID})) {
      System.err.println("usage: java tools/smoke/PageCapacityOneThrashCycle.java " + ID);
      System.exit(2);
    }
    try {
      new PageCapacityOneThrashCycle().execute();
    } catch (Exception e) {
      System.err.println("Page-capacity-one thrash failed: " + e.getMessage());
      System.exit(1);
    }
  }
  private void execute() throws Exception {
    load(smoke.resolve("smoke.properties"), config);
    require(value("replicas").equals("2") && value("warmup.frames").equals("300")
            && value("warmup.millis").equals("5000") && value("minimum.frames").equals("720")
            && value("minimum.millis").equals("12000") && value("max.cached.pages").equals("1")
            && value("aero.rebuilds.per.frame").equals("8")
            && value("aero.page.ttl.frames").equals("100000") && value("fps.limit").equals("0")
            && value("aero.frame.pacing").equals("false"),
        "M97 design drift");
    Path checkout = root.resolve(value("aero.path")).normalize();
    verifyCheckout(checkout);
    verifyBoundary();
    recreate(build);
    buildAero(checkout);
    boolean diagnostic = Boolean.getBoolean("worldline.m97.diagnostic");
    int runs = diagnostic ? 1 : 2, nonce = 9709701;
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
      System.out.println("M97 diagnostic replica passed; qualification not attempted");
      System.out.println("  " + arms.get(0).summary());
      return;
    }
    Arm first = arms.get(0), second = arms.get(1);
    require(first.nonce == second.nonce && first.x == second.x && first.y == second.y
            && first.z == second.z && first.raw == second.raw
            && first.paged.pages == second.paged.pages,
        "replica fixture/page drift");
    String trace =
        "v1|design=2-fresh-same-plan-nonce-four-page-capacity-one-thrash-replicas|fixture=constant16-synced+client-marker+exact-camera|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|path=enqueue16+flush2+maxCached1+cachedPages1+pageCalls4+direct0+rebuild4+evictedDelta4-per-record|window=M74-min720intervals+12s|capture=primitive-timers+page-counters+cumulative-evictions+post-seal-sidecar|per-record=M74-render0/list0/identity16+state16/maskffff|stats=descriptive-paged-spans-dynamic|uninstrumented-cost-causality-regression-historical-lag=not-claimed|shutdown=clean";
    String signature = sha256(trace);
    require(signature.equals(value("expected.signature")), "M97 signature drift: " + signature);
    Files.writeString(build.resolve("evidence.txt"),
        "id=" + ID + "\nreplicas=2\nserver.jvm=2\nclient.jvm=2\nfirst=" + first.summary()
            + "\nsecond=" + second.summary() + "\ntrace=" + trace + "\nsignature=" + signature
            + "\n",
        StandardCharsets.UTF_8);
    System.out.println("M97 page-capacity-one thrash passed");
    System.out.println("  first: " + first.summary());
    System.out.println("  second: " + second.summary());
    System.out.println("  signature: " + signature);
  }
  private Arm run(Path checkout, Path workspace, int replica, int nonce, Plan plan)
      throws Exception {
    Files.createDirectories(workspace);
    int port = freePort(), timeout = Integer.parseInt(value("timeout.seconds"));
    Path base = root.resolve(".worldline/worktrees/m97-" + ProcessHandle.current().pid() + "-"
             + replica + "-" + System.nanoTime()),
         serverTree = base.resolve("server"), clientTree = base.resolve("client");
    Captured server = null;
    try {
      addWorktree(checkout, serverTree);
      addWorktree(checkout, clientTree);
      Path init = root.resolve(value("runner")), serverGame = workspace.resolve("server"),
           clientGame = workspace.resolve("client"), censusFile = clientGame.resolve("census.bin"),
           pagedFile = clientGame.resolve("paged.bin");
      server = startServer(serverTree.resolve("stationapi/test-bare"),
          command(serverTree, init, "server", serverGame, port, nonce, null, null, plan), timeout);
      String client = runGradle(clientTree.resolve("stationapi/test-bare"),
          command(clientTree, init, "client", clientGame, port, nonce, censusFile, pagedFile, null),
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
              && count(client, "[WorldlinePaged] complete ") == 1,
          "client lifecycle drift");
      Census census = parseCensus(censusFile, nonce);
      Paged paged = parsePaged(pagedFile, census, nonce);
      String complete = unique(client, "[WorldlinePaged] complete ");
      require(marker(complete, "samples") == paged.count
              && marker(complete, "bytes") == Files.size(pagedFile)
              && token(complete, "sha256").equals(sha256(Files.readAllBytes(pagedFile))),
          "paged marker drift");
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
      String scene = unique(serverText, "[WorldlineCensus] scene ");
      require(token(scene, "mode").equals("present") && marker(scene, "planned") == 16
              && marker(scene, "placed") == 16 && marker(scene, "yaw") == -90
              && marker(scene, "pitch") == 0 && marker(scene, "nonce") == nonce,
          "scene drift: " + scene);
      int x = marker(scene, "x"), y = marker(scene, "baseY"), z = marker(scene, "baseZ"),
          raw = marker(scene, "raw");
      require((plan == null || plan.x == x && plan.y == y && plan.z == z) && census.x == x
              && census.y == y && census.z == z,
          "replica plan drift");
      require(serverText.indexOf("[WorldlineCensus] activation ")
                  < serverText.indexOf("[WorldlineCensus] tracking-ready ")
              && serverText.indexOf("[WorldlineCensus] tracking-ready ")
                  < serverText.indexOf("[WorldlineCensus] scene ")
              && serverText.contains(value("username") + " lost connection")
              && serverText.contains("Stopping server"),
          "server lifecycle drift");
      verifyWorktree(serverTree);
      verifyWorktree(clientTree);
      return new Arm(nonce, x, y, z, raw, census, paged, sha256(Files.readAllBytes(censusFile)),
          sha256(Files.readAllBytes(pagedFile)));
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
  private Census parseCensus(Path file, int nonce) throws Exception {
    byte[] bytes = Files.readAllBytes(file);
    try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
      require(in.readInt() == 0x574c3734 && in.readInt() == 1 && in.readInt() == 28
              && in.readInt() == 16 && in.readInt() == nonce,
          "census schema drift");
      int x = in.readInt(), y = in.readInt(), z = in.readInt();
      require(in.readInt() == 720 && in.readLong() == 12_000_000_000L, "census window drift");
      int count = in.readInt();
      long elapsed = in.readLong();
      require(count >= 720 && count <= 65536 && bytes.length == 56L + count * 28L,
          "census length drift");
      long sum = 0;
      long[] frames = new long[count];
      for (int i = 0; i < count; i++) {
        long delta = in.readLong();
        int renders = in.readInt(), lists = in.readInt(), visible = in.readInt(),
            calls = in.readInt(), state = in.readUnsignedShort(), mask = in.readUnsignedShort();
        require(delta > 0 && renders == 0 && lists == 0 && visible > 0 && calls == 16
                && state == 0x1010 && mask == 0xffff,
            "paged census record drift at " + i + " values=" + renders + "/" + lists + "/" + calls);
        frames[i] = delta;
        sum = Math.addExact(sum, delta);
      }
      require(in.read() == -1 && sum == elapsed && elapsed >= 12_000_000_000L,
          "census aggregate drift");
      return new Census(x, y, z, count, elapsed, frames);
    }
  }
  private Paged parsePaged(Path file, Census census, int nonce) throws Exception {
    byte[] bytes = Files.readAllBytes(file);
    try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
      require(in.readInt() == 0x574c3937 && in.readInt() == 1 && in.readInt() == 44
              && in.readInt() == 56 && in.readInt() == nonce,
          "paged schema drift");
      int x = in.readInt(), y = in.readInt(), z = in.readInt(), count = in.readInt();
      long elapsed = in.readLong();
      require(x == census.x && y == census.y && z == census.z && count == census.count
              && elapsed == census.elapsed && bytes.length == 44L + count * 56L,
          "paged/census identity drift");
      long[] renderer = new long[count], queue = new long[count], flush = new long[count];
      int pages = 1, zeroFlush = 0, firstEvicted = -1, priorEvicted = -1, rebuild3 = 0,
          rebuild4 = 0;
      long flushSum = 0;
      for (int i = 0; i < count; i++) {
        renderer[i] = in.readLong();
        queue[i] = in.readLong();
        flush[i] = in.readLong();
        int rc = in.readUnsignedShort(), qc = in.readUnsignedShort(), fc = in.readUnsignedShort(),
            reserved = in.readUnsignedShort(), queued = in.readInt(), pageCalls = in.readInt(),
            direct = in.readInt(), rebuilds = in.readInt(), cached = in.readInt(),
            evicted = in.readInt();
        require(renderer[i] > 0 && queue[i] > 0 && flush[i] >= 0 && renderer[i] >= queue[i]
                && rc == 16 && qc == 16 && fc == 2 && reserved == 0 && queued == 16
                && pageCalls == 4 && direct == 0 && rebuilds == 4 && cached == pages && evicted > 0
                && (priorEvicted < 0 || evicted == priorEvicted + 4),
            "capacity record drift at " + i + " time=" + renderer[i] + "/" + queue[i] + "/"
                + flush[i] + " calls=" + rc + "/" + qc + "/" + fc + " state=" + queued + "/"
                + pageCalls + "/" + direct + "/" + rebuilds + "/" + cached + " evicted=" + evicted
                + " prior=" + priorEvicted);
        if (rebuilds == 3)
          rebuild3++;
        else
          rebuild4++;
        if (firstEvicted < 0)
          firstEvicted = evicted;
        priorEvicted = evicted;
        if (flush[i] == 0)
          zeroFlush++;
        flushSum = Math.addExact(flushSum, flush[i]);
      }
      require(in.read() == -1 && flushSum > 0, "paged aggregate drift");
      return new Paged(count, pages, renderer, queue, flush, zeroFlush, firstEvicted, priorEvicted,
          rebuild3, rebuild4);
    }
  }
  private List<String> command(Path tree, Path init, String role, Path game, int port, int nonce,
      Path census, Path paged, Plan plan) {
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
          "-PworldlineMaxCachedPages=" + value("max.cached.pages"),
          "-PworldlineRebuildsPerFrame=" + value("aero.rebuilds.per.frame"),
          "-PworldlinePageTtl=" + value("aero.page.ttl.frames"),
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
    Path tree = root.resolve(".worldline/worktrees/m97-build-" + System.nanoTime());
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
            smoke.resolve("runtime-src/worldline/m74")))
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
      throw new IllegalStateException("M97 cleanup failed " + t, e);
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
                || x.contains("Loading ") || x.contains("Exception") || x.contains("BUILD "))
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
  private static final class Paged {
    final int count, pages, zeroFlush, firstEvicted, lastEvicted, rebuild3, rebuild4;
    final long[] renderer, queue, flush;
    Paged(
        int n, int p, long[] r, long[] q, long[] f, int zero, int first, int last, int r3, int r4) {
      count = n;
      pages = p;
      renderer = r;
      queue = q;
      flush = f;
      zeroFlush = zero;
      firstEvicted = first;
      lastEvicted = last;
      rebuild3 = r3;
      rebuild4 = r4;
    }
    String summary() {
      return "pages=" + pages + ",rebuild3=" + rebuild3 + ",rebuild4=" + rebuild4
          + ",evicted=" + firstEvicted + ".." + lastEvicted + ",rendererNs=" + q(renderer, .5) + "/"
          + q(renderer, .95) + "/" + q(renderer, .99) + "/"
          + Arrays.stream(renderer).max().orElseThrow() + ",enqueueNs=" + q(queue, .5) + "/"
          + q(queue, .95) + "/" + q(queue, .99) + "/" + Arrays.stream(queue).max().orElseThrow()
          + ",flushNs=" + q(flush, .5) + "/" + q(flush, .95) + "/" + q(flush, .99) + "/"
          + Arrays.stream(flush).max().orElseThrow() + ",flushZero=" + zeroFlush;
    }
  }
  private static final class Arm {
    final int nonce, x, y, z, raw;
    final Census census;
    final Paged paged;
    final String censusHash, pagedHash;
    Arm(int n, int x, int y, int z, int raw, Census c, Paged p, String ch, String ph) {
      nonce = n;
      this.x = x;
      this.y = y;
      this.z = z;
      this.raw = raw;
      census = c;
      paged = p;
      censusHash = ch;
      pagedHash = ph;
    }
    String summary() {
      return "samples=" + census.count + "," + paged.summary()
          + ",census=" + censusHash.substring(0, 12) + ",paged=" + pagedHash.substring(0, 12);
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
