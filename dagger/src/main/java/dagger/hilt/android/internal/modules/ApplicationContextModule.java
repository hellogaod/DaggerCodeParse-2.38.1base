package dagger.hilt.android.internal.modules;


import android.app.Application;
import android.content.Context;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.internal.Contexts;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

/** Provides a binding for an Android BinderFragment Context. */
@Module
@InstallIn(SingletonComponent.class)
public final class ApplicationContextModule {
    private final Context applicationContext;

    public ApplicationContextModule(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Provides
    @ApplicationContext
    Context provideContext() {
        return applicationContext;
    }

    @Provides
    Application provideApplication() {
        return Contexts.getApplication(applicationContext);
    }
}
