package worldline.m782;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** External Java 8 scheduler soak with deterministic transition-state inspection. */
public final class ChunkSchedulerSoakProbe {
    private static final int EPOCHS = 256;
    private static final int HIDDEN = 128;
    private static final int ARRIVAL_FRAMES = 512;
    private static final int TRANSITION_PENDING = 16;
    private static final int MAXIMUM_HIDDEN_WAIT = 160;

    private ChunkSchedulerSoakProbe() {}

    public static void main(String[] arguments) throws Exception {
        require(arguments.length == 0, "probe takes no arguments");
        Class<?> schedulerType = Class.forName(
                "aero.modellib.render.Aero_ChunkWorkScheduler");
        Class<?> adapterType = Class.forName(
                "aero.modellib.render.Aero_ChunkWorkScheduler$Adapter");
        Object scheduler = schedulerType.getConstructor().newInstance();
        Method schedule = schedulerType.getMethod("schedule", List.class,
                adapterType, int.class, int.class, int.class);
        Method reset = schedulerType.getMethod("reset");
        Method built = schedulerType.getMethod("built");
        SoakAdapter handler = new SoakAdapter();
        Object adapter = Proxy.newProxyInstance(adapterType.getClassLoader(),
                new Class<?>[] {adapterType}, handler);
        long frames = 0L;
        long rebuilds = 0L;
        int maximumWait = 0;

        for (int epoch = 0; epoch < EPOCHS; epoch++) {
            List<Work> queue = new ArrayList<Work>();
            List<Work> hidden = new ArrayList<Work>();
            for (int index = 0; index < HIDDEN; index++) {
                Work work = new Work(epoch * 10000 + index, false, 0);
                hidden.add(work);
                queue.add(work);
            }
            int frame = 0;
            while (frame < ARRIVAL_FRAMES || !queue.isEmpty()) {
                if (frame < ARRIVAL_FRAMES) queue.add(
                        new Work(epoch * 10000 + HIDDEN + frame, true, frame));
                handler.frame = frame;
                int count = ((Integer) schedule.invoke(
                        scheduler, queue, adapter, 1, 120, 30)).intValue();
                require(count == 1, "one-rebuild budget drift");
                frame++;
                frames++;
                rebuilds++;
            }
            require(frame == HIDDEN + ARRIVAL_FRAMES, "epoch drain drift");
            for (Work work : hidden) {
                require(work.built >= 0, "hidden work starved");
                maximumWait = Math.max(maximumWait, work.built - work.created);
            }
            require(maximumWait <= MAXIMUM_HIDDEN_WAIT, "hidden wait bound drift");
            rebuilds += transition(reset, schedule, built, scheduler, adapter,
                    schedulerType, handler, epoch);
        }

        String output = "M782_SOAK=epochs=" + EPOCHS + ";frames=" + frames
                + ";rebuilt=" + rebuilds + ";maxHiddenWait=" + maximumWait
                + ";transitions=" + EPOCHS + ";pendingBeforeReset=" + TRANSITION_PENDING
                + ";statesAfterReset=0;queueAfterReset=null;invocationAfterReset=0";
        System.out.println(output);
    }

    private static int transition(Method reset, Method schedule, Method built,
            Object scheduler, Object adapter, Class<?> schedulerType,
            SoakAdapter handler, int epoch) throws Exception {
        List<Work> pending = new ArrayList<Work>();
        for (int index = 0; index < TRANSITION_PENDING; index++)
            pending.add(new Work(epoch * 10000 + 9000 + index, false, 0));
        pending.add(new Work(epoch * 10000 + 9999, true, 0));
        handler.frame = 0;
        schedule.invoke(scheduler, pending, adapter, 1, 120, 30);
        require(stateCount(schedulerType, scheduler) == TRANSITION_PENDING,
                "pending transition state absent");
        reset.invoke(scheduler);
        require(stateCount(schedulerType, scheduler) == 0, "reset retained states");
        require(field(schedulerType, scheduler, "activeQueue") == null,
                "reset retained queue");
        require(((Integer) field(schedulerType, scheduler, "invocation")).intValue() == 0,
                "reset retained invocation");
        require(((Integer) built.invoke(scheduler)).intValue() == 0,
                "reset retained metrics");
        return 1;
    }

    private static int stateCount(Class<?> type, Object value) throws Exception {
        return ((Map<?, ?>) field(type, value, "states")).size();
    }

    private static Object field(Class<?> type, Object value, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(value);
    }

    private static final class Work {
        final int id;
        final boolean visible;
        final int created;
        boolean dirty = true;
        int built = -1;

        Work(int id, boolean visible, int created) {
            this.id = id;
            this.visible = visible;
            this.created = created;
        }
    }

    private static final class SoakAdapter implements InvocationHandler {
        int frame;

        public Object invoke(Object proxy, Method method, Object[] arguments) {
            String name = method.getName();
            Work work = arguments == null || arguments.length == 0
                    ? null : (Work) arguments[0];
            if (name.equals("isDirty")) return Boolean.valueOf(work.dirty);
            if (name.equals("isVisible")) return Boolean.valueOf(work.visible);
            if (name.equals("priority")) return Integer.valueOf(work.visible ? 0 : 3);
            if (name.equals("isPrebake")) return Boolean.FALSE;
            if (name.equals("squaredDistance")) return Double.valueOf(work.id);
            if (name.equals("rebuild")) { work.built = frame; return null; }
            if (name.equals("markClean")) { work.dirty = false; return null; }
            if (name.equals("hashCode")) return Integer.valueOf(System.identityHashCode(proxy));
            if (name.equals("equals")) return Boolean.valueOf(proxy == arguments[0]);
            if (name.equals("toString")) return "M782Adapter";
            throw new IllegalStateException("unexpected adapter method: " + name);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
