package com.earth.OsToolkit

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import com.earth.OsToolkit.base.Accessing.Companion.openPackage
import com.earth.OsToolkit.base.BaseKotlinOperation.Companion.ShortToast
import com.topjohnwu.superuser.Shell

import kotlinx.android.synthetic.main.activity_disableapp.*
import kotlinx.android.synthetic.main.activity_disableselection.*
import kotlinx.android.synthetic.main.view_appicon.view.*
import kotlinx.android.synthetic.main.view_selectdisable.view.*
import java.lang.Exception

/*
 * OsToolkit - Kotlin
 *
 * Date : 3 Feb 2019
 *
 * By   : 1552980358
 *
 */

/*
 * Modify
 *
 * 7-Feb-2019
 *
 */

@Suppress("all")
class DisableAppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disableapp)

        initialize()
        Thread {
            // 已加入列表 added list
            val savedList = getSharedPreferences("disabledApp", Context.MODE_PRIVATE)
                .getStringSet("added", setOf<String>())
            // 已禁用列表 disabled list
            val hidedList = Shell.su("pm list package -d").exec().out

            if (savedList!!.size > 0) {
                val packageManager = packageManager
                // 创建图标 Create Icon
                for ((j, i) in savedList.withIndex()) {
                    val appIconView = AppIconView(this, i, hidedList, packageManager)

                    // 定义图标位置
                    val layoutParams = GridLayout.LayoutParams()
                    layoutParams.rowSpec = GridLayout.spec(j / 4, 1f)
                    layoutParams.columnSpec = GridLayout.spec(j % 4, 1f)

                    runOnUiThread { gridLayout.addView(appIconView, layoutParams) }
                }
            } else {
                runOnUiThread { song.visibility = View.VISIBLE }
            }

            runOnUiThread {
                progressBar.visibility = View.GONE
            }
        }.start()
    }

    private fun initialize() {
        toolbar.setTitle(R.string.disable_toolbar)
        setSupportActionBar(toolbar)

        if (supportActionBar != null)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)

        toolbar.setNavigationOnClickListener { onBackPressed() }
        floatingActionButton.setOnClickListener {
            startActivityForResult(Intent(this, DisableSelectActivity::class.java), 0)
        }
        toolbar.setTitle(R.string.disable_toolbar)
        if (!getSharedPreferences("ui", Context.MODE_PRIVATE).getBoolean("disableNotice", true)) {
            notice.visibility = View.GONE
        }
        notice.setOnLongClickListener {
            getSharedPreferences("ui", Context.MODE_PRIVATE).edit().putBoolean("disableNotice", false).apply()
            notice.visibility = View.GONE
            true
        }
    }

    @Suppress("all")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 移除内容 重新加载 Remove all and reload
        gridLayout.removeAllViews()
        val dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.setContentView(LayoutInflater.from(this).inflate(R.layout.dialog_loading, null))
        dialog.show()

        Thread {
            runOnUiThread { song.visibility = View.GONE }
            val savedList = getSharedPreferences("disabledApp", Context.MODE_PRIVATE)
                .getStringSet("added", setOf<String>())
            val hidedList = Shell.su("pm list package -d").exec().out

            if (savedList!!.size > 0) {
                val packageManager = packageManager
                for ((j, i) in savedList.withIndex()) {
                    val appIconView = AppIconView(this, i, hidedList, packageManager)

                    val layoutParams = GridLayout.LayoutParams()
                    layoutParams.rowSpec = GridLayout.spec(j / 4, 1f)
                    layoutParams.columnSpec = GridLayout.spec(j % 4, 1f)

                    runOnUiThread { gridLayout.addView(appIconView, layoutParams) }
                }
            } else {
                runOnUiThread { song.visibility = View.VISIBLE }
            }

            runOnUiThread { dialog.cancel() }
        }.start()
    }

    @Suppress("all")
    class AppIconView(
        activity: Activity,
        packageName: String,
        hideList: List<String>,
        packageManager: PackageManager
    ) :
        LinearLayout(activity) {
        init {
            LayoutInflater.from(activity).inflate(R.layout.view_appicon, this)

            // 应用名 App's name
            title.text = packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.GET_META_DATA
                )
            ).toString()
            // 图标 Icon
            imageView.setImageDrawable(packageManager.getApplicationIcon(packageName))
            // 版本名 Version Name
            version.text = packageManager.getPackageInfo(packageName, 0).versionName

            if (!hideList.contains("package:$packageName")) {
                isDisabled.visibility = View.GONE
            }

            // 点击/长按监听 listener to click and long click
            root.setOnClickListener {
                Thread {
                    activity.runOnUiThread { isDisabled.visibility = View.GONE }
                    Shell.su("pm enable $packageName").exec()
                    try {
                        openPackage(activity, packageManager, packageName)
                    } catch (e: Exception) {
                        activity.runOnUiThread { ShortToast(activity, e.toString()) }
                    }
                }.start()
            }

            root.setOnLongClickListener {
                Thread {
                    activity.runOnUiThread { isDisabled.visibility = View.VISIBLE }
                    Shell.su("pm disable $packageName").exec()
                }.start()

                ShortToast(
                    activity as Context,
                    String.format(
                        activity.getString(R.string.t_disable),
                        packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0))
                    )
                )
                return@setOnLongClickListener true
            }

        }
    }

    @Suppress("all")
    class DisableSelectActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_disableselection)
            val dialog = Dialog(this)
            dialog.setCancelable(false)
            dialog.setContentView(LayoutInflater.from(this).inflate(R.layout.dialog_loading, null))
            dialog.show()

            Thread {
                // 获取已安装应用 fetch installed apps
                val installedPackage = packageManager.getInstalledPackages(0)
                // 获取已添加应用 fetch added apps
                val savedSet = getSharedPreferences("disabledApp", Context.MODE_PRIVATE)
                    .getStringSet("added", mutableSetOf<String>())
                // 显示 show
                for (i in installedPackage) {
                    val selectDisableView = SelectDisableView(this, i, packageManager, savedSet)
                    runOnUiThread { root.addView(selectDisableView) }
                }

                runOnUiThread { dialog.cancel() }
            }.start()
            initialize()
        }

        private fun initialize() {
            toolbar_sub.setTitle(R.string.disable_select)
            setSupportActionBar(toolbar_sub)

            if (supportActionBar != null)
                supportActionBar!!.setDisplayHomeAsUpEnabled(true)

            if (getSharedPreferences("ui", Context.MODE_PRIVATE).getBoolean("navBar", true)) {
                window.navigationBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
            }

            toolbar_sub.setNavigationOnClickListener { onBackPressed() }
        }

        override fun onBackPressed() {
            super.onBackPressed()
            setResult(0)
        }

        @Suppress("all")
        class SelectDisableView(
            activity: Activity,
            packageInfo: PackageInfo,
            packageManager: PackageManager,
            addedSet: MutableSet<String>
        ) :
            LinearLayout(activity) {
            init {
                LayoutInflater.from(activity).inflate(R.layout.view_selectdisable, this)
                // 应用名 App's name
                label.text = packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(
                        packageInfo.packageName,
                        PackageManager.GET_META_DATA
                    )
                )
                // 包名 package name
                name.text = packageInfo.packageName
                // 版本名 version name
                versionName.text = packageInfo.versionName
                // 图标 icon
                icon.setImageDrawable(packageManager.getApplicationIcon(packageInfo.packageName))

                // 设置是否添加 added status
                if (addedSet.contains(packageInfo.packageName)) {
                    checkBox.isChecked = true
                }

                // 设置监听 listeners
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    val dialog = Dialog(activity)
                    dialog.setCancelable(false)
                    dialog.setContentView(LayoutInflater.from(activity).inflate(R.layout.dialog_loading, null))
                    dialog.show()
                    if (isChecked) {
                        Thread {
                            addedSet.add(packageInfo.packageName)
                            activity.getSharedPreferences("disabledApp", Context.MODE_PRIVATE).edit()
                                .remove("added").commit()
                            activity.getSharedPreferences("disabledApp", Context.MODE_PRIVATE).edit()
                                .putStringSet("added", addedSet).commit()
                            Shell.su("pm disable ${packageInfo.packageName}").exec()
                            dialog.cancel()
                        }.start()
                    } else {
                        Thread {
                            addedSet.remove(packageInfo.packageName)
                            Log.i("addedSet", addedSet.toString())
                            activity.getSharedPreferences("disabledApp", Context.MODE_PRIVATE).edit()
                                .remove("added").commit()
                            if (addedSet.size > 0) {
                                activity.getSharedPreferences("disabledApp", Context.MODE_PRIVATE).edit()
                                    .putStringSet("added", addedSet).commit()
                            }
                            try {
                                Shell.su("pm enable ${packageInfo.packageName}").exec()
                            } catch (e: Exception) {
                                ShortToast(activity, e.toString())
                            }
                            dialog.cancel()
                        }.start()
                    }
                }
                rootRelative.setOnClickListener {
                    checkBox.isChecked = !checkBox.isChecked
                }
            }
        }
    }
}