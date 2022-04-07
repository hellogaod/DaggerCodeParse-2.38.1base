package dagger.hilt.android.lifecycle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dagger.hilt.GeneratesRootInput;

/**
 * Identifies a {@link androidx.lifecycle.ViewModel} for construction injection.
 *
 * <p>The {@code ViewModel} annotated with {@link HiltViewModel} will be available for creation by
 * the {@link dagger.hilt.android.lifecycle.HiltViewModelFactory} and can be retrieved by default in
 * an {@code Activity} or {@code Fragment} annotated with {@link
 * dagger.hilt.android.AndroidEntryPoint}. The {@code HiltViewModel} containing a constructor
 * annotated with {@link javax.inject.Inject} will have its dependencies defined in the constructor
 * parameters injected by Dagger's Hilt.
 *
 * <p>Example:
 *
 * <pre>
 * &#64;HiltViewModel
 * public class DonutViewModel extends ViewModel {
 *     &#64;Inject
 *     public DonutViewModel(SavedStateHandle handle, RecipeRepository repository) {
 *         // ...
 *     }
 * }
 * </pre>
 *
 * <pre>
 * &#64;AndroidEntryPoint
 * public class CookingActivity extends AppCompatActivity {
 *     public void onCreate(Bundle savedInstanceState) {
 *         DonutViewModel vm = new ViewModelProvider(this).get(DonutViewModel.class);
 *     }
 * }
 * </pre>
 *
 * <p>Exactly one constructor in the {@code ViewModel} must be annotated with {@code Inject}.
 *
 * <p>Only dependencies available in the {@link dagger.hilt.android.components.ViewModelComponent}
 * can be injected into the {@code ViewModel}.
 *
 * <p>
 *
 * @see dagger.hilt.android.components.ViewModelComponent
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@GeneratesRootInput
public @interface HiltViewModel {}
