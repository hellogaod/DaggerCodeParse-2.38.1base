package dagger.hilt.android;


import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Makes a View annotated with {@link AndroidEntryPoint} have access to fragment bindings.
 *
 * <p>By default, views annotated with {@literal @}AndroidEntryPoint do not have access to fragment
 * bindings and must use this annotation if fragment bindings are required. When this annotation is
 * used, this view must always be attached through a fragment.
 */
@Target({ElementType.TYPE})
public @interface WithFragmentBindings {}
