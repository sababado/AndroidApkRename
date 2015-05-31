/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 */

package com.sababado.android.apkrename

import org.gradle.api.Plugin
import org.gradle.api.Project

class ApkRenamePlugin implements Plugin<Project> {

    void apply(Project project) {
        project.extensions.create("apkRename", ApkRenameExtension)
        ApkRenameExtension apkRenameConfig = project.apkRename
        project.afterEvaluate { validateExtension(apkRenameConfig) }

        // make per-variant version code
        project.android.applicationVariants.all { variant ->
            if (shouldVariantBeRenamed(variant, apkRenameConfig)) {
                // set the composite code
                def appName = getAppName(variant, apkRenameConfig)

                // set the output file name
                def nameExtras = buildApkNameExtras(project, variant, apkRenameConfig)
                variant.outputs.each { output ->
                    if (output.zipAlign) {
                        def file = output.outputFile
                        def fileName = file.name.replace("app", appName).replace(".apk", nameExtras)
                        output.outputFile = new File(file.parent, fileName)
                    }

                    def file = output.packageApplication.outputFile
                    def fileName = file.name.replace("app", appName).replace(".apk", nameExtras)
                    output.packageApplication.outputFile = new File(file.parent, fileName)
                }
            }
        }
    }

    private static boolean shouldVariantBeRenamed(def variant, ApkRenameExtension apkRenameConfig) {
        println "${variant.name} / ${variant.buildType.name}"
        // check if the variant is in the build type
        // If there are build types then the variant must be in the build type.
        if (!apkRenameConfig.buildTypes || variant.buildType.name in apkRenameConfig.buildTypes) {
            // If the variant is in the build type it must be in the given variants (if there are given variants)
            return variantIsInVariants(variant, apkRenameConfig)
        } else if (apkRenameConfig.buildTypes) {
            // Not in the build type
            return false;
        }
        // The variant must be in the given variants (if there are given variants)
        return variantIsInVariants(variant, apkRenameConfig)
    }

    private static boolean variantIsInVariants(def variant, ApkRenameExtension apkRenameConfig) {
        return !apkRenameConfig.variants || variant.name in apkRenameConfig.variants;
    }

    /**
     * Get a timestamp in the format yyyyMMdd. Either use one provided by a "timestamp" property
     * or use the current timestamp.
     *
     * @return A timestamp in the format of yyyyMMdd.
     */
    static def getDate(Project project) {
        if (!project.hasProperty("timestamp")) {
            def date = new Date()
            def formattedDate = date.format('yyyyMMdd')
            println "Using gradle date"
            return formattedDate
        }
        println "Using environment date"
        return project.getProperties().get('timestamp')
    }

    /**
     * Get the name of the working branch of the project
     *
     * @return Name of the working branch
     */
    static def getWorkingBranch(Project project) {
        if (!project.hasProperty("gitbranch")) {
            def rootDir = project.rootDir
            // Triple double-quotes for the breaklines
            def workingBranch = """git --git-dir=${rootDir}/../.git
                               --work-tree=${rootDir}/..
                               rev-parse --abbrev-ref HEAD""".execute().text.trim()
            println "Working gradle branch: " + workingBranch
            return workingBranch
        }
        def workingBranch = project.getProperties().get('gitbranch')
        println "Working environment branch: " + workingBranch
        return workingBranch
    }

    /**
     * Validate the properties in the extension to make sure all required fields are there.
     * @param apkRenameConfig Extension to validate.
     */
    private static void validateExtension(ApkRenameExtension apkRenameConfig) {
        if (!apkRenameConfig.applicationName && !apkRenameConfig.useFlavorNameAsAppName) {
            throw new RuntimeException("applicationName is required in apkRename.")
        } else if (apkRenameConfig.useFlavorNameAsAppName) {
            if (apkRenameConfig.flavors == null || apkRenameConfig.flavors.size() == 0) {
                throw new RuntimeException("flavors are required in apkRename when useFlavorNameAsAppName is true.")
            }
        }
        boolean emptyBuildTypes = false;
        if (!apkRenameConfig.buildTypes || apkRenameConfig.buildTypes.length == 0) {
            emptyBuildTypes = true;
        }
        if (!apkRenameConfig.variants || apkRenameConfig.variants.length == 0) {
            if (emptyBuildTypes) {
                throw new RuntimeException("At least one variant or buildType is required in apkRename.")
            }
        }
    }

    /**
     * Get the application name based on the variant
     * @param variant Variant to get the app name for
     * @param apkRenameExtension Config that tells us what name to use.
     * @return The application name.
     */
    private static String getAppName(def variant, ApkRenameExtension apkRenameExtension) {
        if (apkRenameExtension.useFlavorNameAsAppName) {
            return getFlavorPart(variant, apkRenameExtension.flavors).name
        }
        return apkRenameExtension.applicationName
    }

    /**
     * Build the APK name that goes after the app and variant name.
     * @param project The project.
     * @param variant The variant the name is changing for.
     * @param apkRenameConfig Config that tells us how to build the name.
     * @return A name with at least ".apk"
     */
    private static String buildApkNameExtras(Project project, def variant, ApkRenameExtension apkRenameConfig) {
        def vName = project.android.defaultConfig.versionName
        def vCode = project.android.defaultConfig.versionCode
        FlavorPart flavorPart = getFlavorPart(variant, apkRenameConfig.flavors);

        if (!apkRenameConfig.useFlavorNameAsAppName) {
            String flavorPartName = flavorPart.name
            if (!flavorPartName?.isEmpty()) {
                flavorPartName = ".${flavorPartName}"
            }
            if (vName) {
                vName = project.android.defaultConfig.versionName + "${flavorPartName}"
            } else {
                vName = "${flavorPartName}"
            }
        }

        if (vCode) {
            vCode = project.android.defaultConfig.versionCode + flavorPart.apiVersion
        } else {
            vCode = flavorPart.apiVersion
        }

        variant.mergedFlavor.versionName = vName
        variant.mergedFlavor.versionCode = vCode

        def include = apkRenameConfig.include

        // set the output file name
        def nameExtras = ""
        nameExtras += getNamePart(include, ApkNamePart.workingDir, "-${getWorkingBranch(project)}")
        nameExtras += getNamePart(include, ApkNamePart.versionName, "-${vName}")
        nameExtras += getNamePart(include, ApkNamePart.versionCode, "-${vCode}")
        nameExtras += getNamePart(include, ApkNamePart.date, "-${getDate(project)}")
        nameExtras += ".apk"

        return nameExtras
    }

    private static FlavorPart getFlavorPart(def variant, Map<Integer, String> flavors) {
        if (flavors) {
            int apiVersion = variant.productFlavors.get(0).versionCode
            if (flavors.containsKey(apiVersion)) {
                return new FlavorPart(flavors.get(apiVersion), apiVersion);
            }
        }
        return new FlavorPart("", 0);
    }

    private static String getNamePart(ApkNamePart[] include, ApkNamePart namePart, String extra) {
        if (include && namePart in include) {
            return extra
        }
        return ""
    }

    static class FlavorPart {
        String name
        int apiVersion

        FlavorPart(String name, int apiVersion) {
            this.name = name
            this.apiVersion = apiVersion
        }
    }
}