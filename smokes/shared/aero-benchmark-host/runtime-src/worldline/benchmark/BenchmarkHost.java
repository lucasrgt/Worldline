package worldline.benchmark;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/** Applies and verifies the scheduling profile of this benchmark process only. */
public final class BenchmarkHost {
  public static final String SIGNAL = "[Worldline benchmark host] logical=4 priority=High mask=";
  private BenchmarkHost() {}

  public static void prepare() {
    if (!System.getProperty("os.name", "").startsWith("Windows"))
      throw new IllegalStateException("The controlled Aero benchmark profile requires Windows");
    long pid = Long.parseLong(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
    String script = "$ErrorActionPreference='Stop';$ProgressPreference='SilentlyContinue';"
        + "$benchProcess=[Diagnostics.Process]::GetProcessById(" + pid + ");"
        + "$benchAvailable=$benchProcess.ProcessorAffinity.ToInt64();$benchMask=0L;$benchCount=0;"
        + "for($benchBit=63;$benchBit -ge 0 -and $benchCount -lt 4;$benchBit--){"
        + "$benchCandidate=1L -shl $benchBit;"
        + "if(($benchAvailable -band $benchCandidate) -ne 0){"
        + "$benchMask=$benchMask -bor $benchCandidate;$benchCount++}};"
        + "if($benchCount -ne 4){throw 'Four permitted logical processors are required'};"
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
}
