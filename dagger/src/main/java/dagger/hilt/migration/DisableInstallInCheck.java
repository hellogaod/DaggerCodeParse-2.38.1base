package dagger.hilt.migration;

/**
 * Marks a {@link dagger.Module}-annotated class to allow it to have no {@link
 * dagger.hilt.InstallIn} annotation.
 *
 * <p>Use this annotation on modules to suppress the error of a missing {@link
 * dagger.hilt.InstallIn} annotation. This is useful in cases where non-Hilt Dagger code must be
 * used long-term. If the issue is widespread, consider changing the error behavior with the
 * compiler flag {@code dagger.hilt.disableModulesHaveInstallInCheck} instead.
 */
public @interface DisableInstallInCheck {
}
