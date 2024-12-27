package net.lifeupapp.lifeup.http

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import io.ktor.util.toLowerCasePreservingASCIIRules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lifeupapp.lifeup.api.LifeUpApi
import net.lifeupapp.lifeup.api.Val
import net.lifeupapp.lifeup.api.Val.DOCUMENT_LINK
import net.lifeupapp.lifeup.api.Val.DOCUMENT_LINK_CN
import net.lifeupapp.lifeup.api.Val.DOCUMENT_LINK_CN_HANT
import net.lifeupapp.lifeup.api.content.info.InfoApi
import net.lifeupapp.lifeup.http.databinding.ActivityMainBinding
import net.lifeupapp.lifeup.http.qrcode.BarcodeScanningActivity
import net.lifeupapp.lifeup.http.service.ConnectStatusManager
import net.lifeupapp.lifeup.http.service.KtorService
import net.lifeupapp.lifeup.http.service.LifeUpService
import net.lifeupapp.lifeup.http.utils.getIpAddressListInLocalNetwork
import net.lifeupapp.lifeup.http.utils.setHtmlText

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val powerManager by lazy {
        getSystemService(POWER_SERVICE) as PowerManager
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val myData: Intent? = result.data
                if (myData != null) {
                    myData.getStringExtra(BarcodeScanningActivity.SCAN_RESULT)?.let {
                        Log.i("MainActivity", "scan result: $it")
                        LifeUpApi.startApiActivity(this, it)
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // init view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // init the view logic
        initView()
    }

    private fun initView() {
        lifecycleScope.launch {
            launch {
                KtorService.isRunning.collect { running ->
                    if (running == LifeUpService.RunningState.RUNNING || running == LifeUpService.RunningState.STARTING) {
                        binding.serverStatusText.text = getString(R.string.serverStartedMessage)
                        binding.switchStartService.isChecked = true
                    } else {
                        binding.serverStatusText.text = getString(R.string.server_status)
                        binding.switchStartService.isChecked = false
                    }
                }
            }

            lifecycleScope.launchWhenResumed {
                withContext(Dispatchers.IO) {
                    while (true) {
                        val isRunning = checkContentProviderAvailable()
                        withContext(Dispatchers.Main) {
                            binding.lifeupStatusText.text = if (isRunning) {
                                getString(R.string.lifeup_status_normal)
                            } else {
                                getString(R.string.lifeup_status_unknown)
                            }
                        }
                        delay(5000L)
                    }
                }
            }

            launch {
                ConnectStatusManager.networkChangedEvent.sample(500L).collect {
                    updateLocalIpAddress()
                }
            }
        }

        binding.switchStartService.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                KtorService.start()
            } else {
                KtorService.stop()
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            binding.includeBatteryConfig.btn.isGone = true
        }

        binding.includeBatteryConfig.apply {
            this.tvTitle.setText(R.string.ignore_battery_optimizations)
            this.tvDesc.setText(R.string.ignore_battery_optimizations_desc)

            this.btn.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !powerManager.isIgnoringBatteryOptimizations(
                        this@MainActivity.packageName
                    )
                ) {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    intent.resolveActivity(this@MainActivity.packageManager)?.let {
                        this@MainActivity.startActivity(intent)
                    }
                }
            }
        }

        binding.includeContentProviderPermission.apply {
            this.tvTitle.setText(R.string.content_provider_permission)
            this.tvDesc.setText(R.string.content_provider_permission_desc)
            this.btn.setOnClickListener {
                val hasPermission = checkContentProviderAvailable()
                if (!hasPermission) {
                    try {
                        LifeUpApi.requestContentProviderPermission(getString(R.string.app_name))
                    } catch (e: ActivityNotFoundException) {
                        val uri = Uri.parse("market://details?id=" + Val.LIFEUP_PACKAGE_NAME)
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        runCatching {
                            this@MainActivity.startActivity(intent)
                        }.onFailure {
                            Log.w(this.javaClass.simpleName, it.stackTraceToString())
                        }
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.lifeup_permission_granted),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.includeOverlayConfig.btn.setOnClickListener {
                val intent = Intent(ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        } else {
            binding.includeOverlayConfig.btn.isGone = true
        }

        binding.btnQrcodeScan.setOnClickListener {
            resultLauncher.launch(Intent(this, BarcodeScanningActivity::class.java))
        }

        binding.tvAboutVersion.text = getString(R.string.cloud_version, BuildConfig.VERSION_NAME)

        binding.btnDocument.setOnClickListener {
            // get current locale
            val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                resources.configuration.locales.get(0)
            } else {
                resources.configuration.locale
            }
            val intent = Intent(Intent.ACTION_VIEW)
            val url = when {
                locale.language == "zh" && locale.country.toLowerCasePreservingASCIIRules() == "cn" -> {
                    DOCUMENT_LINK_CN
                }

                locale.language == "zh" -> {
                    DOCUMENT_LINK_CN_HANT
                }

                else -> {
                    DOCUMENT_LINK
                }
            }
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }

    private fun updateLocalIpAddress() {
        val localIpAddress =
            getIpAddressListInLocalNetwork().joinToString("\n") {
                "$it:${KtorService.port}"
            }
        if (localIpAddress.isNotBlank()) {
            binding.ipAddressText.text = getString(R.string.localIpAddressMessage, localIpAddress)
        } else {
            binding.ipAddressText.text = getString(R.string.ipAddressUnknown)
        }

        binding.tvAboutDesc.setHtmlText(R.string.about_text)
        binding.tvAboutDesc.movementMethod = android.text.method.LinkMovementMethod.getInstance()
        binding.tvAboutDesc.linksClickable = true
    }

    private fun checkContentProviderAvailable() =
        (LifeUpApi.getContentProviderApi<InfoApi>().getInfo().getOrNull()?.appVersion ?: 0) > 0
}
