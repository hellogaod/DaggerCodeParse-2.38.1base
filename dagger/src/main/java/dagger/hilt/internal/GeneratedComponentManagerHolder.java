package dagger.hilt.internal;


/** An interface that provides a managed generated component holder. */
public interface GeneratedComponentManagerHolder extends GeneratedComponentManager<Object> {

    public GeneratedComponentManager<?> componentManager();
}
