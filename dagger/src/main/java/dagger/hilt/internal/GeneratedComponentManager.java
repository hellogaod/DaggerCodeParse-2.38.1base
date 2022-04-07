package dagger.hilt.internal;

/**
 * An interface that provides a managed generated component.
 */
// TODO(bcorso): Consider either removing type parameter or using actual component type in usages.
public interface GeneratedComponentManager<T> {
    T generatedComponent();
}
