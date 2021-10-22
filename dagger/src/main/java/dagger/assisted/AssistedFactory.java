package dagger.assisted;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotates an abstract class or interface used to create an instance of a type via an {@link
 * AssistedInject} constructor.
 *
 * <p>An {@link AssistedFactory}-annotated type must obey the following constraints:
 *
 * <ul>
 *   <li>The type must be an abstract class or interface,                           1.使用该注释的必须是abstract类或接口
 *   <li>The type must contain exactly one abstract, non-default method whose       2.该类型必须只包含一个抽象的、非默认的方法
 *                                                                                        (1)返回类型必须与辅助注入类型的类型完全匹配
 *                                                                                        (2)参数必须与辅助注入类型的构造函数中的 {@link Assisted} 参数的确切列表匹配（并且顺序相同）。
 *       <ul>
 *         <li>return type must exactly match the type of an assisted injection type, and
 *         <li>parameters must match the exact list of {@link Assisted} parameters in the assisted
 *             injection type's constructor (and in the same order).
 *       </ul>
 * </ul>
 * <p>
 * See {@link AssistedInject}
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface AssistedFactory {
}
