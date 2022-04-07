package dagger.hilt.android.internal.managers;


import dagger.hilt.internal.GeneratedComponentManager;

/**
 * Do not use except in Hilt generated code!
 *
 * <p>A manager for the creation of components that live in the Application.
 */
public final class ApplicationComponentManager implements GeneratedComponentManager<Object> {
    private volatile Object component;
    private final Object componentLock = new Object();
    private final ComponentSupplier componentCreator;

    public ApplicationComponentManager(ComponentSupplier componentCreator) {
        this.componentCreator = componentCreator;
    }

    @Override
    public Object generatedComponent() {
        if (component == null) {
            synchronized (componentLock) {
                if (component == null) {
                    component = componentCreator.get();
                }
            }
        }
        return component;
    }
}
