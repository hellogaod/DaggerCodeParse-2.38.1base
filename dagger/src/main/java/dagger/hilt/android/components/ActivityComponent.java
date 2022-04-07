package dagger.hilt.android.components;


import dagger.hilt.DefineComponent;
import dagger.hilt.android.scopes.ActivityScoped;

/** A Hilt component that has the lifetime of the activity. */
@ActivityScoped
@DefineComponent(parent = ActivityRetainedComponent.class)
public interface ActivityComponent {}
