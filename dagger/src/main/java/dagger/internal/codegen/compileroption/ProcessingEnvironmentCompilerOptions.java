package dagger.internal.codegen.compileroption;

import com.google.common.collect.ImmutableList;

import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;

import androidx.room.compiler.processing.XMessager;

import static dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions.KeyOnlyOption.HEADER_COMPILATION;
import static java.util.stream.Stream.concat;

/**
 * {@link CompilerOptions} for the given processor.
 */
public final class ProcessingEnvironmentCompilerOptions extends CompilerOptions {

    private final Map<String, String> options;

    @Inject
    ProcessingEnvironmentCompilerOptions(
            @ProcessingOptions Map<String, String> options
    ) {
        this.options = options;
    }

    @Override
    public boolean formatGeneratedSource() {
        return false;
    }

    @Override
    public boolean headerCompilation() {
        return isEnabled(HEADER_COMPILATION);
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
