using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Runtime.InteropServices;
using System.Text;

// Native Windows launcher. It assigns the suspended child to a Job Object before any user code runs.
internal static class WindowsJobRunner
{
    private const uint CreateSuspended = 0x00000004;
    private const uint CreateNoWindow = 0x08000000;
    private const uint Infinite = 0xffffffff;
    private const uint WaitTimeout = 0x00000102;
    private const uint GenericWrite = 0x40000000;
    private const uint ShareRead = 0x00000001;
    private const uint CreateAlways = 2;
    private const uint LimitActiveProcess = 0x00000008;
    private const uint LimitJobMemory = 0x00000200;
    private const uint LimitKillOnClose = 0x00002000;
    private const uint CpuEnable = 0x00000001;
    private const uint CpuHardCap = 0x00000004;

    public static int Main(string[] args)
    {
        try { return Run(Options.Parse(args)); }
        catch (Exception error) { Console.Error.WriteLine("windows job runner failed: " + error.Message); return 125; }
    }

    private static int Run(Options options)
    {
        Directory.CreateDirectory(Path.GetDirectoryName(Path.GetFullPath(options.Log)));
        IntPtr job = CreateJobObject(IntPtr.Zero, null); Check(job != IntPtr.Zero, "CreateJobObject");
        IntPtr log = CreateFile(options.Log, GenericWrite, ShareRead, IntPtr.Zero, CreateAlways, 0, IntPtr.Zero);
        Check(log.ToInt64() != -1, "CreateFile"); Check(SetHandleInformation(log, 1, 1), "SetHandleInformation");
        PROCESS_INFORMATION process = new PROCESS_INFORMATION();
        try
        {
            ConfigureJob(job, options);
            SECURITY_ATTRIBUTES security = new SECURITY_ATTRIBUTES();
            security.nLength = Marshal.SizeOf(typeof(SECURITY_ATTRIBUTES)); security.bInheritHandle = true;
            STARTUPINFO start = new STARTUPINFO(); start.cb = Marshal.SizeOf(typeof(STARTUPINFO));
            start.dwFlags = 0x00000100; start.hStdOutput = log; start.hStdError = log;
            start.hStdInput = GetStdHandle(-10);
            StringBuilder command = new StringBuilder(CommandLine(options.Command));
            bool created = CreateProcess(null, command, ref security, ref security, true,
                    CreateSuspended | CreateNoWindow, IntPtr.Zero, options.WorkingDirectory, ref start, out process);
            Check(created, "CreateProcess"); Check(AssignProcessToJobObject(job, process.hProcess), "AssignProcessToJobObject");
            Check(ResumeThread(process.hThread) != 0xffffffff, "ResumeThread");
            uint wait = WaitForSingleObject(process.hProcess, options.TimeoutSeconds <= 0
                    ? Infinite : checked((uint)options.TimeoutSeconds * 1000));
            bool timedOut = wait == WaitTimeout;
            if (timedOut) Check(TerminateJobObject(job, 124), "TerminateJobObject");
            else Check(wait == 0, "WaitForSingleObject");
            WaitForSingleObject(process.hProcess, 10000);
            uint exitCode; Check(GetExitCodeProcess(process.hProcess, out exitCode), "GetExitCodeProcess");
            WriteMetrics(job, options, timedOut, exitCode);
            return timedOut ? 124 : unchecked((int)exitCode);
        }
        finally
        {
            if (process.hThread != IntPtr.Zero) CloseHandle(process.hThread);
            if (process.hProcess != IntPtr.Zero) CloseHandle(process.hProcess);
            CloseHandle(log); CloseHandle(job);
        }
    }

    private static void ConfigureJob(IntPtr job, Options options)
    {
        JOBOBJECT_EXTENDED_LIMIT_INFORMATION limits = new JOBOBJECT_EXTENDED_LIMIT_INFORMATION();
        limits.BasicLimitInformation.LimitFlags = LimitKillOnClose | LimitActiveProcess | LimitJobMemory;
        limits.BasicLimitInformation.ActiveProcessLimit = checked((uint)options.ActiveProcesses);
        limits.JobMemoryLimit = new UIntPtr(checked((ulong)options.MemoryBytes));
        Set(job, 9, limits);
        JOBOBJECT_CPU_RATE_CONTROL_INFORMATION cpu = new JOBOBJECT_CPU_RATE_CONTROL_INFORMATION();
        cpu.ControlFlags = CpuEnable | CpuHardCap; cpu.CpuRate = checked((uint)options.CpuRate);
        Set(job, 15, cpu);
    }

