package dagger.hilt.android.components;


import dagger.hilt.DefineComponent;
import dagger.hilt.android.scopes.ServiceScoped;
import dagger.hilt.components.SingletonComponent;

/** A Hilt component that has the lifetime of the service. */
@ServiceScoped
@DefineComponent(parent = SingletonComponent.class)
public interface ServiceComponent {}
