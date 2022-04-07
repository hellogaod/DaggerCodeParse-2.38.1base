package dagger.hilt.android.internal.builders;

import dagger.hilt.DefineComponent;
import dagger.hilt.android.components.ActivityRetainedComponent;


/**
 * Interface for creating a {@link ActivityRetainedComponent}.
 */
@DefineComponent.Builder
public interface ActivityRetainedComponentBuilder {
    ActivityRetainedComponent build();
}

