package dagger.assisted;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotates a parameter within an {@link AssistedInject}-annotated constructor.
 * <p>
 * AssistedInject修饰的构造方法，其参数使用本注解
 *
 * <p>See {@link AssistedInject}.
 */
@Documented
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface Assisted {

    /**
     * Returns an identifier for an {@link Assisted} parameter.
     *
     * <p>Within an {@link AssistedInject} constructor, each {@link Assisted} parameter must be
     * uniquely defined by the combination of its identifier and type. If no identifier is specified,
     * the default identifier is an empty string. Thus, the following parameters are equivalent within
     * an {@link AssistedInject} constructor:
     *
     * <ul>
     *   <li> {@code @Assisted Foo foo}
     *   <li> {@code @Assisted("") Foo foo}
     * </ul>
     *
     * <p>Within an {@link AssistedFactory} method, each parameter must match an {@link Assisted}
     * parameter in the associated {@link AssistedInject} constructor (i.e. identifier + type).
     * A parameter with no {@code @Assisted} annotation will be assigned the default identifier. Thus,
     * the following parameters are equivalent within an {@link AssistedFactory} method:
     *
     * <ul>
     *   <li> {@code Foo foo}
     *   <li> {@code @Assisted Foo foo}
     *   <li> {@code @Assisted("") Foo foo}
     * </ul>
     *
     * <p>Example:
     *
     * <pre><code>
     * final class DataService {
     *   {@literal @}AssistedInject
     *   DataService(
     *       BindingFromDagger bindingFromDagger,
     *       {@literal @}Assisted String name,
     *       {@literal @}Assisted("id") String id,
     *       {@literal @}Assisted("repo") String repo) {}
     * }
     *
     * {@literal @}AssistedFactory
     * interface DataServiceFactory {
     *   DataService create(
     *       String name,
     *       {@literal @}Assisted("id") String id,
     *       {@literal @}Assisted("repo") String repo);
     * }
     * </code></pre>
     */
    String value() default "";
}
