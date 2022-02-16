package dagger.internal.codegen.binding;

import java.util.Comparator;
import java.util.Optional;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.spi.model.BindingKind;
import dagger.spi.model.Key;

import static dagger.internal.codegen.extension.Optionals.emptiesLast;
import static java.util.Comparator.comparing;

/**
 * An object that declares or specifies a binding.
 * <p>
 * 声明或指定的绑定对象
 */
public abstract class BindingDeclaration {

    /**
     * A comparator that compares binding declarations with elements.
     * <p>
     * 将绑定声明与元素进行比较的
     *
     * <p>Compares, in order:
     *
     * <ol>
     *   <li>Contributing module or enclosing type name
     *   <li>Binding element's simple name
     *   <li>Binding element's type
     * </ol>
     * <p>
     * Any binding declarations without elements are last.
     * <p>
     * 任何没有元素的绑定声明都放在最后。
     */
    public static final Comparator<BindingDeclaration> COMPARATOR =
            comparing(
                    //contributingModule如果不存在，使用bindingTypeElement
                    (BindingDeclaration declaration) ->
                            declaration.contributingModule().isPresent()
                                    ? declaration.contributingModule()
                                    : declaration.bindingTypeElement(),
                    //节点名称
                    emptiesLast(comparing((TypeElement type) -> type.getQualifiedName().toString())))

                    .thenComparing(
                            (BindingDeclaration declaration) -> declaration.bindingElement(),
                            emptiesLast(
                                    comparing((Element element) -> element.getSimpleName().toString())
                                            .thenComparing((Element element) -> element.asType().toString())));


    /**
     * The {@link Key} of this declaration.
     * <p>
     * 当前绑定声明表示的key
     */
    public abstract Key key();

    /**
     * The {@link Element} that declares this binding. Absent for {@linkplain BindingKind binding
     * kinds} that are not always declared by exactly one element.
     *
     * <p>For example, consider {@link BindingKind#MULTIBOUND_SET}. A component with many
     * {@code @IntoSet} bindings for the same key will have a synthetic binding that depends on all
     * contributions, but with no identifiying binding element. A {@code @Multibinds} method will also
     * contribute a synthetic binding, but since multiple {@code @Multibinds} methods can coexist in
     * the same component (and contribute to one single binding), it has no binding element.
     * <p>
     * 声明的绑定
     */
    public abstract Optional<Element> bindingElement();

    /**
     * The type enclosing the {@link #bindingElement()}, or {@link Optional#empty()} if {@link
     * #bindingElement()} is empty.
     * <p>
     * 绑定节点所在类
     */
    public final Optional<TypeElement> bindingTypeElement() {
        return bindingElement().map(DaggerElements::closestEnclosingTypeElement);
    }

    /**
     * The installed module class that contributed the {@link #bindingElement()}. May be a subclass of
     * the class that contains {@link #bindingElement()}. Absent if {@link #bindingElement()} is
     * empty.
     * <p>
     * 表示bindingElement绑定节点所在module 类
     */
    public abstract Optional<TypeElement> contributingModule();
}
