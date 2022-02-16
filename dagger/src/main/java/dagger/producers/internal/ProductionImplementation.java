/*
 * Copyright (C) 2016 The Dagger Authors.
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

package dagger.producers.internal;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Qualifier;

import dagger.internal.Beta;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Qualifies a type that will be used as an internal implementation detail in the framework.
 *
 * <p>This is only intended to be used by the framework. It is the internal counterpart to
 * {@link dagger.producers.Production}.
 */
@Documented
@Retention(RUNTIME)
@Qualifier
@Beta
public @interface ProductionImplementation {}
