/*
 * Copyright (C) 2018 The Dagger Authors.
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

package dagger.android;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;

import dagger.MapKey;
import dagger.internal.Beta;

import static java.lang.annotation.ElementType.METHOD;

/**
 * {@link MapKey} annotation to key {@link AndroidInjector.Factory} bindings. The {@linkplain
 * #value() value} of the annotation is the canonical name of the class that will be passed to
 * {@link AndroidInjector#inject(Object)}.
 *
 * <p>All key strings will be obfuscated by ProGuard/R8 if the named class is obfuscated.
 */
@Beta
@MapKey
@Target(METHOD)
@Documented
public @interface AndroidInjectionKey {
  /** The fully qualified class name of the type to be injected. */
  String value();
}
