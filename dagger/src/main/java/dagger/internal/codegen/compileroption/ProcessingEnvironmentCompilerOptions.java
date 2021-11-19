package dagger.internal.codegen.compileroption;

import com.google.common.base.Ascii;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import androidx.room.compiler.processing.XMessager;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.producers.Produces;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Sets.immutableEnumSet;
import static dagger.internal.codegen.compileroption.FeatureStatus.DISABLED;
import static dagger.internal.codegen.compileroption.FeatureStatus.ENABLED;
import static dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions.Feature.EXPERIMENTAL_AHEAD_OF_TIME_SUBCOMPONENTS;
import static dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions.Feature.EXPERIMENTAL_ANDROID_MODE;
import static dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions.Feature.EXPERIMENTAL_DAGGER_ERROR_MESSAGES;
import static dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions.Feature.FAST_INIT;
import static dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions.Feature.FLOATING_BINDS_METHODS;
import static dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions.Feature.FORMAT_GENERATED_SOURCE;
import static dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions.Feature.IGNORE_PRIVATE_AND_STATIC_INJECTION_FOR_COMPONENT;
import static dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions.Feature.PLUGINS_VISIT_FULL_BINDING_GRAPHS;
import static dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions.Feature.STRICT_MULTIBINDING_VALIDATION;
import static dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions.Feature.VALIDATE_TRANSITIVE_COMPONENT_DEPENDENCIES;
import static dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions.Feature.WARN_IF_INJECTION_FACTORY_NOT_GENERATED_UPSTREAM;
import static dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions.Feature.WRITE_PRODUCER_NAME_IN_TOKEN;
import static dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions.KeyOnlyOption.HEADER_COMPILATION;
import static dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions.KeyOnlyOption.USE_GRADLE_INCREMENTAL_PROCESSING;
import static dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions.Validation.DISABLE_INTER_COMPONENT_SCOPE_VALIDATION;
import static dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions.Validation.EXPLICIT_BINDING_CONFLICTS_WITH_INJECT;
import static dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions.Validation.FULL_BINDING_GRAPH_VALIDATION;
import static dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions.Validation.MODULE_HAS_DIFFERENT_SCOPES_VALIDATION;
import static dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions.Validation.NULLABLE_VALIDATION;
import static dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions.Validation.PRIVATE_MEMBER_VALIDATION;
import static dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions.Validation.STATIC_MEMBER_VALIDATION;
import static dagger.internal.codegen.compileroption.ValidationType.ERROR;
import static dagger.internal.codegen.compileroption.ValidationType.NONE;
import static dagger.internal.codegen.compileroption.ValidationType.WARNING;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;

/**
 * {@link CompilerOptions} for the given processor.
 *
 * 处理编译时命令行参数收集以及一些禁用和启用指令影响编译导向
 */
public final class ProcessingEnvironmentCompilerOptions extends CompilerOptions {
    // EnumOption<T> doesn't support integer inputs so just doing this as a 1-off for now.
    private static final String KEYS_PER_COMPONENT_SHARD = "dagger.keysPerComponentShard";

    private final XMessager messager;
    private final Map<String, String> options;//存放支持的命令集合
    private final DaggerElements elements;
    private final Map<EnumOption<?>, Object> enumOptions = new HashMap<>();//哪些启用哪些禁用信息
    private final Map<EnumOption<?>, ImmutableMap<String, ? extends Enum<?>>> allCommandLineOptions =
            new HashMap<>();

    @Inject
    ProcessingEnvironmentCompilerOptions(
            XMessager messager,
            @ProcessingOptions Map<String, String> options,
            DaggerElements elements
    ) {
        this.messager = messager;
        this.options = options;
        this.elements = elements;
        checkValid();
    }

    @Override
    public boolean usesProducers() {
        return elements.getTypeElement(Produces.class.getCanonicalName()) != null;
    }

    @Override
    public boolean headerCompilation() {
        return isEnabled(HEADER_COMPILATION);
    }

    @Override
    public boolean fastInit(TypeElement component) {
        return isEnabled(FAST_INIT);
    }

    @Override
    public boolean formatGeneratedSource() {
        return isEnabled(FORMAT_GENERATED_SOURCE);
    }

    @Override
    public boolean writeProducerNameInToken() {
        return isEnabled(WRITE_PRODUCER_NAME_IN_TOKEN);
    }

    @Override
    public Diagnostic.Kind nullableValidationKind() {
        return diagnosticKind(NULLABLE_VALIDATION);
    }

