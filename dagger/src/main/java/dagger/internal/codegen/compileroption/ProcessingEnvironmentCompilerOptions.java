package dagger.internal.codegen.compileroption;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;

import androidx.room.compiler.processing.XMessager;
import dagger.internal.codegen.langmodel.DaggerElements;

import static dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions.KeyOnlyOption.HEADER_COMPILATION;
import static java.util.stream.Stream.concat;

/**
 * {@link CompilerOptions} for the given processor.
 */
public final class ProcessingEnvironmentCompilerOptions extends CompilerOptions {

    private final XMessager messager;
    private final Map<String, String> options;
    private final DaggerElements elements;
    private final Map<EnumOption<?>, Object> enumOptions = new HashMap<>();
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

    }


    /**
     * An option that can be set on the command line.
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

    @Override
    public boolean formatGeneratedSource() {
        return false;
    }

    @Override
    public boolean headerCompilation() {
        return isEnabled(HEADER_COMPILATION);
    }

    @Override
    public boolean experimentalDaggerErrorMessages() {
        return false;
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

    private boolean isEnabled(KeyOnlyOption keyOnlyOption) {
        return options.containsKey(keyOnlyOption.toString());
    }

    private interface CommandLineOption {
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
}
