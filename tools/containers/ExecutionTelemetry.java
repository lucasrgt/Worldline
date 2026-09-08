/** Sanitized observational metrics retained after native receipt verification. */
record ExecutionTelemetry(String executorVersion, String backend, String termination,
        long elapsedMs, long bootstrapMs, long runtimeMs, Long peakMemoryBytes,
        String memoryMeasurement, Long peakProcesses, Long peakTasks, Long cpuUser100ns,
        Long cpuKernel100ns, Long ioReadBytes, Long ioWriteBytes, Long memoryLimitEvents,
        Long oomKillEvents, Long taskLimitEvents, Long cpuThrottled100ns,
        long stdoutObservedBytes, long stderrObservedBytes, boolean stdoutTruncated,
        boolean stderrTruncated) { }
