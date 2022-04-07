package dagger.hilt.android.components;


import dagger.hilt.DefineComponent;
import dagger.hilt.android.scopes.ViewScoped;

/** A Hilt component that has the lifetime of the view. */
@ViewScoped
@DefineComponent(parent = FragmentComponent.class)
public interface ViewWithFragmentComponent {}
