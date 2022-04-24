/*
 * Copyright (C) 2021 The Dagger Authors.
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

package dagger.hilt.processor.internal.root.ir

import kotlin.jvm.Throws

// Validates roots being processed.
object AggregatedRootIrValidator {
    @JvmStatic
    @Throws(InvalidRootsException::class)
    fun rootsToProcess(
        isCrossCompilationRootValidationDisabled: Boolean,
        //dagger.hilt.internal.processedrootsentinel.codegen包下使用@ProcessedRootSentinel注解修饰的节点生成的对象
        processedRoots: Set<ProcessedRootSentinelIr>,
        //dagger.hilt.internal.aggregatedroot.codegen包下使用@AggregatedRoot修饰的节点生成的对象
        aggregatedRoots: Set<AggregatedRootIr>
    ): Set<AggregatedRootIr> {

        //@ProcessedRootSentinel#roots
        val processedRootNames = processedRoots.flatMap { it.roots }.toSet()

        //@AggregatedRoot#root的值筛选出不存在与@ProcessedRootSentinel#roots中
        val rootsToProcess = aggregatedRoots
            .filterNot { processedRootNames.contains(it.root) }
            .sortedBy { it.root.toString() }

        //再筛选出@HiltAndroidTest和@InternalTestRoot
        val testRootsToProcess = rootsToProcess.filter { it.isTestRoot }

        //@AggregatedRoot#root的值（筛选出不存在与@ProcessedRootSentinel#roots中） 如果@AggregatedRoot#rootAnnotation中存在@HiltAndroidTest或@InternalTestRoot，将其去掉
        val appRootsToProcess = rootsToProcess - testRootsToProcess

        //相当于class类中的函数表达式：该用法比较有借鉴意义。
        fun Collection<AggregatedRootIr>.rootsToString() = map { it.root }.joinToString()

        // 1. @HiltAndroidApp在一个项目中只允许出现一次；
        if (appRootsToProcess.size > 1) {
            throw InvalidRootsException(
                "Cannot process multiple app roots in the same compilation unit: " +
                        appRootsToProcess.rootsToString()
            )
        }

        //2.同一个项目中不允许同时出现@HiltAndroidTest或@InternalTestRoot 和 @HiltAndroidApp；
        // - 可以理解为@HiltAndroidApp应用于项目，@HiltAndroidTest或@InternalTestRoot应用于测试环境；
        if (testRootsToProcess.isNotEmpty() && appRootsToProcess.isNotEmpty()) {
            throw InvalidRootsException(
                """
        Cannot process test roots and app roots in the same compilation unit:
          App root in this compilation unit: ${appRootsToProcess.rootsToString()}
          Test roots in this compilation unit: ${testRootsToProcess.rootsToString()}
        """.trimIndent()
            )
        }

        // Perform validation across roots previous compilation units.
        if (!isCrossCompilationRootValidationDisabled) {

            //@AggregatedRoot#rootAnnotation中的注解如果是@HiltAndroidTest或@InternalTestRoot ,并且当前注解已经被处理过了。
            val alreadyProcessedTestRoots = aggregatedRoots.filter {
                it.isTestRoot && processedRootNames.contains(it.root)
            }

            // TODO(b/185742783): Add an explanation or link to docs to explain why we're forbidding this.
            //3. 如果@AggregatedRoot#rootAnnotation中的注解是@HiltAndroidTest或@InternalTestRoot && @AggregatedRoot#root包含在@ProcessedRootSentinel#roots中（已经被处理过了） ，那么不允许再次处理该注解；
            if (alreadyProcessedTestRoots.isNotEmpty() && rootsToProcess.isNotEmpty()) {
                throw InvalidRootsException(
                    """
          Cannot process new roots when there are test roots from a previous compilation unit:
            Test roots from previous compilation unit: ${alreadyProcessedTestRoots.rootsToString()}
            All roots from this compilation unit: ${rootsToProcess.rootsToString()}
          """.trimIndent()
                )
            }

            //@AggregatedRoot#rootAnnotation中的注解如果是@HiltAndroidApp && @HiltAndroidApp包含在@ProcessedRootSentinel#roots中(已经被处理过了)
            val alreadyProcessedAppRoots = aggregatedRoots.filter {
                !it.isTestRoot && processedRootNames.contains(it.root)
            }

            //4. @AggregatedRoot#rootAnnotation中的注解如果是@HiltAndroidApp && @HiltAndroidApp包含在@ProcessedRootSentinel#roots中(已经被处理过了)，那么不允许再次在当前项目中处理 @HiltAndroidApp注解；
            if (alreadyProcessedAppRoots.isNotEmpty() && appRootsToProcess.isNotEmpty()) {
                throw InvalidRootsException(
                    """
          Cannot process new app roots when there are app roots from a previous compilation unit:
            App roots in previous compilation unit: ${alreadyProcessedAppRoots.rootsToString()}
            App roots in this compilation unit: ${appRootsToProcess.rootsToString()}
          """.trimIndent()
                )
            }
        }

        return rootsToProcess.toSet()
    }
}

// An exception thrown when root validation fails.
class InvalidRootsException(msg: String) : Exception(msg)
