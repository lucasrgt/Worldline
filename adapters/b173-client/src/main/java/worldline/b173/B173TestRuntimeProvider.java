package worldline.b173;

import java.io.IOException;
import worldline.api.AutomatedMinecraftRuntime;
import worldline.api.WorldSource;
import worldline.mods.LoadedMod;
import worldline.mods.ModLoader;
import worldline.test.TestRuntimeProvider;
import worldline.test.TestRuntimeRequest;
import worldline.test.TestRuntimeSession;

/** Fresh controlled b1.7.3 client session for the neutral TestKit runner. */
public final class B173TestRuntimeProvider implements TestRuntimeProvider {
    @Override public String runtimeId() { return "b1.7.3"; }

    @Override public TestRuntimeSession open(TestRuntimeRequest request) throws Exception {
        if (request == null) throw new NullPointerException("request");
        B173Runtime runtime = B173Runtimes.create(request.seed());
        LoadedMod<B173Mod> loaded = null;
        try {
            runtime.bootHeadless(); runtime.loadWorld(WorldSource.at(request.worldPath()));
            if (request.modPath() != null) {
                loaded = ModLoader.load(request.modPath(), runtimeId(), "1", B173Mod.class);
                runtime.installMod(loaded.instance());
            }
            return new Session(runtime, loaded);
        } catch (Exception | Error failure) {
            runtime.close(); close(loaded, failure); throw failure;
        }
    }

    private static final class Session implements TestRuntimeSession {
        private final B173Runtime runtime; private final LoadedMod<B173Mod> mod;
        Session(B173Runtime runtime, LoadedMod<B173Mod> mod) { this.runtime = runtime; this.mod = mod; }
        @Override public AutomatedMinecraftRuntime runtime() { return runtime; }
        @Override public void close() {
            Throwable failure = null;
            try { runtime.close(); } catch (RuntimeException | Error error) { failure = error; }
            try { if (mod != null) mod.close(); }
            catch (IOException error) {
                if (failure == null) failure = new IllegalStateException("mod class loader close failed", error);
                else failure.addSuppressed(error);
            }
            if (failure instanceof RuntimeException) throw (RuntimeException) failure;
            if (failure instanceof Error) throw (Error) failure;
        }
    }
    private static void close(LoadedMod<B173Mod> mod, Throwable failure) {
        if (mod == null) return;
        try { mod.close(); } catch (IOException error) { failure.addSuppressed(error); }
    }
}
