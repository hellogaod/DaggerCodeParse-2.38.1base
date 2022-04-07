package dagger.hilt.android.internal.managers;

import android.content.Context;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.EntryPoint;
import dagger.hilt.EntryPoints;
import dagger.hilt.InstallIn;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.android.components.ActivityRetainedComponent;
import dagger.hilt.android.internal.ThreadUtil;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.scopes.ActivityRetainedScoped;
import dagger.hilt.components.SingletonComponent;
import dagger.hilt.internal.GeneratedComponentManager;

/**
 * A manager for the creation of components that survives activity configuration changes.
 */
final class ActivityRetainedComponentManager
        implements GeneratedComponentManager<ActivityRetainedComponent> {

    /**
     * Entry point for {@link ActivityRetainedComponentBuilder}.
     */
    @EntryPoint
    @InstallIn(SingletonComponent.class)
    public interface ActivityRetainedComponentBuilderEntryPoint {
        ActivityRetainedComponentBuilder retainedComponentBuilder();
    }

    /**
     * Entry point for {@link Lifecycle}.
     */
    @EntryPoint
    @InstallIn(ActivityRetainedComponent.class)
    public interface ActivityRetainedLifecycleEntryPoint {
        ActivityRetainedLifecycle getActivityRetainedLifecycle();
    }

    static final class ActivityRetainedComponentViewModel extends ViewModel {
        private final ActivityRetainedComponent component;

        ActivityRetainedComponentViewModel(ActivityRetainedComponent component) {
            this.component = component;
        }

        ActivityRetainedComponent getComponent() {
            return component;
        }

        @Override
        protected void onCleared() {
            super.onCleared();
            ActivityRetainedLifecycle lifecycle =
                    EntryPoints.get(component, ActivityRetainedLifecycleEntryPoint.class)
                            .getActivityRetainedLifecycle();
            ((ActivityRetainedComponentManager.Lifecycle) lifecycle).dispatchOnCleared();
        }
    }

    private final ViewModelProvider viewModelProvider;

    @Nullable
    private volatile ActivityRetainedComponent component;
    private final Object componentLock = new Object();

    ActivityRetainedComponentManager(ComponentActivity activity) {
        this.viewModelProvider = getViewModelProvider(activity, activity);
    }

    private ViewModelProvider getViewModelProvider(
            ViewModelStoreOwner owner, Context context) {

        return new ViewModelProvider(
                owner,
                new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    @SuppressWarnings("unchecked")
                    public <T extends ViewModel> T create(@NonNull Class<T> aClass) {
                        ActivityRetainedComponent component =
                                EntryPointAccessors.fromApplication(
                                        context, ActivityRetainedComponentBuilderEntryPoint.class)
                                        .retainedComponentBuilder()
                                        .build();
                        return (T) new ActivityRetainedComponentViewModel(component);
                    }
                });
    }

    @Override
    public ActivityRetainedComponent generatedComponent() {
        if (component == null) {
            synchronized (componentLock) {
                if (component == null) {
                    component = createComponent();
                }
            }
        }
        return component;
    }

    private ActivityRetainedComponent createComponent() {
        return viewModelProvider.get(ActivityRetainedComponentViewModel.class).getComponent();
    }

    /**
     * The default implementation of {@link ActivityRetainedLifecycle}.
     */
    @ActivityRetainedScoped
    static final class Lifecycle implements ActivityRetainedLifecycle {

        private final Set<OnClearedListener> listeners = new HashSet<>();
        private boolean onClearedDispatched = false;

        @Inject
        Lifecycle() {
        }

        @Override
        public void addOnClearedListener(@NonNull OnClearedListener listener) {
            ThreadUtil.ensureMainThread();
            throwIfOnClearedDispatched();
            listeners.add(listener);
        }

        @Override
        public void removeOnClearedListener(@NonNull OnClearedListener listener) {
            ThreadUtil.ensureMainThread();
            throwIfOnClearedDispatched();
            listeners.remove(listener);
        }

        void dispatchOnCleared() {
            ThreadUtil.ensureMainThread();
            onClearedDispatched = true;
            for (OnClearedListener listener : listeners) {
                listener.onCleared();
            }
        }

        private void throwIfOnClearedDispatched() {
            if (onClearedDispatched) {
                throw new IllegalStateException(
                        "There was a race between the call to add/remove an OnClearedListener and onCleared(). "
                                + "This can happen when posting to the Main thread from a background thread, "
                                + "which is not supported.");
            }
        }
    }

    @Module
    @InstallIn(ActivityRetainedComponent.class)
    abstract static class LifecycleModule {
        @Binds
        abstract ActivityRetainedLifecycle bind(Lifecycle impl);
    }
}
