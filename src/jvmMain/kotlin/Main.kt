import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import command.CommandBuilder
import command.CommandExecutor
import data.SaveBean
import local.FileStorageHelper
import ui.Styles
import ui.components.ButtonWithToolTip
import ui.components.CheckboxWithText
import ui.components.ChooseFileTextField
import ui.components.CustomTextField
import ui.components.LoadingDialog
import utils.*
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.net.URI

@Composable
@Preview
fun App(
    getConfig: () -> SaveBean,
    saveConfig: (SaveBean) -> Unit
) {
    val density = LocalDensity.current // to calculate the intrinsic size of vector images (SVG, XML)

    // 协程
    val coroutineScope = rememberCoroutineScope()

    // 日志
    var logs by remember { mutableStateOf("") }

    // UI状态
    var processState by remember { mutableStateOf(ProcessStep.NONE) }
    var isDeviceIdEnabled by remember { mutableStateOf(false) }
    var deviceSerialId by remember { mutableStateOf("") }

    // 弹窗类型
    var fileDialogType by remember { mutableStateOf(0) }

    // 文件存储
    val configSaver = remember { Saver<SaveBean, SaveBean>({ value -> value.apply(saveConfig) }, { _ -> getConfig() }) }
    var conf by rememberSaveable(stateSaver = configSaver) { mutableStateOf(getConfig()) }

    if (processState == ProcessStep.OPEN_DIALOG) {
        FileDialog { fileName, directory ->
            if (fileName.isNullOrEmpty() || directory.isNullOrEmpty()) {
                processState = ProcessStep.NONE
                return@FileDialog
            }
            when (fileDialogType) {
                FileDialogType.BUNDLE_TOOL -> conf = conf.copy(bundleToolPath = "$directory$fileName")
                FileDialogType.AAPT2 -> conf = conf.copy(aapt2Path = "$directory$fileName")
                FileDialogType.KEY_STORE_PATH -> conf = conf.copy(keyStorePath = "$directory$fileName")
                FileDialogType.ADB_PATH -> {
                    conf = conf.copy(adbPath = "$directory$fileName")
                    // Show Loading Here
                    processState = ProcessStep.CHECK
                    CommandExecutor().executeCommand(
                        CommandBuilder()
                            .verifyAdbPath(true, conf.adbPath)
                            .getAdbVerifyCommand(), coroutineScope,
                        onSuccess = {
                            logs += it
                            Log.i("Saving Path in DB ${conf.adbPath}")
                            Thread.sleep(1000L)
                            // 降级
                            processState = ProcessStep.NONE
                        },
                        onFailure = {
                            logs += it
                            conf = conf.copy(adbPath = "")
                            processState = ProcessStep.NONE
                        }
                    )
                }

                FileDialogType.AAB -> conf = conf.copy(aabPath = "$directory$fileName")
            }
            processState = ProcessStep.NONE
        }
    }

    if (processState == ProcessStep.CHECK) {
        LoadingDialog(Strings.VERIFYING_ADB_PATH)
    }

    if (processState == ProcessStep.EXECUTING) {
        // Get Command to Execute
        val (cmd, isValid) = CommandBuilder()
            .bundleToolPath(conf.bundleToolPath)
            .aabFilePath(conf.aabPath)
            .isOverwrite(conf.isOverwrite)
            .isUniversalMode(conf.isUniversalMode)
            .aapt2Path(conf.aapt2Path)
            .signingMode(conf.signingMode)
            .keyStorePath(conf.keyStorePath)
            .keyStorePassword(conf.keyStorePassword)
            .keyAlias(conf.keyAlias)
            .keyPassword(conf.keyPassword)
            .validateAndGetCommand()
        if (isValid) {
            Log.i("Command $cmd")
            // logs += "Executing Command : \n$cmd\n"
            CommandExecutor()
                .executeCommand(
                    cmd,
                    coroutineScope,
                    onSuccess = {
                        logs += "$it\n"
                        // Do further file operation after new apks is generated
                        // From Auto Zip you can control further file operations.
                        if (conf.isAutoUnzip) {
                            FileHelper.performFileOperations(conf.aabPath) { status, message ->
                                processState = ProcessStep.NONE
                                Log.i("STATUS - $status\nMESSAGE - $message")
                                logs += "\nFiles Operations Starting...\n$message\n"
                            }
                        } else {
                            logs += "\nFile will be saved at ${conf.aabPath.parent().removeSuffix("\\")}.\n"
                            processState = ProcessStep.NONE
                        }
                    },
                    onFailure = {
                        processState = ProcessStep.NONE
                        logs += "Failed -> ${it.printStackTrace()}"
                    }
                )
        } else {
            Log.i("Error $cmd")
            logs += "\nError -> $cmd"
            processState = ProcessStep.NONE
        }
    }

    val rememberScrollableState = rememberScrollState()

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollableState, true),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.padding(12.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = Strings.APP_NAME,
                    style = Styles.TextStyleBold(28.sp),
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
                )
                ButtonWithToolTip(
                    if (conf.adbPath.isNotBlank()) Strings.ABD_SETUP_DONE else Strings.SETUP_ADB,
                    onClick = {
                        fileDialogType = FileDialogType.ADB_PATH
                        processState = ProcessStep.OPEN_DIALOG
                    },
                    Strings.SETUP_ADB_INFO,
                    icon = if (conf.adbPath.isNotBlank()) "done" else "info"
                )
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.wrapContentSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bundle tool select flow
                ChooseFileTextField(
                    conf.bundleToolPath,
                    Strings.SELECT_BUNDLETOOL_JAR,
                    onSelect = {
                        fileDialogType = FileDialogType.BUNDLE_TOOL
                        processState = ProcessStep.OPEN_DIALOG
                    }
                )
