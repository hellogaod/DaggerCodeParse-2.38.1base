package dagger.hilt.android.internal.builders;


import androidx.lifecycle.SavedStateHandle;
import dagger.BindsInstance;
import dagger.hilt.DefineComponent;
import dagger.hilt.android.components.ViewModelComponent;

/**
 * Interface for creating a {@link ViewModelComponent}.
 */
@DefineComponent.Builder
public interface ViewModelComponentBuilder {
    ViewModelComponentBuilder savedStateHandle(@BindsInstance SavedStateHandle handle);

    ViewModelComponent build();
}

