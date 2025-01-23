package data

import utils.SigningMode


data class SaveBean(
    // bundleTool工具路径
    val bundleToolPath: String = "",
    // aab路径
    val aabPath: String = "",
    // --overWrite
    val isOverwrite: Boolean = false,
    // aapt2Path
    val aapt2Path: String = "",
    // 是否是通用模式
    val isUniversalMode: Boolean = false,
    // 调试模式
    val signingMode: Int = SigningMode.DEBUG,
    // keyStore路径
    val keyStorePath: String = "",
    // keyStore密码
    val keyStorePassword: String = "",
    val keyAlias: String = "",
    val keyPassword: String = "",
    // 自动解压
    val isAutoUnzip: Boolean = true,
    val adbPath: String = ""
)