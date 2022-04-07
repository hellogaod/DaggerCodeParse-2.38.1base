package dagger.hilt.android;


import android.app.Activity;
import android.content.Context;
import android.view.View;

import javax.annotation.Nonnull;

import androidx.fragment.app.Fragment;
import dagger.hilt.EntryPoints;
import dagger.hilt.android.internal.Contexts;

/**
 * Static utility methods for dealing with entry points for standard Android components.
 */
public final class EntryPointAccessors {

    /**
     * Returns the entry point interface from an application. The context can be any context derived
     * from the application context. May only be used with entry point interfaces installed in the
     * SingletonComponent.
     */
    @Nonnull
    public static <T> T fromApplication(Context context, Class<T> entryPoint) {
        return EntryPoints.get(Contexts.getApplication(context.getApplicationContext()), entryPoint);
    }

    /**
     * Returns the entry point interface from an activity. May only be used with entry point
     * interfaces installed in the ActivityComponent.
     */
    @Nonnull
    public static <T> T fromActivity(Activity activity, Class<T> entryPoint) {
        return EntryPoints.get(activity, entryPoint);
    }

    /**
     * Returns the entry point interface from a fragment. May only be used with entry point interfaces
     * installed in the FragmentComponent.
     */
    @Nonnull
    public static <T> T fromFragment(Fragment fragment, Class<T> entryPoint) {
        return EntryPoints.get(fragment, entryPoint);
    }

    /**
     * Returns the entry point interface from a view. May only be used with entry point interfaces
     * installed in the ViewComponent or ViewNoFragmentComponent.
     */
    @Nonnull
    public static <T> T fromView(View view, Class<T> entryPoint) {
        return EntryPoints.get(view, entryPoint);
    }

    private EntryPointAccessors() {}
}
