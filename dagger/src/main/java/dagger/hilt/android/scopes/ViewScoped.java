package dagger.hilt.android.scopes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Scope;

import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Scope annotation for bindings that should exist for the life of a View.
 */
@Scope
@Retention(CLASS)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ViewScoped {}
