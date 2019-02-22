package com.earth.OsToolkit.base

import android.content.Context
import android.os.Build
import com.topjohnwu.superuser.Shell
import java.io.*
import java.util.regex.Pattern
import android.widget.Toast


/*
 * OsToolkit - Kotlin
 *
 * Date : 31/12/2018
 *
 * By   : 1552980358
 *
 */

class BaseKotlinOperation {
    companion object {
        fun checkRoot(): Boolean {
            try {
                val process: Process = Runtime.getRuntime().exec("su")
                val dataOutPutStream = DataOutputStream(process.outputStream)
                dataOutPutStream.writeBytes("exit\n")
                dataOutPutStream.flush()
                dataOutPutStream.close()
                val i = process.waitFor()
                if (i == 1)
                    return false
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }

        fun getPackageVersion(context: Context?): String {
            try {
                return context?.packageManager!!.getPackageInfo(BaseIndex.PackageName, 0).versionName
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return ""
        }

        fun getAvailableCore(): Int {
            try {
                val dir = File("/sys/devices/system/cpu/")
                val file: Array<File> = dir.listFiles(FileFilter {
                    Pattern.matches("cpu[0-9]", it.name)
                })
                return file.size
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return 0
        }

        fun setPermission(filePath: String): Boolean {
            val file = File(filePath)

            file.setReadable(true)
            file.setWritable(true)
            file.setExecutable(true)

            return file.canExecute()
        }

        fun readFile(filePath: String): String {
            val su: List<String> = Shell.su("cat $filePath").exec().out
            val stringBuilder = StringBuilder()
            for (i: Int in 0 until su.size) {
                stringBuilder.append(su[i])
                if (i != su.size - 1) {
                    stringBuilder.append("\n")
                }
            }

            return if (!stringBuilder.toString().isEmpty()) {
                stringBuilder.toString()
            } else {
                "Fail"
            }
        }

        fun checkFilePresent(filePath: String): Boolean {
            val file = File(filePath)
            return file.exists()
        }

        fun getAndroidVersion(): String {
            when (Build.VERSION.SDK_INT) {
                21 -> return "5.0"
                22 -> return "5.1"
                23 -> return "6.0"
                24 -> return "7.0"
                25 -> return "7.1"
                26 -> return "8.0"
                27 -> return "8.1"
                28 -> return "9.0"
            }
            return "Fail"
        }

        fun getAndroidVersionName(): String {
            when (Build.VERSION.SDK_INT) {
                21, 22 -> return "Lollipop"
                23 -> return "Marshmallow"
                24, 25 -> return "Nougat"
                26, 27 -> return "Oreo"
                28 -> return "Pie"
            }
            return "Fail"
        }

        fun getABIs(): String {
            val stringBuilder = StringBuilder()
            for (i: Int in 0 until Build.SUPPORTED_ABIS.size) {
                stringBuilder.append(Build.SUPPORTED_ABIS[i])
                if (i < Build.SUPPORTED_ABIS.size - 1) {
                    stringBuilder.append("\n")
                }
            }
            return stringBuilder.toString()
        }

        fun getABI64(): String {
            val stringBuilder = StringBuilder()
            for (i: Int in 0 until Build.SUPPORTED_64_BIT_ABIS.size) {
                stringBuilder.append(Build.SUPPORTED_64_BIT_ABIS[i])
                if (i < Build.SUPPORTED_64_BIT_ABIS.size - 1) {
                    stringBuilder.append("\n")
                }
            }
            return stringBuilder.toString()
        }

        fun getABI32(): String {
            val stringBuilder = StringBuilder()
            for (i: Int in 0 until Build.SUPPORTED_32_BIT_ABIS.size) {
                stringBuilder.append(Build.SUPPORTED_32_BIT_ABIS[i])
                if (i < Build.SUPPORTED_32_BIT_ABIS.size - 1) {
                    stringBuilder.append("\n")
                }
            }
            return stringBuilder.toString()
        }

        fun unitConvert(long: Long): String {
            return when {
                long > 1024 * 1024 -> (long / 1024 / 1024).toString() + "MB"
                long > 1024 -> (long / 1024).toString() + "KB"
                else -> long.toString() + "B"
            }
        }

        fun ShortToast(context: Context, string: String) {
            Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
        }
    }
}