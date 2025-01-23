package command

import utils.*

class CommandBuilder {
    private var bundleToolPath: String = ""
    private var aabFilePath: String = ""
    private var isOverwrite: Boolean = false
    private var aapt2Path: String = ""
    private var isUniversalMode: Boolean = true
    private var signingMode: Int = 1
    private var keyStorePath: String = ""
    private var keyStorePassword: String = ""
    private var keyAlias: String = ""
    private var keyPassword: String = ""
    private var adbVerifyCommandExecute = Pair(false, "")
    private var isDeviceIdEnabled: Boolean = false
    private var adbSerialId: String = ""

    fun bundleToolPath(path: String) = apply { this.bundleToolPath = path }
    fun aabFilePath(path: String) = apply { this.aabFilePath = path }
    fun isOverwrite(overwrite: Boolean) = apply { this.isOverwrite = overwrite }
    fun aapt2Path(path: String) = apply { this.aapt2Path = path }
    fun isUniversalMode(universalMode: Boolean) = apply { this.isUniversalMode = universalMode }
    fun signingMode(mode: Int) = apply { this.signingMode = mode }
    fun keyStorePath(path: String) = apply { this.keyStorePath = path }
    fun keyStorePassword(password: String) = apply { this.keyStorePassword = password }
    fun keyAlias(alias: String) = apply { this.keyAlias = alias }
    fun keyPassword(password: String) = apply { this.keyPassword = password }

    fun verifyAdbPath(value: Boolean, path: String) = apply { this.adbVerifyCommandExecute = Pair(value, path) }

    fun isDeviceSerialIdEnabled(value: Boolean) = apply { this.isDeviceIdEnabled = value }

    fun adbSerialId(value: String) = apply { this.adbSerialId = value }

    fun getAdbVerifyCommand(): String {
        val (forVerify, path) = adbVerifyCommandExecute
        if (forVerify) {
            return if (Utils.isWindowsOS()) "\"${path}\" version" else "$path version"
        }
        return ""
    }

    fun getAdbFetchCommand(adbPath: String): String {
        return if (Utils.isWindowsOS()) "\"${adbPath}\" devices" else "$adbPath devices"
    }

    fun validateAndGetCommand(): Pair<String, Boolean> {
        if (bundleToolPath.isEmpty()) {
            return Pair("bundleToolPath", false)
        }
        if (aabFilePath.isNotBlank()) {
            return Pair("aabFilePath", false)
        }
        if (aapt2Path.isNotBlank()) {
            return Pair("aapt2Path", false)
        }
        if (signingMode == SigningMode.RELEASE && (keyStorePath.isEmpty() || keyStorePassword.isEmpty() || keyAlias.isEmpty() || keyPassword.isEmpty())) {
            return Pair("Check Keystore Info!", false)
        }
        if (isDeviceIdEnabled && adbSerialId.isEmpty()) {
            return Pair("Invalid Serial ID", false)
        }
        return Pair(getCommand(), true)
    }

    private fun getCommand(): String {
        val commandBuilder = StringBuilder()
        if (Utils.isWindowsOS()) {
            commandBuilder.append("java -jar \"$bundleToolPath\" build-apks ")
            if (aapt2Path.isNotBlank()) {
                commandBuilder.append("--aapt2=\"$aapt2Path\" ")
            }
            commandBuilder.append(
                "--bundle=\"${aabFilePath}\" --output=\"${aabFilePath.parent()}" +
                        "${aabFilePath.fileName().split(".")[0]}.apks\" "
            )
        } else {
            commandBuilder.append("java -jar $bundleToolPath build-apks ")
            if (aapt2Path.isNotBlank()) {
                commandBuilder.append("--aapt2=$aapt2Path ")
            }
            commandBuilder.append(
                "--bundle=${aabFilePath} --output=${aabFilePath.parent()}${
                    aabFilePath.fileName().split(".")[0]
                }.apks "
            )
        }

        if (isUniversalMode) {
            commandBuilder.append("--mode=universal ")
        }
        if (isOverwrite) {
            commandBuilder.append("--overwrite ")
        }

        if (signingMode == SigningMode.RELEASE) {
            commandBuilder.append("--ks=$keyStorePath --ks-pass=pass:$keyStorePassword --ks-key-alias=$keyAlias --key-pass=pass:$keyPassword ")
        }

        if (isDeviceIdEnabled) {
            commandBuilder.append("--device-id=$adbSerialId ")
        }

        Log.i("COMMAND_BUILDER -> $commandBuilder")
        return commandBuilder.toString()
    }
}