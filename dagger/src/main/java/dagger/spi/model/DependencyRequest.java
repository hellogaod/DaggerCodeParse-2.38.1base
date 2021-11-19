package dagger.spi.model;

import com.google.auto.value.AutoValue;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;

import java.util.Optional;

import javax.inject.Inject;

import dagger.Provides;

/**
 * Represents a request for a {@link Key} at an injection point. For example, parameters to {@link
 * Inject} constructors, {@link Provides} methods, and component methods are all dependency
 * requests.
 * <p>
 * 表示在注入点对 {@link Key} 的请求。 例如，{@link Inject} 构造函数、{@link Provides} 方法和组件方法的参数都是依赖请求。
 *
 * <p id="synthetic">A dependency request is considered to be <em>synthetic</em> if it does not have
 * an {@link DaggerElement} in code that requests the key directly. For example, an {@link
 * java.util.concurrent.Executor} is required for all {@code @Produces} methods to run
 * asynchronously even though it is not directly specified as a parameter to the binding method.
 * <p>
 * 如果依赖项请求在直接请求密钥的代码中没有 {@link DaggerElement}，则它被认为是<em>合成</em>。
 * 例如，所有 {@code @Produces} 方法都需要 {@link java.util.concurrent.Executor} 异步运行，即使它没有直接指定为绑定方法的参数。
 */
@AutoValue
public abstract class DependencyRequest {

    /**
     * The kind of this request.
     */
    public abstract RequestKind kind();

    /**
     * The key of this request.
     */
    public abstract Key key();

    /**
     * The element that declares this dependency request. Absent for <a href="#synthetic">synthetic
     * </a> requests.
     */
    public abstract Optional<DaggerElement> requestElement();

    /**
     * Returns {@code true} if this request allows null objects. A request is nullable if it is
     * has an annotation with "Nullable" as its simple name.
     */
    public abstract boolean isNullable();

    /**
     * Returns a new builder of dependency requests.
     */
    public static DependencyRequest.Builder builder() {
        return new AutoValue_DependencyRequest.Builder().isNullable(false);
    }

    /**
     * A builder of {@link DependencyRequest}s.
     */
    @CanIgnoreReturnValue
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder kind(RequestKind kind);

        public abstract Builder key(Key key);

        public abstract Builder requestElement(DaggerElement element);

        public abstract Builder isNullable(boolean isNullable);

        @CheckReturnValue
        public abstract DependencyRequest build();
    }
}
