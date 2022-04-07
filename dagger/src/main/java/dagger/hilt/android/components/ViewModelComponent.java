package dagger.hilt.android.components;

import dagger.hilt.DefineComponent;
import dagger.hilt.android.scopes.ViewModelScoped;


/**
 * A Hilt component that has the lifetime of a single {@link androidx.lifecycle.ViewModel}.
 *
 * <p>This Hilt component is the source of {@link
 * dagger.hilt.android.lifecycle.HiltViewModel}-annotated {@link androidx.lifecycle.ViewModel}s
 * used by the {@link dagger.hilt.android.lifecycle.HiltViewModelFactory}. It contains a default
 * binding for the {@link androidx.lifecycle.SavedStateHandle} associated with the {@code
 * ViewModel} that can be used by other dependencies provided by the component.
 *
 * <p>Dependencies available in the {@link dagger.hilt.components.SingletonComponent} and {@link
 * ActivityRetainedComponent} are also available in this component since it is a child of {@code
 * ActivityRetainedComponent}.
 *
 * <p>Example usage:
 *
 * <pre>
 * &#64;Module
 * &#64;InstallIn(ViewModelComponent.class)
 * public final class ViewModelMovieModule {
 *     &#64;Provides
 *     public static MovieRepository provideRepo(SavedStateHandle handle) {
 *         return new MovieRepository(handle.getString("movie-id"));
 *     }
 * }
 * </pre>
 *
 * <p>Dependencies in the {@code ViewModelComponent} can be scoped using the {@link ViewModelScoped}
 * annotation. This allows for a single instance of a dependency to be provided across the
 * dependencies of a single {@link dagger.hilt.android.lifecycle.HiltViewModel}-annotated {@code
 * ViewModel}.
 *
 * @see dagger.hilt.android.lifecycle.HiltViewModel
 * @see dagger.hilt.android.scopes.ViewModelScoped
 */
@ViewModelScoped
@DefineComponent(parent = ActivityRetainedComponent.class)
public interface ViewModelComponent {}