    private static void WriteMetrics(IntPtr job, Options options, bool timedOut, uint exitCode)
    {
        JOBOBJECT_EXTENDED_LIMIT_INFORMATION limits = Query<JOBOBJECT_EXTENDED_LIMIT_INFORMATION>(job, 9);
        JOBOBJECT_BASIC_ACCOUNTING_INFORMATION accounting = Query<JOBOBJECT_BASIC_ACCOUNTING_INFORMATION>(job, 1);
        Directory.CreateDirectory(Path.GetDirectoryName(Path.GetFullPath(options.Metrics)));
        using (StreamWriter writer = new StreamWriter(options.Metrics, false, new UTF8Encoding(false)))
        {
            writer.WriteLine("format=1"); writer.WriteLine("backend=windows-job");
            writer.WriteLine("timed.out=" + timedOut.ToString().ToLowerInvariant());
            writer.WriteLine("exit.code=" + exitCode.ToString(CultureInfo.InvariantCulture));
            writer.WriteLine("processes.total=" + accounting.TotalProcesses.ToString(CultureInfo.InvariantCulture));
            writer.WriteLine("processes.terminated=" + accounting.TotalTerminatedProcesses.ToString(CultureInfo.InvariantCulture));
            writer.WriteLine("memory.peak.bytes=" + limits.PeakJobMemoryUsed.ToUInt64().ToString(CultureInfo.InvariantCulture));
            writer.WriteLine("user.time.100ns=" + accounting.TotalUserTime.ToString(CultureInfo.InvariantCulture));
            writer.WriteLine("kernel.time.100ns=" + accounting.TotalKernelTime.ToString(CultureInfo.InvariantCulture));
        }
    }

    private static void Set<T>(IntPtr job, int infoClass, T value) where T : struct
    {
        int size = Marshal.SizeOf(typeof(T)); IntPtr pointer = Marshal.AllocHGlobal(size);
        try { Marshal.StructureToPtr(value, pointer, false); Check(SetInformationJobObject(job, infoClass, pointer, (uint)size), "SetInformationJobObject"); }
        finally { Marshal.FreeHGlobal(pointer); }
    }
    private static T Query<T>(IntPtr job, int infoClass) where T : struct
    {
        int size = Marshal.SizeOf(typeof(T)); IntPtr pointer = Marshal.AllocHGlobal(size); uint written;
        try { Check(QueryInformationJobObject(job, infoClass, pointer, (uint)size, out written), "QueryInformationJobObject");
            return (T)Marshal.PtrToStructure(pointer, typeof(T)); }
        finally { Marshal.FreeHGlobal(pointer); }
    }
    private static string CommandLine(IList<string> command)
    {
        StringBuilder value = new StringBuilder();
        foreach (string item in command) { if (value.Length > 0) value.Append(' '); value.Append(Quote(item)); }
        return value.ToString();
    }
    private static string Quote(string value)
    {
        if (value.Length > 0 && value.IndexOfAny(new[] {' ', '\t', '"'}) < 0) return value;
        StringBuilder output = new StringBuilder("\""); int slashes = 0;
        foreach (char character in value) { if (character == '\\') { slashes++; continue; }
            if (character == '"') output.Append('\\', slashes * 2 + 1); else output.Append('\\', slashes);
            slashes = 0; output.Append(character); }
        output.Append('\\', slashes * 2); return output.Append('"').ToString();
    }
    private static void Check(bool value, string operation) { if (!value) throw new Win32Exception(Marshal.GetLastWin32Error(), operation); }

    private sealed class Options
    {
        internal long MemoryBytes; internal int CpuRate, ActiveProcesses, TimeoutSeconds;
        internal string WorkingDirectory, Log, Metrics; internal List<string> Command;
        internal static Options Parse(string[] args)
        {
            Options value = new Options(); int index = 0;
            while (index < args.Length && args[index] != "--") { string key = args[index++];
                if (index >= args.Length) throw new ArgumentException("missing value for " + key); string item = args[index++];
                if (key == "--memory") value.MemoryBytes = long.Parse(item, CultureInfo.InvariantCulture);
                else if (key == "--cpu-rate") value.CpuRate = int.Parse(item, CultureInfo.InvariantCulture);
                else if (key == "--active-processes") value.ActiveProcesses = int.Parse(item, CultureInfo.InvariantCulture);
                else if (key == "--timeout-seconds") value.TimeoutSeconds = int.Parse(item, CultureInfo.InvariantCulture);
                else if (key == "--cwd") value.WorkingDirectory = item; else if (key == "--log") value.Log = item;
                else if (key == "--metrics") value.Metrics = item; else throw new ArgumentException("unknown option " + key); }
            if (index >= args.Length || args[index++] != "--") throw new ArgumentException("missing -- command separator");
            value.Command = new List<string>(); while (index < args.Length) value.Command.Add(args[index++]);
            if (value.MemoryBytes < 64L * 1024 * 1024 || value.CpuRate < 1 || value.CpuRate > 10000
                    || value.ActiveProcesses < 1 || value.Command.Count == 0 || String.IsNullOrEmpty(value.WorkingDirectory)
                    || String.IsNullOrEmpty(value.Log) || String.IsNullOrEmpty(value.Metrics)) throw new ArgumentException("invalid limits or paths");
            return value;
        }
    }

