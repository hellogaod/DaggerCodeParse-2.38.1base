package dagger.hilt.android.internal.builders;

import android.view.View;

import dagger.BindsInstance;
import dagger.hilt.DefineComponent;
import dagger.hilt.android.components.ViewWithFragmentComponent;

/** Interface for creating a {@link ViewWithFragmentComponent}. */
@DefineComponent.Builder
public interface ViewWithFragmentComponentBuilder {
    ViewWithFragmentComponentBuilder view(@BindsInstance View view);

    ViewWithFragmentComponent build();
}
