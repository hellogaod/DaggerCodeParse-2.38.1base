package dagger.hilt.android.internal.builders;


import android.view.View;

import dagger.BindsInstance;
import dagger.hilt.DefineComponent;
import dagger.hilt.android.components.ViewComponent;

/** Interface for creating a {@link ViewComponent}. */
@DefineComponent.Builder
public interface ViewComponentBuilder {
    ViewComponentBuilder view(@BindsInstance View view);

    ViewComponent build();
}
