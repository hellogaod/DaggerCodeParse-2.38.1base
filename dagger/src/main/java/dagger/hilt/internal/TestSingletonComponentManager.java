package dagger.hilt.internal;


/**
 * A marker that the given component manager is for an {@link TestSingletonComponent}.
 */
public interface TestSingletonComponentManager extends GeneratedComponentManager<Object> {
    Object earlySingletonComponent();
}
