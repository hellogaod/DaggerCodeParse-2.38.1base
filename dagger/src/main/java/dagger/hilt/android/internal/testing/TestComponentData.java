package dagger.hilt.android.internal.testing;


import java.util.Map;
import java.util.Set;

import dagger.hilt.internal.Preconditions;

/**
 * Contains the data needed to create a test's component.
 */
public final class TestComponentData {
    private final ComponentSupplier componentSupplier;
    private final TestInjector<Object> testInjector;
    private final Set<Class<?>> daggerRequiredModules;
    private final Set<Class<?>> hiltRequiredModules;
    private final boolean waitForBindValue;


    public TestComponentData(
            boolean waitForBindValue,
            TestInjector<Object> testInjector,
            Set<Class<?>> daggerRequiredModules,
            Set<Class<?>> hiltRequiredModules,
            ComponentSupplier componentSupplier) {
        Preconditions.checkState(
                daggerRequiredModules.containsAll(hiltRequiredModules),
                "Hilt required modules should be subset of Dagger required modules.");
        this.componentSupplier = componentSupplier;
        this.testInjector = testInjector;
        this.daggerRequiredModules = daggerRequiredModules;
        this.waitForBindValue = waitForBindValue;
        this.hiltRequiredModules = hiltRequiredModules;
    }

    /**
     * Returns the {@link ComponentSupplier}.
     */
    public ComponentSupplier componentSupplier() {
        return componentSupplier;
    }

    /**
     * Returns the {@link TestInjector}.
     */
    public TestInjector<Object> testInjector() {
        return testInjector;
    }

    /**
     * Returns the set of modules that Dagger cannot create instances of itself
     */
    public Set<Class<?>> daggerRequiredModules() {
        return daggerRequiredModules;
    }

    /**
     * Returns a subset of {@link #daggerRequiredModules} that filters out the modules Hilt can
     * instantiate itself.
     */
    public Set<Class<?>> hiltRequiredModules() {
        return hiltRequiredModules;
    }

    /**
     * Returns true if creation of the component needs to wait for bind() to be called.
     */
    public boolean waitForBindValue() {
        return waitForBindValue;
    }

    /**
     * Returns the component using the given registered modules.
     */
    public interface ComponentSupplier {
        Object get(Map<Class<?>, ?> registeredModules, Object testInstance, Boolean autoAddModule);
    }
}