    @Override
    public Diagnostic.Kind privateMemberValidationKind() {
        return diagnosticKind(PRIVATE_MEMBER_VALIDATION);
    }

    @Override
    public Diagnostic.Kind staticMemberValidationKind() {
        return diagnosticKind(STATIC_MEMBER_VALIDATION);
    }

    @Override
    public boolean ignorePrivateAndStaticInjectionForComponent() {
        return isEnabled(IGNORE_PRIVATE_AND_STATIC_INJECTION_FOR_COMPONENT);
    }

    @Override
    public ValidationType scopeCycleValidationType() {
        return parseOption(DISABLE_INTER_COMPONENT_SCOPE_VALIDATION);
    }

    @Override
    public boolean validateTransitiveComponentDependencies() {
        return isEnabled(VALIDATE_TRANSITIVE_COMPONENT_DEPENDENCIES);
    }

    @Override
    public boolean warnIfInjectionFactoryNotGeneratedUpstream() {
        return isEnabled(WARN_IF_INJECTION_FACTORY_NOT_GENERATED_UPSTREAM);
    }

    @Override
    public ValidationType fullBindingGraphValidationType() {
        return parseOption(FULL_BINDING_GRAPH_VALIDATION);
    }

    @Override
    public boolean pluginsVisitFullBindingGraphs(TypeElement component) {
        return isEnabled(PLUGINS_VISIT_FULL_BINDING_GRAPHS);
    }

    @Override
    public Diagnostic.Kind moduleHasDifferentScopesDiagnosticKind() {
        return diagnosticKind(MODULE_HAS_DIFFERENT_SCOPES_VALIDATION);
    }

    @Override
    public ValidationType explicitBindingConflictsWithInjectValidationType() {
        return parseOption(EXPLICIT_BINDING_CONFLICTS_WITH_INJECT);
    }

    @Override
    public boolean experimentalDaggerErrorMessages() {
        return isEnabled(EXPERIMENTAL_DAGGER_ERROR_MESSAGES);
    }

    @Override
    public boolean strictMultibindingValidation() {
        return isEnabled(STRICT_MULTIBINDING_VALIDATION);
    }

    @Override
    public int keysPerComponentShard(TypeElement component) {
        if (options.containsKey(KEYS_PER_COMPONENT_SHARD)) {
            checkArgument(
                    com.google.auto.common.MoreElements.getPackage(component)
                            .getQualifiedName().toString().startsWith("dagger."),
                    "Cannot set %s. It is only meant for internal testing.", KEYS_PER_COMPONENT_SHARD);
            return Integer.parseInt(options.get(KEYS_PER_COMPONENT_SHARD));
        }
        return super.keysPerComponentShard(component);
    }

    private boolean isEnabled(KeyOnlyOption keyOnlyOption) {
        return options.containsKey(keyOnlyOption.toString());
    }

    private boolean isEnabled(Feature feature) {
        return parseOption(feature).equals(ENABLED);
    }

    private Diagnostic.Kind diagnosticKind(Validation validation) {
        return parseOption(validation).diagnosticKind().get();
    }

    @SuppressWarnings("CheckReturnValue")
    private ProcessingEnvironmentCompilerOptions checkValid() {

        for (KeyOnlyOption keyOnlyOption : KeyOnlyOption.values()) {
            isEnabled(keyOnlyOption);
        }
        for (Feature feature : Feature.values()) {
            parseOption(feature);
        }
        for (Validation validation : Validation.values()) {
            parseOption(validation);
        }
        noLongerRecognized(EXPERIMENTAL_ANDROID_MODE);
        noLongerRecognized(FLOATING_BINDS_METHODS);
        noLongerRecognized(EXPERIMENTAL_AHEAD_OF_TIME_SUBCOMPONENTS);
        noLongerRecognized(USE_GRADLE_INCREMENTAL_PROCESSING);
        return this;
    }

    private void noLongerRecognized(CommandLineOption commandLineOption) {
        if (options.containsKey(commandLineOption.toString())) {
            messager.printMessage(
                    Diagnostic.Kind.WARNING, commandLineOption + " is no longer recognized by Dagger");
        }
    }

    private interface CommandLineOption {//命令行选项

        /**
         * The key of the option (appears after "-A").
         */
        @Override
        String toString();

        /**
         * Returns all aliases besides {@link #toString()}, such as old names for an option, in order of
         * precedence.
         */
        default ImmutableList<String> aliases() {
            return ImmutableList.of();
        }

        /**
         * All the command-line names for this option, in order of precedence.
         */
        default Stream<String> allNames() {
            return concat(Stream.of(toString()), aliases().stream());
        }
    }

