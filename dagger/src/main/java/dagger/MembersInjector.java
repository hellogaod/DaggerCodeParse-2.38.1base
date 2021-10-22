package dagger;


/**
 * Injects dependencies into the fields and methods on instances of type {@code T}. Ignores the
 * presence or absence of an injectable constructor.
 * <p>
 * 将依赖项注入 {@code T} 类型实例的字段和方法中。 忽略可注入构造函数的存在与否。
 *
 * @param <T> type to inject members of
 * @since 2.0 (since 1.0 without the provision that {@link #injectMembers} cannot accept
 * {@code null})
 * <p>
 * e.g.ComponentProcessor使用了@Injector注解，那么针对ComponentProcessor会生成ComponentProcessor_MembersInjector，
 * 用于将@Inject注解的遍历赋值给ComponentProcessor
 */
public interface MembersInjector<T> {

    /**
     * Injects dependencies into the fields and methods of {@code instance}. Ignores the presence or
     * absence of an injectable constructor.
     *
     * <p>Whenever a {@link Component} creates an instance, it performs this injection automatically
     * (after first performing constructor injection), so if you're able to let the component create
     * all your objects for you, you'll never need to use this method.
     *
     * @param instance into which members are to be injected
     * @throws NullPointerException if {@code instance} is {@code null}
     */
    void injectMembers(T instance);
}
