package dagger.hilt.android.internal.lifecycle;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Internal qualifier for the multibinding map of ViewModels used by the {@link
 * HiltViewModelFactory}.
 */
@Qualifier
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface HiltViewModelMap {

    /** Internal qualifier for the multibinding set of class names annotated with @ViewModelInject. */
    @Qualifier
    @Retention(RetentionPolicy.CLASS)
    @Target({ElementType.METHOD, ElementType.PARAMETER})
    @interface KeySet {}
}
