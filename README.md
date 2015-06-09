# Android APK Rename

This plugin will rename your APK in the build process based on a configuration specified in your own build script.

View the change log [here](https://github.com/sababado/AndroidApkRename/blob/master/CHANGELOG.md).

## Include the buildscript dependency
**Note** The library is now on `jcenter()` and the artifact name is changing from `apk-rename` to `AndroidApkRename`.
```GRADLE
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.sababado.android:AndroidApkRename:1.1.2'
    }
}
```

## Apply the plugin
It is important to do this after including the buildscript dependency
```GRADLE
apply plugin: 'android-apk-rename'
```

## Configure the APK name
### Basic Configuration
This configuration gives the name: `Dork-release-master-1.0.1-1010000-20150509.apk`
```GRADLE
// other parts of the build script
apkRename {
    applicationName "Dork"
    buildTypes = ["release"] // Apply this configuration to all 'release' type builds
    include = ["workingDir", "versionCode", "versionName", "date"]
}
// more parts of the builds script.
```
### Flavor Configuration
It is also possible to provide a part to the name based on the flavor.
This configuration can give the names:  
`IronMike-lollipop-release-master-1.2.0.0.L-1200002-20150509.apk`  
`IronMike-lollipop-beta-master-1.2.0.0.L-1200002-20150509.apk`  
`IronMike-preLollipop-release-master-1.2.0.0.preL-1200002-20150509.apk`  
`IronMike-preLollipop-beta-master-1.2.0.0.preL-1200002-20150509.apk`

```GRADLE
apkRename {
    applicationName "IronMike"
    buildTypes = ["release", "beta"] // Apply this configuration to all 'release' and 'beta' type builds
    include = ["workingDir", "versionCode", "versionName", "date"]
    flavors = [1:"preL", 2:"L"]
}
```

`flavor`s map `versionCode` to `flavorName`. The `versionCode`s are the same as the `versionCode`s defined in the `productFlavors` block:

```GRADLE
android {
    // . . .
    
    flavorDimensions "api"
    productFlavors {
        prelollipop {
            flavorDimension "api"
            minSdkVersion 16
            versionCode = 1
        }
        lollipop {
            flavorDimension "api"
            minSdkVersion 21
            versionCode = 2
        }
    }
}
```

## `apkRename` Extension
* `applicationName` The name of the application
* `buildTypes` The build types that this naming configuration should be applied to.
* `variants` The variants that this naming configuration should be applied to.
* `include` Include additional parts to the name. This part of the name is built on the order of the values given:
    * `workingDir` The Git working directory. This is either taken automatically or a custom value can be used by adding
a build property for `gitBranch`. The automatic retrieval starts at the project's root directory and searches back to the root folder.
For example if the root project directory is `/Users/rjszabo/Git/date-night/DateNight` the search will start there, looking for `.git`,
and go backwards, stopping after looking in `/Users`.
    * `versionCode` The version code of the build variant
    * `versionName` The version name of the build variant. Flavor names will be appended here.
    * `date` The current date in the format `yyyyMMdd`. A custom value can be provided by using a build property for
`timestamp`.
* `flavors` A mapping of flavor version codes and their names. The version codes are the same ones specified in the
 `productFlavors` block.
* `useFlavorNameAsAppName` Default is false. Set to true to use the name specified in the `flavors` mapping in place of the `applicationName`.

Either a `buildType` or a `variant` must be provided. If both are provided then the variant will only be counted if
it falls under one of the provided build types.

# Logging
If debug logs are needed, use the property `showRenameLogs`. For example append `-DshowRenameLogs` to the build execution command.

# Backlog
* provide the option to remove the full flavor name
* Better flavor support (ex: different app name per flavor)
* Provide a custom name to replace the entire name
