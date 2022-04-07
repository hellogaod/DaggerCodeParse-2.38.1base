package dagger.hilt.android.internal.builders;


import androidx.fragment.app.Fragment;
import dagger.BindsInstance;
import dagger.hilt.DefineComponent;
import dagger.hilt.android.components.FragmentComponent;

/** Interface for creating a {@link FragmentComponent}. */
@DefineComponent.Builder
public interface FragmentComponentBuilder {
    FragmentComponentBuilder fragment(@BindsInstance Fragment fragment);
    FragmentComponent build();
}
