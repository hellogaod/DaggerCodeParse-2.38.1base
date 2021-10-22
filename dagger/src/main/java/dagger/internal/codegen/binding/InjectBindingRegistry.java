package dagger.internal.codegen.binding;

import javax.inject.Inject;

import dagger.Component;
import dagger.Provides;

/**
 * Maintains the collection of provision bindings from {@link Inject} constructors and members
 * injection bindings from {@link Inject} fields and methods known to the annotation processor. Note
 * that this registry <b>does not</b> handle any explicit bindings (those from {@link Provides}
 * methods, {@link Component} dependencies, etc.).
 * <p>
 * 收集使用Inject注解的构造函数和成员注入绑定，成员注入绑定来源于使用Inject注解的变量和普通方法：
 * 1.这里构造函数使用Inject注解，等价于Module注解类使用Privider注解的方法
 * 2.成员注入绑定，表示当前类使用@Inject注解的变量（或普通方法）需要注入到当前类：
 *
 * e.g.如何注入？
 * 1.当前类A使用@Inject注解变量B
 * 2.①变量B的构造函数使用了Inject注解，或②变量B在Module注解类使用了Provider注解方法，返回类型是B
 */
public interface InjectBindingRegistry {
}