    /**
     * An option that can be set on the command line.
     * <p>
     * 可以在命令行上设置的选项。
     */
    private interface EnumOption<E extends Enum<E>> extends CommandLineOption {
        /**
         * The default value for this option.
         */
        E defaultValue();

        /**
         * The valid values for this option.
         */
        Set<E> validValues();
    }

    enum KeyOnlyOption implements CommandLineOption {
        HEADER_COMPILATION {
            @Override
            public String toString() {
                return "experimental_turbine_hjar";
            }
        },

        USE_GRADLE_INCREMENTAL_PROCESSING {
            @Override
            public String toString() {
                return "dagger.gradle.incremental";
            }
        },
    }

    /**
     * A feature that can be enabled or disabled on the command line by setting {@code -Akey=ENABLED}
     * or {@code -Akey=DISABLED}.
     * <p>
     * 可以通过设置 {@code -Akey=ENABLED} 或 {@code -Akey=DISABLED} 在命令行上启用或禁用的功能。
     */
    enum Feature implements EnumOption<FeatureStatus> {
        FAST_INIT,//fast init

        EXPERIMENTAL_ANDROID_MODE, // experimental android mode 实验安卓模式

        FORMAT_GENERATED_SOURCE,// format generated source

        WRITE_PRODUCER_NAME_IN_TOKEN,// write producer name in token

        WARN_IF_INJECTION_FACTORY_NOT_GENERATED_UPSTREAM,// warn if injection factory not generated upstream

        IGNORE_PRIVATE_AND_STATIC_INJECTION_FOR_COMPONENT,// ignore private and static injection for component

        EXPERIMENTAL_AHEAD_OF_TIME_SUBCOMPONENTS,// experimental ahead of time subcomponent

        FORCE_USE_SERIALIZED_COMPONENT_IMPLEMENTATIONS, // force use serialized component implementations

        EMIT_MODIFIABLE_METADATA_ANNOTATIONS(ENABLED),// emit modifiable metadata annotations(enabled)

        PLUGINS_VISIT_FULL_BINDING_GRAPHS,// plugins visit full binding graphs

        FLOATING_BINDS_METHODS,// floating binds methods

        EXPERIMENTAL_DAGGER_ERROR_MESSAGES,// experimental dagger error messages

        STRICT_MULTIBINDING_VALIDATION,// strict multibinding validation

        VALIDATE_TRANSITIVE_COMPONENT_DEPENDENCIES(ENABLED);// validate transitive component dependencies(enabled)

        final FeatureStatus defaultValue;

        Feature() {
            this(DISABLED);//默认都是禁用的
        }

        Feature(FeatureStatus defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public FeatureStatus defaultValue() {
            return defaultValue;
        }

        @Override
        public Set<FeatureStatus> validValues() {
            return EnumSet.allOf(FeatureStatus.class);
        }

        @Override
        public String toString() {
            return optionName(this);
        }
    }

    /**
     * The diagnostic kind or validation type for a kind of validation.
     */
    enum Validation implements EnumOption<ValidationType> {
        DISABLE_INTER_COMPONENT_SCOPE_VALIDATION(),// disable inter component scope validation

        NULLABLE_VALIDATION(ERROR, WARNING),// nullable validation(error,warning)

        PRIVATE_MEMBER_VALIDATION(ERROR, WARNING),// private member validation(error,warning)

        STATIC_MEMBER_VALIDATION(ERROR, WARNING),// static member validation(error,warning)

        /**
         * Whether to validate full binding graphs for components, subcomponents, and modules.
         */
        FULL_BINDING_GRAPH_VALIDATION(NONE, ERROR, WARNING) {// full binding graph validation(none,error,warning)

            @Override
            public ImmutableList<String> aliases() {
                return ImmutableList.of("dagger.moduleBindingValidation");
            }
        },

        /**
         * How to report conflicting scoped bindings when validating partial binding graphs associated
         * with modules.
         */
        MODULE_HAS_DIFFERENT_SCOPES_VALIDATION(ERROR, WARNING),// module has different scopes validation(error,warning)

        /**
         * How to report that an explicit binding in a subcomponent conflicts with an {@code @Inject}
         * constructor used in an ancestor component.
         */
        EXPLICIT_BINDING_CONFLICTS_WITH_INJECT(WARNING, ERROR, NONE),// explicit binding confilicts with inject(warning,error,none)
        ;

        final ValidationType defaultType;
        final ImmutableSet<ValidationType> validTypes;

        Validation() {
            this(ERROR, WARNING, NONE);
        }

