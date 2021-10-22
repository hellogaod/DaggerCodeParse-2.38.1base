package dagger;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotates <em>abstract</em> methods of a {@link Module} that delegate bindings. For example, to
 * bind {@link java.util.Random} to {@link java.security.SecureRandom} a module could declare the
 * following: {@code @Binds abstract Random bindRandom(SecureRandom secureRandom);}
 *
 * <p>{@code @Binds} methods are a drop-in replacement for {@link Provides} methods that simply
 * return an injected parameter. Prefer {@code @Binds} because the generated implementation is
 * likely to be more efficient.
 *
 * <p>A {@code @Binds} method:  Binds注解使用规则
 *
 * <ul>
 *   <li>Must be {@code abstract}.                              1.必须是abstract修饰或是接口类的普通方法（非default修饰）
 *   <li>May be {@linkplain javax.inject.Scope scoped}.         2.同时可以使用Scope注解修饰
 *   <li>May be {@linkplain javax.inject.Qualifier qualified}.  3.同时可以使用Qualifier注解修饰
 *   <li>Must have a single parameter whose type is assignable to the return type. The return type
 *       declares the bound type (just as it would for a {@literal @}{@link Provides} method)
 *       and the parameter is the type to which it is bound.    4.必须有且仅有一个参数，并且参数类型是返回类型的子类型（或同类）
 *       <p>For {@linkplain dagger.multibindings multibindings}, assignability is checked in similar
 *       ways:                                                  5.如果方法同时被multibindings修饰
 *       <dl>
 *         <dt>{@link dagger.multibindings.IntoSet}                 ①被IntoSet注解修饰：参数必须可分配给返回类型,即参数可以通过Set#add给返回类型
 *         <dd>The parameter must be assignable to the only parameter of {@link java.util.Set#add}
 *             when viewed as a member of the return type — the parameter must be assignable to the
 *             return type.
 *         <dt>{@link dagger.multibindings.ElementsIntoSet}         ②被ElementsIntoSet修饰：参数必须可以通过Set#addAll分配给返回类型，参数是Set类型
 *         <dd>The parameter must be assignable to the only parameter of {@link
 *             java.util.Set#addAll} when viewed as a member of the return type — if the return type
 *             is {@code Set<E>}, the parameter must be assignable to {@code Collection<? extends
 *             E>}.
 *         <dt>{@link dagger.multibindings.IntoMap}                 ③如果被IntoMap修饰：参数必须可以分配给Map的value
 *         <dd>The parameter must be assignable to the {@code value} parameter of {@link
 *             java.util.Map#put} when viewed as a member of a {@link java.util.Map} in which {@code
 *             V} is bound to the return type — the parameter must be assignable to the return type
 *       </dl>
 * </ul>
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface Binds {
}
