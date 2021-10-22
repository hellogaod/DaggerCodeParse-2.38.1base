package dagger;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Map;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Identifies annotation types that are used to associate keys with values returned by {@linkplain
 * Provides provider methods} in order to compose a {@linkplain dagger.multibindings.IntoMap map}.
 * <p>
 * 标识用于将键与 {@linkplain Provides provider methods} 返回的值相关联的注释类型，以组成 {@linkplain dagger.multibindings.IntoMap map}。
 *
 * <p>Every provider method annotated with {@code @Provides} and {@code @IntoMap} must also have an
 * annotation that identifies the key for that map entry. That annotation's type must be annotated
 * with {@code @MapKey}.
 *
 * <p>Every provider method annotated with {@code @Provides} and {@code @IntoMap} must also have an
 * annotation that identifies the key for that map entry. That annotation's type must be annotated
 * with {@code @MapKey}.
 * <p>
 * 每个使用 {@code @Provides} 和 {@code @IntoMap} 注释的提供者方法还必须有一个注释来标识该映射条目的键。
 * 该注解的类型必须使用 {@code @MapKey} 进行注解。
 *
 * <p>Typically, the key annotation has a single member, whose value is used as the map key.
 *
 * <p>For example, to add an entry to a {@code Map<SomeEnum, Integer>} with key {@code
 * SomeEnum.FOO}, you could use an annotation called {@code @SomeEnumKey}:
 *
 * <pre><code>
 * {@literal @}MapKey
 * {@literal @}interface SomeEnumKey {
 *   SomeEnum value();
 * }
 *
 * {@literal @}Module
 * class SomeModule {
 *   {@literal @}Provides
 *   {@literal @}IntoMap
 *   {@literal @}SomeEnumKey(SomeEnum.FOO)
 *   Integer provideFooValue() {
 *     return 2;
 *   }
 * }
 *
 * class SomeInjectedType {
 *   {@literal @}Inject
 *   SomeInjectedType({@literal Map<SomeEnum, Integer>} map) {
 *     assert map.get(SomeEnum.FOO) == 2;
 *   }
 * }
 * </code></pre>
 *
 * <p>If {@code unwrapValue} is true, the annotation's single member can be any type except an
 * array.
 *
 * <p>See {@link dagger.multibindings} for standard unwrapped map key annotations for keys that are
 * boxed primitives, strings, or classes.
 *
 * <h2>Annotations as keys</h2>
 *
 * <p>If {@link #unwrapValue} is false, then the annotation itself is used as the map key. For
 * example, to add an entry to a {@code Map<MyMapKey, Integer>} map:
 *
 * <pre><code>
 * {@literal @}MapKey(unwrapValue = false)
 * {@literal @}interface MyMapKey {
 *   String someString();
 *   MyEnum someEnum();
 * }
 *
 * {@literal @}Module
 * class SomeModule {
 *   {@literal @}Provides
 *   {@literal @}IntoMap
 *   {@literal @}MyMapKey(someString = "foo", someEnum = BAR)
 *   Integer provideFooBarValue() {
 *     return 2;
 *   }
 * }
 *
 * class SomeInjectedType {
 *   {@literal @}Inject
 *   SomeInjectedType({@literal Map<MyMapKey, Integer>} map) {
 *     assert map.get(new MyMapKeyImpl("foo", MyEnum.BAR)) == 2;
 *   }
 * }
 * </code></pre>
 *
 * <p>(Note that there must be a class {@code MyMapKeyImpl} that implements {@code MyMapKey} in
 * order to call {@link Map#get(Object)} on the provided map.)
 *
 * @see <a href="https://dagger.dev/multibindings#map-multibindings">Map multibinding</a>
 */
@Documented
@Target(ANNOTATION_TYPE)
@Retention(RUNTIME)
public @interface MapKey {

    boolean unwrapValue() default true;
}
