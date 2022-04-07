package dagger.hilt.android.internal.managers;


import android.app.Application;
import android.content.Context;

import dagger.hilt.android.internal.Contexts;
import dagger.hilt.internal.GeneratedComponentManager;
import dagger.hilt.internal.Preconditions;

/**
 * Do not use except in Hilt generated code!
 *
 * <p>A manager for the creation of components that live in the BroadcastReceiver.
 */
public final class BroadcastReceiverComponentManager {

    public static Object generatedComponent(Context context) {
        Application application = Contexts.getApplication(context.getApplicationContext());

        Preconditions.checkArgument(
                application instanceof GeneratedComponentManager,
                "Hilt BroadcastReceiver must be attached to an @AndroidEntryPoint Application. "
                        + "Found: %s",
                application.getClass());

        return ((GeneratedComponentManager<?>) application).generatedComponent();
    }

    private BroadcastReceiverComponentManager() {
    }
}
