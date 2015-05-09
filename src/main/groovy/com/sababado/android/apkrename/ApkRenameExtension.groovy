package com.sababado.android.apkrename

/**
 * Extension used to configure the APK name.
 * Created by robert on 5/9/15.
 */
class ApkRenameExtension {
    String[] buildTypes
    String[] variants
    String applicationName
    ApkNamePart[] include
    Map<Integer, String> flavors
}
