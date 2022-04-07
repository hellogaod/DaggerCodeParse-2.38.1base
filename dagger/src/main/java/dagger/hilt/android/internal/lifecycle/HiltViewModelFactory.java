package dagger.hilt.android.internal.lifecycle;


import android.app.Activity;
import android.os.Bundle;

import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AbstractSavedStateViewModelFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.savedstate.SavedStateRegistryOwner;
import dagger.Module;
import dagger.hilt.EntryPoint;
import dagger.hilt.EntryPoints;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.android.components.ViewModelComponent;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.multibindings.Multibinds;

/**
 * View Model Provider Factory for the Hilt Extension.
 *
 * <p>A provider for this factory will be installed in the {@link
 * dagger.hilt.android.components.ActivityComponent} and {@link
 * dagger.hilt.android.components.FragmentComponent}. An instance of this factory will also be the
 * default factory by activities and fragments annotated with {@link
 * dagger.hilt.android.AndroidEntryPoint}.
 */
public final class HiltViewModelFactory implements ViewModelProvider.Factory {

    /** Hilt entry point for getting the multi-binding map of ViewModels. */
    @EntryPoint
    @InstallIn(ViewModelComponent.class)
    public interface ViewModelFactoriesEntryPoint {
        @HiltViewModelMap
        Map<String, Provider<ViewModel>> getHiltViewModelMap();
    }

    /** Hilt module for providing the empty multi-binding map of ViewModels. */
    @Module
    @InstallIn(ViewModelComponent.class)
    interface ViewModelModule {
        @Multibinds
        @HiltViewModelMap
        Map<String, ViewModel> hiltViewModelMap();
    }

    private final Set<String> hiltViewModelKeys;
    private final ViewModelProvider.Factory delegateFactory;
    private final AbstractSavedStateViewModelFactory hiltViewModelFactory;

    public HiltViewModelFactory(
            @NonNull SavedStateRegistryOwner owner,
            @Nullable Bundle defaultArgs,
            @NonNull Set<String> hiltViewModelKeys,
            @NonNull ViewModelProvider.Factory delegateFactory,
            @NonNull ViewModelComponentBuilder viewModelComponentBuilder) {
        this.hiltViewModelKeys = hiltViewModelKeys;
        this.delegateFactory = delegateFactory;
        this.hiltViewModelFactory =
                new AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                    @NonNull
                    @Override
                    @SuppressWarnings("unchecked")
                    protected <T extends ViewModel> T create(
                            @NonNull String key, @NonNull Class<T> modelClass, @NonNull SavedStateHandle handle) {
                        ViewModelComponent component =
                                viewModelComponentBuilder.savedStateHandle(handle).build();
                        Provider<? extends ViewModel> provider =
                                EntryPoints.get(component, ViewModelFactoriesEntryPoint.class)
                                        .getHiltViewModelMap()
                                        .get(modelClass.getName());
                        if (provider == null) {
                            throw new IllegalStateException(
                                    "Expected the @HiltViewModel-annotated class '"
                                            + modelClass.getName()
                                            + "' to be available in the multi-binding of "
                                            + "@HiltViewModelMap but none was found.");
                        }
                        return (T) provider.get();
                    }
                };
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (hiltViewModelKeys.contains(modelClass.getName())) {
            return hiltViewModelFactory.create(modelClass);
        } else {
            return delegateFactory.create(modelClass);
        }
    }

    @EntryPoint
    @InstallIn(ActivityComponent.class)
    interface ActivityCreatorEntryPoint {
        @HiltViewModelMap.KeySet
        Set<String> getViewModelKeys();
        ViewModelComponentBuilder getViewModelComponentBuilder();
    }

    public static ViewModelProvider.Factory createInternal(
            @NonNull Activity activity,
            @NonNull SavedStateRegistryOwner owner,
            @Nullable Bundle defaultArgs,
            @NonNull ViewModelProvider.Factory delegateFactory) {
        ActivityCreatorEntryPoint entryPoint =
                EntryPoints.get(activity, ActivityCreatorEntryPoint.class);
        return new HiltViewModelFactory(
                owner,
                defaultArgs,
                entryPoint.getViewModelKeys(),
                delegateFactory,
                entryPoint.getViewModelComponentBuilder()
        );
    }
}
