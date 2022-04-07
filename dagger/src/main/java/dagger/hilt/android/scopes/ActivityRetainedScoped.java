package dagger.hilt.android.scopes;


import java.lang.annotation.Retention;

import javax.inject.Scope;

import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Scope annotation for bindings that should exist for the life of an activity, surviving
 * configuration.
 */
@Scope
@Retention(CLASS)
public @interface ActivityRetainedScoped {}

