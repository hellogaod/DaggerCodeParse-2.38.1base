package dagger.hilt.android.components;


import dagger.hilt.DefineComponent;
import dagger.hilt.android.scopes.ActivityRetainedScoped;
import dagger.hilt.components.SingletonComponent;

/** A Hilt component that has the lifetime of a configuration surviving activity. */
@ActivityRetainedScoped
@DefineComponent(parent = SingletonComponent.class)
public interface ActivityRetainedComponent {}

