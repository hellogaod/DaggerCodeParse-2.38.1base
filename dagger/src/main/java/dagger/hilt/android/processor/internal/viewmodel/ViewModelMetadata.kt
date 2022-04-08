/*
 * Copyright (C) 2020 The Dagger Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dagger.hilt.android.processor.internal.viewmodel

import com.google.auto.common.MoreElements
import com.squareup.javapoet.ClassName
import dagger.hilt.android.processor.internal.AndroidClassNames
import dagger.hilt.processor.internal.ClassNames
import dagger.hilt.processor.internal.ProcessorErrors
import dagger.hilt.processor.internal.Processors
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier
import javax.lang.model.element.NestingKind
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter

/**
 * Data class that represents a Hilt injected ViewModel
 */
internal class ViewModelMetadata private constructor(
  val typeElement: TypeElement
) {
  val className = ClassName.get(typeElement)

  val modulesClassName = ClassName.get(
    MoreElements.getPackage(typeElement).qualifiedName.toString(),
    "${className.simpleNames().joinToString("_")}_HiltModules"
  )

  companion object {
    internal fun create(
      processingEnv: ProcessingEnvironment,
      typeElement: TypeElement,
    ): ViewModelMetadata? {
      val types = processingEnv.typeUtils
      val elements = processingEnv.elementUtils

      //1. 使用@HiltViewModel修饰的节点必须继承androidx.lifecycle.ViewModel
      ProcessorErrors.checkState(
        types.isSubtype(
          typeElement.asType(),
          elements.getTypeElement(AndroidClassNames.VIEW_MODEL.toString()).asType()
        ),
        typeElement,
        "@HiltViewModel is only supported on types that subclass %s.",
        AndroidClassNames.VIEW_MODEL
      )

      //2. 使用@HiltViewModel修饰的节点的构造函数不允许使用@AssistedInject，如果使用了@Inject修饰，那么有且仅有一个@Inject修饰的构造函数，并且该@Inject修饰的构造函数不允许使用private修饰；
      ElementFilter.constructorsIn(typeElement.enclosedElements).filter { constructor ->
        ProcessorErrors.checkState(
          !Processors.hasAnnotation(constructor, ClassNames.ASSISTED_INJECT),
          constructor,
          "ViewModel constructor should be annotated with @Inject instead of @AssistedInject."
        )
        Processors.hasAnnotation(constructor, ClassNames.INJECT)
      }.let { injectConstructors ->
        ProcessorErrors.checkState(
          injectConstructors.size == 1,
          typeElement,
          "@HiltViewModel annotated class should contain exactly one @Inject " +
            "annotated constructor."
        )

        injectConstructors.forEach { constructor ->
          ProcessorErrors.checkState(
            !constructor.modifiers.contains(Modifier.PRIVATE),
            constructor,
            "@Inject annotated constructors must not be private."
          )
        }
      }

      //3. @HiltViewModel修饰的节点如果是内部类，那么需要使用static修饰；
      ProcessorErrors.checkState(
        typeElement.nestingKind != NestingKind.MEMBER ||
          typeElement.modifiers.contains(Modifier.STATIC),
        typeElement,
        "@HiltViewModel may only be used on inner classes if they are static."
      )

      //4. @HiltViewModel修饰的节点不允许使用@Scope修饰的注解修饰
      Processors.getScopeAnnotations(typeElement).let { scopeAnnotations ->
        ProcessorErrors.checkState(
          scopeAnnotations.isEmpty(),
          typeElement,
          "@HiltViewModel classes should not be scoped. Found: %s",
          scopeAnnotations.joinToString()
        )
      }

      return ViewModelMetadata(
        typeElement
      )
    }
  }
}
