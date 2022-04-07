package dagger.hilt.android.components;


import dagger.hilt.DefineComponent;
import dagger.hilt.android.scopes.FragmentScoped;

/**
 * A Hilt component that has the lifetime of the fragment.
 */
@FragmentScoped
@DefineComponent(parent = ActivityComponent.class)
public interface FragmentComponent {
}