        //默认都报错
        Validation(ValidationType defaultType, ValidationType... moreValidTypes) {
            this.defaultType = defaultType;
            this.validTypes = immutableEnumSet(defaultType, moreValidTypes);
        }

        @Override
        public ValidationType defaultValue() {
            return defaultType;
        }

        @Override
        public Set<ValidationType> validValues() {
            return validTypes;
        }

        @Override
        public String toString() {
            return optionName(this);
        }
    }

    private static String optionName(Enum<? extends EnumOption<?>> option) {

        return "dagger." + UPPER_UNDERSCORE.to(LOWER_CAMEL, option.name());
    }

    /**
     * Returns the value for the option as set on the command line by any name, or the default value
     * if not set.
     *
     * <p>If more than one name is used to set the value, but all names specify the same value,
     * reports a warning and returns that value.
     *
     * <p>If more than one name is used to set the value, and not all names specify the same value,
     * reports an error and returns the default value.
     */
    private <T extends Enum<T>> T parseOption(EnumOption<T> option) {
        @SuppressWarnings("unchecked") // we only put covariant values into the map
        T value = (T) enumOptions.computeIfAbsent(option, this::parseOptionUncached);
        return value;
    }


    private <T extends Enum<T>> T parseOptionUncached(EnumOption<T> option) {
        ImmutableMap<String, T> values = parseOptionWithAllNames(option);

        // If no value is specified, return the default value.
        if (values.isEmpty()) {
            return option.defaultValue();
        }

        // If all names have the same value, return that.
        if (values.asMultimap().inverse().keySet().size() == 1) {
            // Warn if an option was set with more than one name. That would be an error if the values
            // differed.
            if (values.size() > 1) {//表示存在重复命令
                reportUseOfDifferentNamesForOption(Diagnostic.Kind.WARNING, option, values.keySet());
            }
            return values.values().asList().get(0);
        }

        // If different names have different values, report an error and return the default
        // value.
        reportUseOfDifferentNamesForOption(Diagnostic.Kind.ERROR, option, values.keySet());
        return option.defaultValue();
    }

    //提示存在重复命令
    private void reportUseOfDifferentNamesForOption(
            Diagnostic.Kind diagnosticKind, EnumOption<?> option, ImmutableSet<String> usedNames) {
        messager.printMessage(
                diagnosticKind,
                String.format(
                        "Only one of the equivalent options (%s) should be used; prefer -A%s",
                        usedNames.stream().map(name -> "-A" + name).collect(joining(", ")), option));
    }

    //选项命令在allCommandLineOptions中查找，不存在则在parseOptionWithAllNamesUncached方法中解析后再存储于allCommandLineOptions中
    private <T extends Enum<T>> ImmutableMap<String, T> parseOptionWithAllNames(
            EnumOption<T> option) {
        @SuppressWarnings("unchecked") // map is covariant
        ImmutableMap<String, T> aliasValues =
                (ImmutableMap<String, T>)
                        allCommandLineOptions.computeIfAbsent(option, this::parseOptionWithAllNamesUncached);
        return aliasValues;
    }

    //在option参数首先在options集合中找存在的信息，然后以命令name，value形式返回一个命令选项的Map对象
    private <T extends Enum<T>> ImmutableMap<String, T> parseOptionWithAllNamesUncached(
            EnumOption<T> option) {
        ImmutableMap.Builder<String, T> values = ImmutableMap.builder();
        getUsedNames(option)
                .forEach(
                        name -> parseOptionWithName(option, name).ifPresent(value -> values.put(name, value)));
        return values.build();
    }

    //1.检查options集合中存在key，并且key能找到对应的value值
    //2.在option选项中找key对应的value值，如果存在返回Optional.of(value)，否则返回Optional.empty()
    private <T extends Enum<T>> Optional<T> parseOptionWithName(EnumOption<T> option, String key) {
        checkArgument(options.containsKey(key), "key %s not found", key);
        String stringValue = options.get(key);
        if (stringValue == null) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Processor option -A" + key + " needs a value");
        } else {
            try {
                T value =
                        Enum.valueOf(option.defaultValue().getDeclaringClass(), Ascii.toUpperCase(stringValue));
                if (option.validValues().contains(value)) {
                    return Optional.of(value);
                }
            } catch (IllegalArgumentException e) {
                // handled below
            }
            messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    String.format(
                            "Processor option -A%s may only have the values %s (case insensitive), found: %s",
                            key, option.validValues(), stringValue));
        }
        return Optional.empty();
    }

    //在命令行选项对象所有命令中查找options集合的K存在的值集合
    private Stream<String> getUsedNames(CommandLineOption option) {
        return option.allNames().filter(options::containsKey);
    }
}
