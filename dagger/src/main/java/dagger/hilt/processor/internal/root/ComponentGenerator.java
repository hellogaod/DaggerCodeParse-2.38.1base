package dagger.hilt.processor.internal.root;


import com.google.common.base.Joiner;
import com.google.common.base.Utf8;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.Processors;

import static dagger.internal.codegen.extension.DaggerStreams.toImmutableList;
import static java.util.Comparator.comparing;

/**
 * Generates a Dagger component or subcomponent interface.
 */
final class ComponentGenerator {
    private static final Joiner JOINER = Joiner.on(".");
    private static final Comparator<ClassName> SIMPLE_NAME_SORTER =
            Comparator.comparing((ClassName c) -> JOINER.join(c.simpleNames()))
                    .thenComparing(ClassName::compareTo);
    private static final Comparator<TypeName> TYPE_NAME_SORTER = comparing(TypeName::toString);

    private final ProcessingEnvironment processingEnv;
    //component节点作为$CLASS_HiltComponents内部类
    private final ClassName name;
    //为空
    private final Optional<ClassName> superclass;
    //(1)component节点在ComponentDependencies对象中modulesBuilder；(2)component节点的子节点拼接"BuilderModule"生成的接口；
    private final ImmutableList<ClassName> modules;
    //(1)当前component节点在ComponentDependencies对象的entryPointsBuilder属性中匹配；
    //(2)GeneratedComponent;
    //(3)当前component节点；
    private final ImmutableList<TypeName> entryPoints;
    //当前component节点上使用的@Scope修饰的注解
    private final ImmutableCollection<ClassName> scopes;
    //空的
    private final ImmutableList<AnnotationSpec> extraAnnotations;
    //如果是是SingletonComponent使用Component，否则使用Subcomponent；
    private final ClassName componentAnnotation;

    //如果descriptor.creator存在，那么生成一个接口
//    @(Sub)Component.Builder
//    static interface Builder implements creator{
//
//    }
    private final Optional<TypeSpec> componentBuilder;

    public ComponentGenerator(
            ProcessingEnvironment processingEnv,
            ClassName name,
            Optional<ClassName> superclass,
            Set<? extends ClassName> modules,
            Set<? extends TypeName> entryPoints,
            ImmutableCollection<ClassName> scopes,
            ImmutableList<AnnotationSpec> extraAnnotations,
            ClassName componentAnnotation,
            Optional<TypeSpec> componentBuilder) {
        this.processingEnv = processingEnv;
        this.name = name;
        this.superclass = superclass;
        this.modules = modules.stream().sorted(SIMPLE_NAME_SORTER).collect(toImmutableList());
        this.entryPoints = entryPoints.stream().sorted(TYPE_NAME_SORTER).collect(toImmutableList());
        this.scopes = scopes;
        this.extraAnnotations = extraAnnotations;
        this.componentAnnotation = componentAnnotation;
        this.componentBuilder = componentBuilder;
    }

    public TypeSpec.Builder typeSpecBuilder() throws IOException {
        TypeSpec.Builder builder =
                TypeSpec.classBuilder(name)
                        // Public because components from a scope below must reference to create
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addAnnotation(getComponentAnnotation());

        componentBuilder.ifPresent(builder::addType);

        scopes.forEach(builder::addAnnotation);

        addEntryPoints(builder);

        superclass.ifPresent(builder::superclass);

        builder.addAnnotations(extraAnnotations);

        return builder;
    }

    /**
     * Returns the component annotation with the list of modules to install for the component.
     */
    private AnnotationSpec getComponentAnnotation() {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(componentAnnotation);
        modules.forEach(module -> builder.addMember("modules", "$T.class", module));
        return builder.build();
    }

    /**
     * Adds entry points to the component.
     * <p>
     * See b/140979968. If the entry points exceed 65763 bytes, we have to partition them to avoid the
     * limit. To be safe, we split at 60000 bytes.
     */
    private void addEntryPoints(TypeSpec.Builder builder) throws IOException {
        int currBytes = 0;
        List<Integer> partitionIndexes = new ArrayList<>();

        partitionIndexes.add(0);
        for (int i = 0; i < entryPoints.size(); i++) {
            // This over estimates the actual length because it includes the fully qualified name (FQN).
            // TODO(bcorso): Have a better way to estimate the upper bound. For example, most types will
            // not include the FQN, but we'll have to consider all of the different subtypes of TypeName,
            // simple name collisions, etc...
            int nextBytes = Utf8.encodedLength(entryPoints.get(i).toString());

            // To be safe, we split at 60000 to account for the component name, spaces, commas, etc...
            if (currBytes + nextBytes > 60000) {
                partitionIndexes.add(i);
                currBytes = 0;
            }

            currBytes += nextBytes;
        }
        partitionIndexes.add(entryPoints.size());

        if (partitionIndexes.size() <= 2) {
            // No extra partitions are needed, so just add all of the entrypoints as is.
            builder.addSuperinterfaces(entryPoints);
        } else {
            // Create interfaces for each partition.
            // The partitioned interfaces will be added to the component instead of the real entry points.
            for (int i = 1; i < partitionIndexes.size(); i++) {
                int startIndex = partitionIndexes.get(i - 1);
                int endIndex = partitionIndexes.get(i);
                builder.addSuperinterface(
                        createPartitionInterface(entryPoints.subList(startIndex, endIndex), i));
            }
        }
    }

    private ClassName createPartitionInterface(List<TypeName> partition, int partitionIndex)
            throws IOException {
        // TODO(bcorso): Nest the partion inside the HiltComponents wrapper rather than appending name
        ClassName partitionName =
                Processors.append(
                        Processors.getEnclosedClassName(name), "_EntryPointPartition" + partitionIndex);
        TypeSpec.Builder builder =
                TypeSpec.interfaceBuilder(partitionName)
                        .addModifiers(Modifier.ABSTRACT)
                        .addSuperinterfaces(partition);

        Processors.addGeneratedAnnotation(builder, processingEnv, ClassNames.ROOT_PROCESSOR.toString());

        JavaFile.builder(name.packageName(), builder.build()).build().writeTo(processingEnv.getFiler());
        return partitionName;
    }
}
