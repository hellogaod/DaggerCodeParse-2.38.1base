package dagger.internal.codegen.validation;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.Traverser;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;

import java.util.Optional;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

import androidx.room.compiler.processing.XAnnotation;
import androidx.room.compiler.processing.XAnnotationValue;
import androidx.room.compiler.processing.XElement;
import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.compat.XConverters;
import dagger.internal.codegen.langmodel.DaggerElements;

import dagger.internal.codegen.base.ElementFormatter;

import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;
import static javax.tools.Diagnostic.Kind.WARNING;

/**
 * A collection of issues to report for source code.
 * <p>
 * 收集不同注解处理逻辑的错误信息。
 * <p>
 * Item对象是收集的节点信息存储对象，存储当前节点，信息，信息类型（error，warning，note三种），注解（选填），注解值（选填）
 * <p>
 * ValidationReport存放：
 * 1.当前节点subject；
 * 2.当前节点的节点信息集合items，
 * 3.当前节点里面涉及到的节点的ValidationReport subreports
 * 4. 如果存在重大错误，直接设置markedDirty = true
 * 5.最终的信息打印是printMessagesTo方法，通过XMessager对象，打印前线设置hasPrintedErrors = true表示打印了，防止出现打印多次的情况
 * <p>
 * items和subreports区别在哪？？？
 * items针对当前类的元素信息收集；而subreports主要针对当前类的一级级父类里面的元素收集；当然了
 */
public class ValidationReport {

    private static final Traverser<ValidationReport> SUBREPORTS =
            Traverser.forTree(report -> report.subreports);

    private final Element subject;
    private final ImmutableSet<Item> items;
    private final ImmutableSet<ValidationReport> subreports;
    private final boolean markedDirty;
    private boolean hasPrintedErrors;

    private ValidationReport(
            Element subject,
            ImmutableSet<Item> items,
            ImmutableSet<ValidationReport> subreports,
            boolean markedDirty) {
        this.subject = subject;
        this.items = items;
        this.subreports = subreports;
        this.markedDirty = markedDirty;
    }

    /**
     * Returns the items from this report and all transitive subreports.
     */
    public ImmutableSet<Item> allItems() {
        return ImmutableSet.copyOf(SUBREPORTS.depthFirstPreOrder(this))
                .stream()
                .flatMap(report -> report.items.stream())
                .collect(toImmutableSet());
    }

