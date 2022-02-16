package dagger.internal.codegen.binding;

import static dagger.internal.codegen.base.ElementFormatter.elementToString;
import static dagger.internal.codegen.base.RequestKinds.requestType;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dagger.Provides;
import dagger.internal.codegen.base.Formatter;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.producers.Produces;
import dagger.spi.model.DaggerAnnotation;
import dagger.spi.model.DependencyRequest;
import java.util.Optional;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementKindVisitor8;

/**
 * Formats a {@link DependencyRequest} into a {@link String} suitable for an error message listing a
 * chain of dependencies.
 *
 * <dl>
 *   <dt>For component provision methods
 *   <dd>{@code @Qualifier SomeType is provided at\n ComponentType.method()}
 *   <dt>For component injection methods
 *   <dd>{@code SomeType is injected at\n ComponentType.method(foo)}
 *   <dt>For parameters to {@link Provides @Provides}, {@link Produces @Produces}, or {@link
 *       Inject @Inject} methods:
 *   <dd>{@code @Qualified ResolvedType is injected at\n EnclosingType.method([…, ]param[, …])}
 *   <dt>For parameters to {@link Inject @Inject} constructors:
 *   <dd>{@code @Qualified ResolvedType is injected at\n EnclosingType([…, ]param[, …])}
 *   <dt>For {@link Inject @Inject} fields:
 *   <dd>{@code @Qualified ResolvedType is injected at\n EnclosingType.field}
 * </dl>
 */
public final class DependencyRequestFormatter extends Formatter<DependencyRequest> {

    private final DaggerTypes types;

    @Inject
    DependencyRequestFormatter(DaggerTypes types) {
        this.types = types;
    }

    @Override
    public String format(DependencyRequest request) {
        return request
                .requestElement()
                .map(element -> element.java().accept(formatVisitor, request))
                .orElse("");
    }

    /**
     * Appends a newline and the formatted dependency request unless {@link
     * #format(DependencyRequest)} returns the empty string.
     */
    @CanIgnoreReturnValue
    public StringBuilder appendFormatLine(
            StringBuilder builder, DependencyRequest dependencyRequest) {
        String formatted = format(dependencyRequest);
        if (!formatted.isEmpty()) {
            builder.append('\n').append(formatted);
        }
        return builder;
    }

    private final ElementVisitor<String, DependencyRequest> formatVisitor =
            new ElementKindVisitor8<String, DependencyRequest>() {

                @Override
                public String visitExecutableAsMethod(ExecutableElement method, DependencyRequest request) {
                    return INDENT
                            + request.key()
                            + " is "
                            + componentMethodRequestVerb(request)
                            + " at\n"
                            + DOUBLE_INDENT
                            + elementToString(method);
                }

                @Override
                public String visitVariable(VariableElement variable, DependencyRequest request) {
                    TypeMirror requestedType =
                            requestType(request.kind(), request.key().type().java(), types);
                    return INDENT
                            + formatQualifier(request.key().qualifier())
                            + requestedType
                            + " is injected at\n"
                            + DOUBLE_INDENT
                            + elementToString(variable);
                }

                @Override
                public String visitType(TypeElement e, DependencyRequest request) {
                    return ""; // types by themselves provide no useful information.
                }

                @Override
                protected String defaultAction(Element element, DependencyRequest request) {
                    throw new IllegalStateException(
                            "Invalid request " + element.getKind() + " element " + element);
                }
            };

    private String formatQualifier(Optional<DaggerAnnotation> maybeQualifier) {
        return maybeQualifier.map(qualifier -> qualifier + " ").orElse("");
    }

    /**
     * Returns the verb for a component method dependency request. Returns "produced", "provided", or
     * "injected", depending on the kind of request.
     */
    private String componentMethodRequestVerb(DependencyRequest request) {
        switch (request.kind()) {
            case FUTURE:
            case PRODUCER:
            case INSTANCE:
            case LAZY:
            case PROVIDER:
            case PROVIDER_OF_LAZY:
                return "requested";

            case MEMBERS_INJECTION:
                return "injected";

            case PRODUCED:
                break;
        }
        throw new AssertionError("illegal request kind for method: " + request);
    }
}
