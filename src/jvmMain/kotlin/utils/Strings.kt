package utils

object Strings {
    const val APP_NAME = "Android Bundletool UI"
    const val URL = "URL"
    const val CHOOSE_FILE = "Choose a file"
    const val SELECT_BUNDLETOOL_JAR = "Select Bundletool Jar"
    const val SAVE_JAR_PATH = "Save Jar Path"
    const val SAVE_JAR_PATH_INFO = "Saves Path of Bundle Tool Jar for Future."
    const val DOWNLOAD_BUNDLETOOL = "Download Bundletool from here"
    const val SELECT_AAB_FILE = "Select Aab File"
    const val OPTIONS_FOR_BUILD_APKS = "Options for the bundletool build-apks command"
    const val OVERWRITE = "Overwrite"
    const val OVERWRITE_INFO = "Overwrites any existing output file with the path you specify using the --output option. If you don't include this flag and the output file already exists, you get a build error."
    const val AAPT2_PATH = "Aapt2 Path"
    const val AAPT2_PATH_INFO = "Specifies a custom path to AAPT2. By default, bundletool includes its own version of AAPT2."
    const val SELECT_AAPT2_FILE = "Select Aapt2 File"
    const val MODE_UNIVERSAL = "Mode Universal"
    const val MODE_UNIVERSAL_INFO = "Sets the mode to universal. Use this option if you want bundletool to build a single APK that includes all of your app's code and resources, so that the APK is compatible with all device configurations your app supports." +
            "\nNote: bundletool includes only feature modules that specify <dist:fusing dist:include=\"true\"/> in their manifest in a universal APK. To learn more, read about the feature module manifest.\n" +
            "Keep in mind, these APKs are larger than those optimized for a particular device configuration. However, they're easier to share with internal testers who, for example, want to test your app on multiple device configurations."
    const val SIGNING_MODE = "Signing Mode"
    const val DEBUG = "Debug"
    const val RELEASE = "Release"
    const val KEYSTORE_PATH = "Select Keystore Path"
    const val KEYSTORE_PASSWORD = "Keystore Password"
    const val KEY_ALIAS = "Key Alias"
    const val KEY_PASSWORD = "Key Password"
    const val EXECUTE = "Execute"
    const val FILE_OPTIONS = "File Options"
    const val DEVICE_OPTIONS = "Device Options"
    const val AUTO_UNZIP = "Automatically Unzip and Delete Apks File"
    const val CLEAR_LOGS = "Clear Logs"
    const val LOGS_VIEW = "Logs View"
    const val SETUP_ADB = "Set up Adb"
    const val ABD_SETUP_DONE = "Adb Connected"
    const val SETUP_ADB_INFO = "Setup ADB path to create builds based on connected device"
    const val DEVICE_ID = "Device Id"
    const val DEVICE_ID_INFO = "Device Id based on the Serial Number. Use with Mode Universal to get one apk."
    const val VERIFYING_ADB_PATH = "Verifying ADB Path.."
    const val SERIAL_ID = "Serial Id"
    const val FETCH_DEVICES = "Fetch Devices"
    const val FETCH_DEVICES_INFO = "Fetch Connected Devices Info in LogView"
}