//                CheckboxWithText(
//                    Strings.SAVE_JAR_PATH,
//                    saveJarPath,
//                    onCheckedChange = {
//                        saveJarPath = it
//                    },
//                    Strings.SAVE_JAR_PATH_INFO
//                )
            }
            val downloadInfo = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)) {
                    append(Strings.DOWNLOAD_BUNDLETOOL)
                }
                addStringAnnotation(
                    tag = Strings.URL,
                    annotation = Constant.BUNDLE_DOWNLOAD_LINK,
                    start = 0,
                    end = length
                )
            }
            ClickableText(
                text = downloadInfo,
                style = Styles.TextStyleMedium(14.sp),
                modifier = Modifier.padding(start = 16.dp),
                onClick = { offset ->
                    val annotations = downloadInfo.getStringAnnotations(Strings.URL, offset, offset)
                    if (annotations.isNotEmpty()) {
                        val uri = URI(annotations.first().item)
                        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                            Desktop.getDesktop().browse(uri)
                        } else {
                            // Desktop not supported, handle as necessary
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.padding(8.dp))
            ChooseFileTextField(
                conf.aabPath,
                Strings.SELECT_AAB_FILE,
                onSelect = {
                    fileDialogType = FileDialogType.AAB
                    processState = ProcessStep.OPEN_DIALOG
                }
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                text = Strings.OPTIONS_FOR_BUILD_APKS,
                style = Styles.TextStyleBold(20.sp),
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CheckboxWithText(
                    Strings.OVERWRITE,
                    conf.isOverwrite,
                    onCheckedChange = { conf = conf.copy(isOverwrite = it) },
                    Strings.OVERWRITE_INFO
                )
                CheckboxWithText(
                    Strings.MODE_UNIVERSAL,
                    conf.isUniversalMode,
                    onCheckedChange = { conf = conf.copy(isUniversalMode = it) },
                    Strings.MODE_UNIVERSAL_INFO
                )
//                CheckboxWithText(
//                    Strings.AAPT2_PATH,
//                    isAapt2PathEnabled,
//                    onCheckedChange = {
//                        isAapt2PathEnabled = it
//                    },
//                    Strings.AAPT2_PATH_INFO
//                )
            }
//            if (isAapt2PathEnabled) {
            ChooseFileTextField(
                conf.aapt2Path,
                Strings.SELECT_AAPT2_FILE,
                onSelect = {
                    fileDialogType = FileDialogType.AAPT2
                    processState = ProcessStep.OPEN_DIALOG
                }
            )
//            }
            Text(
                text = Strings.SIGNING_MODE,
                style = Styles.TextStyleBold(16.sp),
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CheckboxWithText(
                    Strings.DEBUG,
                    conf.signingMode == SigningMode.DEBUG,
                    onCheckedChange = {
                        val signingMode = if (it) SigningMode.DEBUG
                        else SigningMode.RELEASE
                        conf = conf.copy(signingMode = signingMode)
                    }
                )
                CheckboxWithText(
                    Strings.RELEASE,
                    conf.signingMode == SigningMode.RELEASE,
                    onCheckedChange = {
                        val signingMode = if (it) SigningMode.RELEASE
                        else SigningMode.DEBUG
                        conf = conf.copy(signingMode = signingMode)
                    }
                )
            }
            if (conf.signingMode == SigningMode.RELEASE) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ChooseFileTextField(
                        conf.keyStorePath,
                        Strings.KEYSTORE_PATH,
                        onSelect = {
                            fileDialogType = FileDialogType.KEY_STORE_PATH
                            processState = ProcessStep.OPEN_DIALOG
                        }
                    )
                    CustomTextField(
                        conf.keyStorePassword,
                        Strings.KEYSTORE_PASSWORD,
                        onValueChange = {
                            conf = conf.copy(keyStorePassword = it)
                        }
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CustomTextField(
                        conf.keyAlias,
                        Strings.KEY_ALIAS,
                        forPassword = false,
                        onValueChange = {
                            conf = conf.copy(keyAlias = it)
                        }
                    )
                    CustomTextField(
                        conf.keyPassword,
                        Strings.KEY_PASSWORD,
                        onValueChange = {
                            conf = conf.copy(keyPassword = it)
                        }
                    )
                }
            }
            Text(
                text = Strings.FILE_OPTIONS,
                style = Styles.TextStyleBold(16.sp),
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
            )
            CheckboxWithText(
                Strings.AUTO_UNZIP,
                conf.isAutoUnzip,
                onCheckedChange = {
                    conf = conf.copy(isAutoUnzip = it)
                },
                Strings.AUTO_UNZIP
            )
            Spacer(modifier = Modifier.padding(8.dp))
            if (conf.adbPath.isNotBlank()) {
                Text(
                    text = Strings.DEVICE_OPTIONS,
                    style = Styles.TextStyleBold(16.sp),
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CheckboxWithText(
                        Strings.DEVICE_ID,
                        isDeviceIdEnabled,
                        onCheckedChange = {
                            isDeviceIdEnabled = it
                        },
                        Strings.DEVICE_ID_INFO
                    )
                    if (isDeviceIdEnabled) {
                        CustomTextField(
                            deviceSerialId,
                            Strings.SERIAL_ID,
                            forPassword = false,
                            onValueChange = {
                                deviceSerialId = it
                            }
                        )
                        ButtonWithToolTip(
                            Strings.FETCH_DEVICES,
                            onClick = {
                                CommandExecutor()
                                    .executeCommand(
                                        CommandBuilder()
                                            .getAdbFetchCommand(conf.adbPath),
                                        coroutineScope,
                                        onSuccess = {
                                            logs += it
                                        },
                                        onFailure = {
                                            logs += it.printStackTrace()
                                        }
                                    )
                            },
                            Strings.FETCH_DEVICES_INFO,
                            icon = "device_fetch"
                        )
                    }
                }
                Spacer(modifier = Modifier.padding(8.dp))
            }
            if (processState == ProcessStep.EXECUTING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(size = 40.dp)
                        .padding(start = 16.dp, top = 0.dp, end = 0.dp, bottom = 0.dp),
                    strokeWidth = 4.dp
                )
            } else {
                Button(
                    onClick = { processState = ProcessStep.EXECUTING },
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp).wrapContentWidth()
                ) {
                    Text(
                        text = Strings.EXECUTE,
                        style = Styles.TextStyleMedium(16.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = Strings.LOGS_VIEW,
                    style = Styles.TextStyleBold(20.sp),
                    modifier = Modifier.padding(start = 0.dp, end = 0.dp, bottom = 8.dp)
                )
                Button(
                    modifier = Modifier.padding(end = 0.dp, bottom = 8.dp),
                    onClick = {
                        logs = ""
                    }
                ) {
                    Text(
                        text = Strings.CLEAR_LOGS,
                        style = Styles.TextStyleBold(13.sp)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Icon(
                        painter = useResource("clear.svg") { loadSvgPainter(it, density) },
                        contentDescription = "Clear",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                }
            }
            TextField(
                modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                value = logs,
                textStyle = Styles.TextStyleMedium(16.sp),
                onValueChange = {}
            )
        }
    }

    LaunchedEffect(conf) {
        saveConfig(conf)
    }

}

@Composable
private fun FileDialog(
    parent: Frame? = null,
    onCloseRequest: (fileName: String?, directory: String?) -> Unit
) = AwtWindow(
    create = {
        object : FileDialog(parent, Strings.CHOOSE_FILE, LOAD) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    onCloseRequest(file, directory)
                }
            }
        }
    },
    dispose = FileDialog::dispose
)

fun main() = application {
    val fileStorageHelper = FileStorageHelper()
    val getConfigFun = remember { { fileStorageHelper.read(DBConstants.SAVE_CONFIG) as? SaveBean ?: SaveBean() } }
    val saveConfigFun = remember { { saveBean: SaveBean -> fileStorageHelper.save(DBConstants.SAVE_CONFIG, saveBean) } }

    Log.showLogs = true

    Window(
        icon = painterResource("launcher.png"),
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(),
        title = Strings.APP_NAME,
    ) {
        App(getConfigFun, saveConfigFun)
    }
}