# Android APK Rename Changelog

# 1.1.2
* Order of extra name parts depend on the order they are defined in the `include` array.
* Fixing `workingDir` issue.

# 1.1.1
* Added more complete method for finding the git working directory.
* Use `showRenameLogs` to view debug logs for the plugin.

# 1.1.0
Added `useFlavorNameAsAppName` flag to the `apkRename` extension. The default value is false.
Set to true to use the name specified in the `flavors` mapping in place of the `applicationName`.

# 1.0.0
Initial Release