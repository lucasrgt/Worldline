package worldline.benchmark;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/** Applies and verifies the scheduling profile of this benchmark process only. */
public final class BenchmarkHost {
  public static final String SIGNAL = "[Worldline benchmark host] logical=4 physical=4 vm=4 priority=High mask=";
  private BenchmarkHost() {}

  public static void prepare() {
    if (!System.getProperty("os.name", "").startsWith("Windows"))
      throw new IllegalStateException("The controlled Aero benchmark profile requires Windows");
    if (Runtime.getRuntime().availableProcessors() != 4)
      throw new IllegalStateException("The benchmark JVM requires ActiveProcessorCount=4 at startup");
    long pid = Long.parseLong(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
    String script = "$ErrorActionPreference='Stop';$ProgressPreference='SilentlyContinue';"
        + "Add-Type -TypeDefinition ([Text.Encoding]::UTF8.GetString([Convert]::FromBase64String('"
        + topology() + "')));"
        + "$benchProcess=[Diagnostics.Process]::GetProcessById(" + pid + ");"
        + "$benchMask=[WorldlineBenchmarkTopology]::Select($benchProcess.ProcessorAffinity.ToInt64());"
        + "$benchProcess.ProcessorAffinity=[IntPtr]$benchMask;"
        + "$benchProcess.PriorityClass=[Diagnostics.ProcessPriorityClass]::High;$benchProcess.Refresh();"
        + "if($benchProcess.ProcessorAffinity.ToInt64() -ne $benchMask -or "
        + "$benchProcess.PriorityClass -ne [Diagnostics.ProcessPriorityClass]::High){"
        + "throw 'Benchmark scheduling profile was not applied'};"
        + "Write-Output ('" + SIGNAL + "'+$benchMask+' pid='+$benchProcess.Id)";
    String encoded = Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));
    try {
      Process process = new ProcessBuilder("powershell.exe", "-NoLogo", "-NoProfile", "-NonInteractive",
          "-WindowStyle", "Hidden", "-EncodedCommand", encoded).redirectErrorStream(true).start();
      if (!process.waitFor(15, TimeUnit.SECONDS)) {
        process.destroyForcibly();
        throw new IllegalStateException("Benchmark scheduling setup timed out");
      }
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (InputStream input = process.getInputStream()) {
        byte[] buffer = new byte[1024];
        for (int count; (count = input.read(buffer)) >= 0;) bytes.write(buffer, 0, count);
      }
      String output = new String(bytes.toByteArray(), StandardCharsets.UTF_8).trim();
      if (process.exitValue() != 0 || !output.startsWith(SIGNAL))
        throw new IllegalStateException("Benchmark scheduling setup failed: " + output);
      System.out.println(output);
    } catch (Exception error) {
      throw new IllegalStateException("Cannot establish the controlled benchmark host", error);
    }
  }

  private static String topology() {
    try (InputStream input = BenchmarkHost.class.getResourceAsStream("BenchmarkTopology.cs")) {
      if (input == null)
        throw new IllegalStateException("Benchmark topology resource is absent");
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      for (int count; (count = input.read(buffer)) >= 0;) bytes.write(buffer, 0, count);
      return Base64.getEncoder().encodeToString(bytes.toByteArray());
    } catch (Exception error) {
      throw new IllegalStateException("Cannot load benchmark topology query", error);
    }
  }
}
