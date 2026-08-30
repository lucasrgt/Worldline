package worldline.m770;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import org.lwjgl.opengl.ARBOcclusionQuery;
import org.lwjgl.opengl.ARBTimerQuery;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import worldline.profiling.ClientProfilerRuntime;

/** Nonblocking frame timer-query ring plus the deliberately intrusive finish probe. */
public final class GpuQueryCapture {
    private static final int MAGIC = 0x574c4751;
    private static final int TARGET = ARBTimerQuery.GL_TIME_ELAPSED;
    private static int[] ids;
    private static long[] slotSequences;
    private static boolean[] pending;
    private static long[] resultSequences;
    private static long[] resultNanos;
    private static int cursor;
    private static int openSlot = -1;
    private static int results;
    private static long issued;
    private static long skipped;
    private static boolean active;
    private static String arm;
    private static Path output;

    private GpuQueryCapture() {}

    public static void start() {
        require(!active && ids == null, "duplicate M770 GPU capture");
        require(GLContext.getCapabilities().GL_ARB_timer_query,
                "GL_ARB_timer_query is required for M770");
        arm = System.getProperty("worldline.m770.arm");
        output = Path.of(System.getProperty("worldline.m770.queryOutput")).toAbsolutePath();
        int ring = Math.max(8, Integer.getInteger("worldline.m770.queryRing", 64));
        int capacity = Math.max(1, Integer.getInteger("worldline.profiler.capacity", 150000));
        ids = new int[ring];
        slotSequences = new long[ring];
        pending = new boolean[ring];
        resultSequences = new long[capacity];
        resultNanos = new long[capacity];
        for (int index = 0; index < ring; index++) {
            ids[index] = ARBOcclusionQuery.glGenQueriesARB();
        }
        Display.setVSyncEnabled(arm.equals("finish-vsync"));
        active = true;
        System.out.println("[WorldlineM770] gpu-query-start arm=" + arm + " ring=" + ring);
    }

    public static void begin(long sequence) {
        if (!active) return;
        require(openSlot < 0, "nested M770 GPU query");
        int slot = cursor;
        cursor = (cursor + 1) % ids.length;
        if (pending[slot]) {
            if (!available(slot)) {
                skipped++;
                return;
            }
            collect(slot, false);
        }
        ARBOcclusionQuery.glBeginQueryARB(TARGET, ids[slot]);
        slotSequences[slot] = sequence;
        pending[slot] = true;
        openSlot = slot;
        issued++;
    }

    public static void end() {
        if (!active || openSlot < 0) return;
        ARBOcclusionQuery.glEndQueryARB(TARGET);
        openSlot = -1;
    }

    public static void beforeDisplayUpdate() {
        if (!active || arm.equals("query-async-off")) return;
        long started = System.nanoTime();
        GL11.glFinish();
        ClientProfilerRuntime.gpuWait(System.nanoTime() - started);
    }

    public static void finish() {
        require(active && openSlot < 0, "M770 GPU capture is not drainable");
        try {
            GL11.glFinish();
            for (int slot = 0; slot < ids.length; slot++) {
                if (pending[slot]) collect(slot, true);
                ARBOcclusionQuery.glDeleteQueriesARB(ids[slot]);
            }
            seal();
            Display.setVSyncEnabled(false);
            active = false;
            System.out.println("[WorldlineM770] gpu-query-sealed arm=" + arm
                    + " results=" + results + " issued=" + issued + " skipped=" + skipped);
        } catch (Exception error) {
            throw new IllegalStateException("M770 GPU query seal failed", error);
        }
    }

    private static boolean available(int slot) {
        return ARBOcclusionQuery.glGetQueryObjectiARB(ids[slot],
                ARBOcclusionQuery.GL_QUERY_RESULT_AVAILABLE_ARB) != 0;
    }

    private static void collect(int slot, boolean blocking) {
        require(blocking || available(slot), "M770 query result is not available");
        require(results < resultSequences.length, "M770 query result capacity exhausted");
        resultSequences[results] = slotSequences[slot];
        resultNanos[results] = ARBTimerQuery.glGetQueryObjectui64(ids[slot],
                ARBOcclusionQuery.GL_QUERY_RESULT_ARB);
        require(resultNanos[results] >= 0L, "negative M770 GPU duration");
        results++;
        pending[slot] = false;
    }

    private static void seal() throws Exception {
        Files.createDirectories(output.getParent());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        byte[] armBytes = arm.getBytes(StandardCharsets.US_ASCII);
        data.writeInt(MAGIC);
        data.writeInt(1);
        data.writeByte(armBytes.length);
        data.write(armBytes);
        data.writeInt(ids.length);
        data.writeLong(issued);
        data.writeLong(skipped);
        data.writeInt(results);
        for (int index = 0; index < results; index++) {
            data.writeLong(resultSequences[index]);
            data.writeLong(resultNanos[index]);
        }
        data.flush();
        byte[] body = bytes.toByteArray();
        bytes.write(MessageDigest.getInstance("SHA-256").digest(body));
        Files.write(output, bytes.toByteArray());
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
