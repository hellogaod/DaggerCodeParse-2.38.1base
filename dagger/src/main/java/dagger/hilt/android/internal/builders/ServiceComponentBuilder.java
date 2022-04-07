package dagger.hilt.android.internal.builders;


import android.app.Service;

import dagger.BindsInstance;
import dagger.hilt.DefineComponent;
import dagger.hilt.android.components.ServiceComponent;

/** Interface for creating a {@link ServiceComponent}. */
@DefineComponent.Builder
public interface ServiceComponentBuilder {
    ServiceComponentBuilder service(@BindsInstance Service service);
    ServiceComponent build();
}

