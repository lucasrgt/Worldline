using System;
using System.Collections.Generic;
using System.Runtime.InteropServices;

// Queries Windows topology without changing any process or global scheduler setting.
public static class WorldlineBenchmarkTopology {
  [StructLayout(LayoutKind.Sequential)]
  public struct Info {
    public IntPtr Mask;
    public int Relation;
    public long Data0, Data1;
  }

  [DllImport("kernel32.dll", SetLastError = true)]
  private static extern bool GetLogicalProcessorInformation(IntPtr buffer, ref uint length);

  public static long Select(long available) {
    uint length = 0;
    GetLogicalProcessorInformation(IntPtr.Zero, ref length);
    if (length == 0) throw new InvalidOperationException("Processor topology absent");
    IntPtr buffer = Marshal.AllocHGlobal((int)length);
    try {
      if (!GetLogicalProcessorInformation(buffer, ref length))
        throw new InvalidOperationException("Processor topology query failed");
      int size = Marshal.SizeOf(typeof(Info));
      if (length % size != 0) throw new InvalidOperationException("Processor topology layout drift");
      var cores = new List<long>();
      for (int offset = 0; offset < length; offset += size) {
        var info = (Info)Marshal.PtrToStructure(IntPtr.Add(buffer, offset), typeof(Info));
        if (info.Relation == 0) cores.Add(info.Mask.ToInt64());
      }
      long selected = 0;
      int count = 0;
      for (int bit = 63; bit >= 0 && count < 4; bit--) {
        long candidate = 1L << bit;
        if ((available & candidate) == 0) continue;
        foreach (long core in cores) {
          if ((core & candidate) == 0 || (selected & core) != 0) continue;
          selected |= candidate;
          count++;
          break;
        }
      }
      if (count != 4) throw new InvalidOperationException("Four permitted physical cores are required");
      return selected;
    } finally {
      Marshal.FreeHGlobal(buffer);
    }
  }
}
