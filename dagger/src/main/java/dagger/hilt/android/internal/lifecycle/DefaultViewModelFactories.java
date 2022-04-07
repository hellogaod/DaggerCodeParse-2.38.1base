package dagger.hilt.android.internal.lifecycle;


import android.app.Application;
import android.os.Bundle;

import java.util.Set;

import javax.inject.Inject;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.savedstate.SavedStateRegistryOwner;
import dagger.Module;
import dagger.hilt.EntryPoint;
import dagger.hilt.EntryPoints;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.android.components.FragmentComponent;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.multibindings.Multibinds;

/**
 * Modules and entry points for the default view model factory used by activities and fragments
 * annotated with @AndroidEntryPoint.
 *
 * <p>Entry points are used to acquire the factory because injected fields in the generated
 * activities and fragments are ignored by Dagger when using the transform due to the generated
 * class not being part of the hierarchy during compile time.
 */
public final class DefaultViewModelFactories {


    /**
     * Retrieves the default view model factory for the activity.
     *
     * <p>Do not use except in Hilt generated code!
     */
    public static ViewModelProvider.Factory getActivityFactory(ComponentActivity activity,
                                                               ViewModelProvider.Factory delegateFactory) {
        return EntryPoints.get(activity, ActivityEntryPoint.class)
                .getHiltInternalFactoryFactory()
                .fromActivity(activity, delegateFactory);
    }

    /**
     * Retrieves the default view model factory for the activity.
     *
     * <p>Do not use except in Hilt generated code!
     */
    public static ViewModelProvider.Factory getFragmentFactory(
            Fragment fragment, ViewModelProvider.Factory delegateFactory) {
        return EntryPoints.get(fragment, FragmentEntryPoint.class)
                .getHiltInternalFactoryFactory()
                .fromFragment(fragment, delegateFactory);
    }


    /**
     * Internal factory for the Hilt ViewModel Factory.
     */
    public static final class InternalFactoryFactory {

        private final Application application;
        private final Set<String> keySet;
        private final ViewModelComponentBuilder viewModelComponentBuilder;

        @Inject
        InternalFactoryFactory(
                Application application,
                @HiltViewModelMap.KeySet Set<String> keySet,
                ViewModelComponentBuilder viewModelComponentBuilder) {
            this.application = application;
            this.keySet = keySet;
            this.viewModelComponentBuilder = viewModelComponentBuilder;
        }

        ViewModelProvider.Factory fromActivity(
                ComponentActivity activity, ViewModelProvider.Factory delegateFactory) {
            return getHiltViewModelFactory(
                    activity,
                    activity.getIntent() != null ? activity.getIntent().getExtras() : null,
                    delegateFactory);
        }

        ViewModelProvider.Factory fromFragment(
                Fragment fragment, ViewModelProvider.Factory delegateFactory) {
            return getHiltViewModelFactory(fragment, fragment.getArguments(), delegateFactory);
        }

        private ViewModelProvider.Factory getHiltViewModelFactory(
                SavedStateRegistryOwner owner,
                @Nullable Bundle defaultArgs,
                @Nullable ViewModelProvider.Factory extensionDelegate) {
            ViewModelProvider.Factory delegate = extensionDelegate == null
                    ? new SavedStateViewModelFactory(application, owner, defaultArgs)
                    : extensionDelegate;
            return new HiltViewModelFactory(
                    owner, defaultArgs, keySet, delegate, viewModelComponentBuilder);
        }
    }

    /**
     * The activity module to declare the optional factories.
     */
    @Module
    @InstallIn(ActivityComponent.class)
    interface ActivityModule {
        @Multibinds
        @HiltViewModelMap.KeySet
        abstract Set<String> viewModelKeys();
    }

    /**
     * The activity entry point to retrieve the factory.
     */
    @EntryPoint
    @InstallIn(ActivityComponent.class)
    public interface ActivityEntryPoint {
        InternalFactoryFactory getHiltInternalFactoryFactory();
    }

    /**
     * The fragment entry point to retrieve the factory.
     */
    @EntryPoint
    @InstallIn(FragmentComponent.class)
    public interface FragmentEntryPoint {
        InternalFactoryFactory getHiltInternalFactoryFactory();
    }


    private DefaultViewModelFactories() {
    }
}
