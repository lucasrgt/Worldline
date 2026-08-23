package worldline.test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/** Fail-closed runtime-provider discovery by stable runtime id or implementation class. */
public final class TestRuntimeProviders {
    private TestRuntimeProviders() {}

    public static TestRuntimeProvider discover(String selector) throws ReflectiveOperationException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) loader = TestRuntimeProviders.class.getClassLoader();
        return discover(selector, loader);
    }

    public static TestRuntimeProvider discover(String selector, ClassLoader loader)
            throws ReflectiveOperationException {
        if (selector == null || selector.trim().isEmpty()) {
            throw new IllegalArgumentException("runtime provider selector is blank");
        }
        if (loader == null) throw new NullPointerException("loader");
        String requested = selector.trim();
        List<TestRuntimeProvider> matches = new ArrayList<TestRuntimeProvider>();
        try {
            for (TestRuntimeProvider provider : ServiceLoader.load(TestRuntimeProvider.class, loader)) {
                validate(provider);
                if (requested.equals(provider.runtimeId())) matches.add(provider);
            }
        } catch (ServiceConfigurationError error) {
            throw new IllegalStateException("runtime provider service is invalid", error);
        }
        if (matches.size() > 1) {
            throw new IllegalStateException("multiple providers advertise runtime " + requested);
        }
        if (matches.size() == 1) return matches.get(0);
        return explicit(requested, loader);
    }

    private static TestRuntimeProvider explicit(String name, ClassLoader loader)
            throws ReflectiveOperationException {
        try {
            Class<?> type = Class.forName(name, true, loader);
            TestRuntimeProvider provider = type.asSubclass(TestRuntimeProvider.class)
                    .getDeclaredConstructor().newInstance();
            validate(provider);
            return provider;
        } catch (InvocationTargetException error) {
            Throwable cause = error.getCause();
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            if (cause instanceof Error) throw (Error) cause;
            throw error;
        } catch (ClassNotFoundException error) {
            throw new IllegalArgumentException("no runtime provider for " + name, error);
        }
    }

    private static void validate(TestRuntimeProvider provider) {
        if (provider == null || provider.runtimeId() == null || provider.runtimeId().trim().isEmpty()) {
            throw new IllegalStateException("runtime provider has no stable runtime id");
        }
        if (!provider.runtimeId().equals(provider.runtimeId().trim())) {
            throw new IllegalStateException("runtime provider id has surrounding whitespace");
        }
    }
}