    /**
     * Returns {@code true} if there are no errors in this report or any subreports and markedDirty is
     * {@code false}.
     */
    public boolean isClean() {
        if (markedDirty) {
            return false;
        }
        for (Item item : items) {
            switch (item.kind()) {
                case ERROR:
                    return false;
                default:
                    break;
            }
        }
        for (ValidationReport subreport : subreports) {
            if (!subreport.isClean()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Prints all messages to {@code messager} (and recurs for subreports). If a message's {@linkplain
     * Item#element() element} is contained within the report's subject, associates the message with
     * the message's element. Otherwise, since {@link Diagnostic} reporting is expected to be
     * associated with elements that are currently being compiled, associates the message with the
     * subject itself and prepends a reference to the item's element.
     * <p>
     * 消息最终通过messager打印
     */
    public void printMessagesTo(XMessager messager) {
        printMessagesTo(XConverters.toJavac(messager));
    }

    /**
     * Prints all messages to {@code messager} (and recurs for subreports). If a
     * message's {@linkplain Item#element() element} is contained within the report's subject,
     * associates the message with the message's element. Otherwise, since {@link Diagnostic}
     * reporting is expected to be associated with elements that are currently being compiled,
     * associates the message with the subject itself and prepends a reference to the item's element.
     */
    public void printMessagesTo(Messager messager) {
        if (hasPrintedErrors) {
            // Avoid printing the errors from this validation report more than once.
            // 避免多次打印此验证报告中的错误。
            return;
        }
        hasPrintedErrors = true;

        //将收集到的item报告打印
        for (Item item : items) {
            //item及其父类能找到subject节点
            if (DaggerElements.transitivelyEncloses(subject, item.element())) {
                //item存在注解
                if (item.annotation().isPresent()) {
                    //item注解有值
                    if (item.annotationValue().isPresent()) {
                        messager.printMessage(
                                item.kind(),
                                item.message(),
                                item.element(),
                                item.annotation().get(),
                                item.annotationValue().get());
                    } else {
                        messager.printMessage(
                                item.kind(), item.message(), item.element(), item.annotation().get());
                    }
                } else {
                    messager.printMessage(item.kind(), item.message(), item.element());
                }
            } else {
                String message = String.format("[%s] %s", ElementFormatter.elementToString(item.element()), item.message());
                messager.printMessage(item.kind(), message, subject);
            }
        }

        //当前节点的子节点验证报告，例如当前类里面包含其他类的校验报告
        for (ValidationReport subreport : subreports) {
            subreport.printMessagesTo(messager);
        }
    }

    /**
     * Metadata about a {@link ValidationReport} item.
     * <p>
     * 收集单个报告信息，包括节点，信息类型（错误还是警告...），信息，注解，注解值
     */
    @AutoValue
    public abstract static class Item {
        public abstract String message();

        public abstract Diagnostic.Kind kind();

        public abstract Element element();

        public abstract Optional<AnnotationMirror> annotation();

        abstract Optional<AnnotationValue> annotationValue();
    }

    //入口1
    public static Builder about(Element subject) {
        return new Builder(subject);
    }

    //入口2
    public static Builder about(XElement subject) {
        return new Builder(XConverters.toJavac(subject));
    }


    /**
     * A {@link ValidationReport} builder.
     */
    @CanIgnoreReturnValue
    public static final class Builder {
        private final Element subject;
        private final ImmutableSet.Builder<Item> items = ImmutableSet.builder();
        private final ImmutableSet.Builder<ValidationReport> subreports = ImmutableSet.builder();
        private boolean markedDirty;

        private Builder(Element subject) {
            this.subject = subject;
        }

        @CheckReturnValue
        Element getSubject() {
            return subject;
        }

        Builder addItems(Iterable<Item> newItems) {
            items.addAll(newItems);
            return this;
        }

        public Builder addError(String message) {
            return addError(message, subject);
        }

        public Builder addError(String message, Element element) {
            return addItem(message, ERROR, element);
        }

        public Builder addError(String message, Element element, AnnotationMirror annotation) {
            return addItem(message, ERROR, element, annotation);
        }

        public Builder addError(
                String message,
                Element element,
                AnnotationMirror annotation,
                AnnotationValue annotationValue) {
            return addItem(message, ERROR, element, annotation, annotationValue);
        }

        public Builder addError(String message, XElement element) {
            return addItem(message, ERROR, element);
        }

        public Builder addError(String message, XElement element, XAnnotation annotation) {
            return addItem(message, ERROR, element, annotation);
        }

        public Builder addError(
                String message,
                XElement element,
                XAnnotation annotation,
                XAnnotationValue annotationValue) {
            return addItem(message, ERROR, element, annotation, annotationValue);
        }

        Builder addWarning(String message) {
            return addWarning(message, subject);
        }

        Builder addWarning(String message, Element element) {
            return addItem(message, WARNING, element);
        }

        Builder addWarning(String message, Element element, AnnotationMirror annotation) {
            return addItem(message, WARNING, element, annotation);
        }

        Builder addWarning(
                String message,
                Element element,
                AnnotationMirror annotation,
                AnnotationValue annotationValue) {
            return addItem(message, WARNING, element, annotation, annotationValue);
        }

        Builder addWarning(String message, XElement element) {
            return addItem(message, WARNING, element);
        }

        Builder addWarning(String message, XElement element, XAnnotation annotation) {
            return addItem(message, WARNING, element, annotation);
        }

        Builder addWarning(
                String message,
                XElement element,
                XAnnotation annotation,
                XAnnotationValue annotationValue) {
            return addItem(message, WARNING, element, annotation, annotationValue);
        }

        Builder addNote(String message) {
            return addNote(message, subject);
        }

        Builder addNote(String message, Element element) {
            return addItem(message, NOTE, element);
        }

        Builder addNote(String message, Element element, AnnotationMirror annotation) {
            return addItem(message, NOTE, element, annotation);
        }

        Builder addNote(
                String message,
                Element element,
                AnnotationMirror annotation,
                AnnotationValue annotationValue) {
            return addItem(message, NOTE, element, annotation, annotationValue);
        }

        Builder addNote(String message, XElement element) {
            return addItem(message, NOTE, element);
        }

        Builder addNote(String message, XElement element, XAnnotation annotation) {
            return addItem(message, NOTE, element, annotation);
        }

        Builder addNote(
                String message,
                XElement element,
                XAnnotation annotation,
                XAnnotationValue annotationValue) {
            return addItem(message, NOTE, element, annotation, annotationValue);
        }

        Builder addItem(String message, Diagnostic.Kind kind, Element element) {
            return addItem(message, kind, element, Optional.empty(), Optional.empty());
        }

        Builder addItem(String message, Diagnostic.Kind kind, Element element, AnnotationMirror annotation) {
            return addItem(message, kind, element, Optional.of(annotation), Optional.empty());
        }

        Builder addItem(
                String message,
                Diagnostic.Kind kind,
                Element element,
                AnnotationMirror annotation,
                AnnotationValue annotationValue) {
            return addItem(message, kind, element, Optional.of(annotation), Optional.of(annotationValue));
        }

        private Builder addItem(
                String message,
                Diagnostic.Kind kind,
                Element element,
                Optional<AnnotationMirror> annotation,
                Optional<AnnotationValue> annotationValue) {
            items.add(
                    new AutoValue_ValidationReport_Item(message, kind, element, annotation, annotationValue));
            return this;
        }

        Builder addItem(String message, Diagnostic.Kind kind, XElement element) {
            return addItem(message, kind, element, Optional.empty(), Optional.empty());
        }

        Builder addItem(String message, Diagnostic.Kind kind, XElement element, XAnnotation annotation) {
            return addItem(message, kind, element, Optional.of(annotation), Optional.empty());
        }

        Builder addItem(
                String message,
                Diagnostic.Kind kind,
                XElement element,
                XAnnotation annotation,
                XAnnotationValue annotationValue) {
            return addItem(message, kind, element, Optional.of(annotation), Optional.of(annotationValue));
        }

        private Builder addItem(
                String message,
                Diagnostic.Kind kind,
                XElement element,
                Optional<XAnnotation> annotation,
                Optional<XAnnotationValue> annotationValue) {
            items.add(
                    new AutoValue_ValidationReport_Item(
                            message,
                            kind,
                            XConverters.toJavac(element),
                            annotation.map(XConverters::toJavac),
                            annotationValue.map(XConverters::toJavac)));
            return this;
        }

        /**
         * If called, then {@link #isClean()} will return {@code false} even if there are no error items
         * in the report.
         */
        void markDirty() {
            this.markedDirty = true;
        }

        public Builder addSubreport(ValidationReport subreport) {
            subreports.add(subreport);
            return this;
        }

        @CheckReturnValue
        public ValidationReport build() {
            return new ValidationReport(getSubject(), items.build(), subreports.build(), markedDirty);
        }
    }
}
