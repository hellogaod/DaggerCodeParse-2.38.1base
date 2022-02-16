package dagger.internal.codegen.binding;

import com.google.auto.common.MoreTypes;
import com.google.common.base.Ascii;
import com.google.common.base.CaseFormat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Provider;

import dagger.Lazy;
import dagger.spi.model.DependencyRequest;

import static dagger.internal.codegen.binding.SourceFiles.simpleVariableName;

/**
 * Picks a reasonable name for what we think is being provided from the variable name associated
 * with the {@link DependencyRequest}.  I.e. strips out words like "lazy" and "provider" if we
 * believe that those refer to {@link Lazy} and {@link Provider} rather than the type being
 * provided.
 */
//TODO(gak): develop the heuristics to get better names
final class DependencyVariableNamer {

    private static final Pattern LAZY_PROVIDER_PATTERN = Pattern.compile("lazy(\\w+)Provider");

    static String name(DependencyRequest dependency) {
        //如果绑定requestElement不存在，直接返回当前依赖参数（存储于Key的type中）
        if (!dependency.requestElement().isPresent()) {
            return simpleVariableName(MoreTypes.asTypeElement(dependency.key().type().java()));
        }

        String variableName = dependency.requestElement().get().java().getSimpleName().toString();
        if (Ascii.isUpperCase(variableName.charAt(0))) {
            variableName = toLowerCamel(variableName);
        }

        //返回的有效名：针对不同kind，裁剪掉针对部分代码，如果存在的情况下。
        switch (dependency.kind()) {
            case INSTANCE:
                return variableName;
            case LAZY:
                return variableName.startsWith("lazy") && !variableName.equals("lazy")
                        ? toLowerCamel(variableName.substring(4))
                        : variableName;
            case PROVIDER_OF_LAZY:
                Matcher matcher = LAZY_PROVIDER_PATTERN.matcher(variableName);
                if (matcher.matches()) {
                    return toLowerCamel(matcher.group(1));
                }
                // fall through
            case PROVIDER:
                return variableName.endsWith("Provider") && !variableName.equals("Provider")
                        ? variableName.substring(0, variableName.length() - 8)
                        : variableName;
            case PRODUCED:
                return variableName.startsWith("produced") && !variableName.equals("produced")
                        ? toLowerCamel(variableName.substring(8))
                        : variableName;
            case PRODUCER:
                return variableName.endsWith("Producer") && !variableName.equals("Producer")
                        ? variableName.substring(0, variableName.length() - 8)
                        : variableName;
            default:
                throw new AssertionError();
        }
    }

    //testData格式
    private static String toLowerCamel(String name) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
    }
}
