package dagger.producers.monitoring;


import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Objects;

import dagger.producers.Produces;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A token that represents an individual {@linkplain Produces producer method}.
 */
public final class ProducerToken {
    @NullableDecl
    private final Class<?> classToken;
    @NullableDecl
    private final String methodName;

    private ProducerToken(@NullableDecl Class<?> classToken, @NullableDecl String methodName) {
        this.classToken = classToken;
        this.methodName = methodName;
    }

    /**
     * Creates a token for a class token that represents the generated factory for a producer method.
     *
     * <p><b>Do not use this!</b> This is intended to be called by generated code only, and its
     * signature may change at any time.
     */
    public static ProducerToken create(Class<?> classToken) {
        return new ProducerToken(checkNotNull(classToken), null);
    }

    /**
     * Creates a token for a producer method.
     *
     * <p><b>Do not use this!</b> This is intended to be called by generated code only, and its
     * signature may change at any time.
     */
    public static ProducerToken create(String methodName) {
        return new ProducerToken(null, checkNotNull(methodName));
    }

    /**
     * Returns an appropriate hash code to match {@link #equals(Object)}.
     */
    @Override
    public int hashCode() {
        int h = 1;
        h *= 1000003;
        h ^= Objects.hashCode(this.classToken);
        h *= 1000003;
        h ^= Objects.hashCode(this.methodName);
        return h;
    }

    /**
     * Two tokens are equal if they represent the same method.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof ProducerToken) {
            ProducerToken that = (ProducerToken) o;
            return Objects.equals(this.classToken, that.classToken)
                    && Objects.equals(this.methodName, that.methodName);
        } else {
            return false;
        }
    }

    /**
     * Returns a representation of the method.
     */
    @Override
    public String toString() {
        if (methodName != null) {
            return methodName;
        } else if (classToken != null) {
            return classToken.getCanonicalName();
        } else {
            throw new IllegalStateException();
        }
    }
}
