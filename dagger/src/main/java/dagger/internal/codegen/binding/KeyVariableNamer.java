package dagger.internal.codegen.binding;

import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableSet;

import java.util.Iterator;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleTypeVisitor8;

import dagger.spi.model.DependencyRequest;
import dagger.spi.model.Key;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static dagger.internal.codegen.binding.SourceFiles.protectAgainstKeywords;

/**
 * Suggests a variable name for a type based on a {@link Key}. Prefer {@link
 * DependencyVariableNamer} for cases where a specific {@link DependencyRequest} is present.
 */
public final class KeyVariableNamer {
    /**
     * Simple names that are very common. Inspired by https://errorprone.info/bugpattern/BadImport
     */
    private static final ImmutableSet<String> VERY_SIMPLE_NAMES =
            ImmutableSet.of(
                    "Builder",
                    "Factory",
                    "Component",
                    "Subcomponent",
                    "Injector");

    private static final TypeVisitor<Void, StringBuilder> TYPE_NAMER =
            new SimpleTypeVisitor8<Void, StringBuilder>() {
                @Override
                public Void visitDeclared(DeclaredType declaredType, StringBuilder builder) {

                    TypeElement element = MoreTypes.asTypeElement(declaredType);

                    //如果节点是内部类，并且使用了VERY_SIMPLE_NAMES集合中的命名，那么还需要加入该节点的父节点名
                    if (element.getNestingKind().isNested()
                            && VERY_SIMPLE_NAMES.contains(element.getSimpleName().toString())) {
                        builder.append(element.getEnclosingElement().getSimpleName());
                    }

                    builder.append(element.getSimpleName());

                    //如果存在泛型
                    Iterator<? extends TypeMirror> argumentIterator =
                            declaredType.getTypeArguments().iterator();
                    if (argumentIterator.hasNext()) {
                        builder.append("Of");
                        TypeMirror first = argumentIterator.next();
                        first.accept(this, builder);
                        while (argumentIterator.hasNext()) {
                            builder.append("And");
                            argumentIterator.next().accept(this, builder);
                        }
                    }
                    return null;
                }

                @Override
                public Void visitPrimitive(PrimitiveType type, StringBuilder builder) {
                    //如果是原始类型使用TestData格式
                    builder.append(LOWER_CAMEL.to(UPPER_CAMEL, type.toString()));
                    return null;
                }

                @Override
                public Void visitArray(ArrayType type, StringBuilder builder) {
                    //getComponentType():数组类型
                    type.getComponentType().accept(this, builder);
                    builder.append("Array");
                    return null;
                }
            };

    private KeyVariableNamer() {
    }

    public static String name(Key key) {
        //如果key是多重绑定，直接返回多重绑定的绑定节点
        if (key.multibindingContributionIdentifier().isPresent()) {
            return key.multibindingContributionIdentifier().get().bindingElement();
        }

        StringBuilder builder = new StringBuilder();

        //如果key存在Qualifier注解修饰的注解，那么获取该注解的名称
        if (key.qualifier().isPresent()) {
            // TODO(gak): Use a better name for fields with qualifiers with members.
            builder.append(key.qualifier().get().java().getAnnotationType().asElement().getSimpleName());
        }

        key.type().java().accept(TYPE_NAMER, builder);

        return protectAgainstKeywords(UPPER_CAMEL.to(LOWER_CAMEL, builder.toString()));
    }
}
