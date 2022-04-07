package dagger.hilt.android.internal.builders;


import android.app.Activity;

import dagger.BindsInstance;
import dagger.hilt.DefineComponent;
import dagger.hilt.android.components.ActivityComponent;

/** Interface for creating an {@link ActivityComponent}. */
@DefineComponent.Builder
public interface ActivityComponentBuilder {
    ActivityComponentBuilder activity(
            @BindsInstance
                    Activity activity);

    ActivityComponent build();
}