    [StructLayout(LayoutKind.Sequential)] private struct SECURITY_ATTRIBUTES { internal int nLength; internal IntPtr lpSecurityDescriptor; [MarshalAs(UnmanagedType.Bool)] internal bool bInheritHandle; }
    [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Unicode)] private struct STARTUPINFO { internal int cb; internal string lpReserved, lpDesktop, lpTitle; internal int dwX, dwY, dwXSize, dwYSize, dwXCountChars, dwYCountChars, dwFillAttribute, dwFlags; internal short wShowWindow, cbReserved2; internal IntPtr lpReserved2, hStdInput, hStdOutput, hStdError; }
    [StructLayout(LayoutKind.Sequential)] private struct PROCESS_INFORMATION { internal IntPtr hProcess, hThread; internal uint dwProcessId, dwThreadId; }
    [StructLayout(LayoutKind.Sequential)] private struct IO_COUNTERS { internal ulong ReadOperationCount, WriteOperationCount, OtherOperationCount, ReadTransferCount, WriteTransferCount, OtherTransferCount; }
    [StructLayout(LayoutKind.Sequential)] private struct JOBOBJECT_BASIC_LIMIT_INFORMATION { internal long PerProcessUserTimeLimit, PerJobUserTimeLimit; internal uint LimitFlags; internal UIntPtr MinimumWorkingSetSize, MaximumWorkingSetSize; internal uint ActiveProcessLimit; internal UIntPtr Affinity; internal uint PriorityClass, SchedulingClass; }
    [StructLayout(LayoutKind.Sequential)] private struct JOBOBJECT_EXTENDED_LIMIT_INFORMATION { internal JOBOBJECT_BASIC_LIMIT_INFORMATION BasicLimitInformation; internal IO_COUNTERS IoInfo; internal UIntPtr ProcessMemoryLimit, JobMemoryLimit, PeakProcessMemoryUsed, PeakJobMemoryUsed; }
    [StructLayout(LayoutKind.Sequential)] private struct JOBOBJECT_CPU_RATE_CONTROL_INFORMATION { internal uint ControlFlags, CpuRate; }
    [StructLayout(LayoutKind.Sequential)] private struct JOBOBJECT_BASIC_ACCOUNTING_INFORMATION { internal long TotalUserTime, TotalKernelTime, ThisPeriodTotalUserTime, ThisPeriodTotalKernelTime; internal uint TotalPageFaultCount, TotalProcesses, ActiveProcesses, TotalTerminatedProcesses; }

    [DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)] private static extern IntPtr CreateJobObject(IntPtr attributes, string name);
    [DllImport("kernel32.dll", SetLastError = true)] private static extern bool SetInformationJobObject(IntPtr job, int infoClass, IntPtr info, uint length);
    [DllImport("kernel32.dll", SetLastError = true)] private static extern bool QueryInformationJobObject(IntPtr job, int infoClass, IntPtr info, uint length, out uint written);
    [DllImport("kernel32.dll", SetLastError = true)] private static extern bool AssignProcessToJobObject(IntPtr job, IntPtr process);
    [DllImport("kernel32.dll", SetLastError = true)] private static extern bool TerminateJobObject(IntPtr job, uint exitCode);
    [DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)] private static extern bool CreateProcess(string app, StringBuilder command, ref SECURITY_ATTRIBUTES processAttributes, ref SECURITY_ATTRIBUTES threadAttributes, bool inherit, uint flags, IntPtr environment, string currentDirectory, ref STARTUPINFO start, out PROCESS_INFORMATION process);
    [DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)] private static extern IntPtr CreateFile(string name, uint access, uint share, IntPtr security, uint creation, uint flags, IntPtr template);
    [DllImport("kernel32.dll")] private static extern IntPtr GetStdHandle(int handle);
    [DllImport("kernel32.dll", SetLastError = true)] private static extern uint ResumeThread(IntPtr thread);
    [DllImport("kernel32.dll", SetLastError = true)] private static extern uint WaitForSingleObject(IntPtr handle, uint milliseconds);
    [DllImport("kernel32.dll", SetLastError = true)] private static extern bool GetExitCodeProcess(IntPtr process, out uint exitCode);
    [DllImport("kernel32.dll", SetLastError = true)] private static extern bool CloseHandle(IntPtr handle);
    [DllImport("kernel32.dll", SetLastError = true)] private static extern bool SetHandleInformation(IntPtr handle, uint mask, uint flags);
}
