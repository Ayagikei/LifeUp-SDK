package net.lifeupapp.lifeup.http

import android.app.Activity
import android.app.ForegroundServiceStartNotAllowedException
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
import net.lifeupapp.lifeup.api.Val.DOCUMENT_LINK_MCP
import net.lifeupapp.lifeup.api.Val.DOCUMENT_LINK_MCP_CN
import net.lifeupapp.lifeup.api.Val.DOCUMENT_LINK_MCP_HANT
import net.lifeupapp.lifeup.api.content.info.InfoApi
import net.lifeupapp.lifeup.http.databinding.ActivityMainBinding
import net.lifeupapp.lifeup.http.databinding.ItemConfigBinding
import net.lifeupapp.lifeup.http.qrcode.BarcodeScanningActivity
import net.lifeupapp.lifeup.http.service.ConnectStatusManager
import net.lifeupapp.lifeup.http.service.BroadcastGate
import net.lifeupapp.lifeup.http.service.KtorService
import net.lifeupapp.lifeup.http.service.LifeUpService
import net.lifeupapp.lifeup.http.utils.CloudControl
import net.lifeupapp.lifeup.http.utils.getIpAddressListInLocalNetwork
import net.lifeupapp.lifeup.http.ui.showIpAddressCopyDialog
import net.lifeupapp.lifeup.http.utils.setHtmlText

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val powerManager by lazy {
        getSystemService(POWER_SERVICE) as PowerManager
    }
    private val settings by lazy {
        net.lifeupapp.lifeup.http.utils.Settings.getInstance(this)
    }
    private var latestRunningState = LifeUpService.RunningState.NOT_RUNNING
    private var latestServiceError: Throwable? = null
    // Result of the last background LifeUp check; null until the first check finishes.
    private var lifeupStatus: LifeUpStatus? = null

    private var localIpAddresses: List<String> = emptyList()
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val myData: Intent? = result.data
                myData?.getStringExtra(BarcodeScanningActivity.SCAN_RESULT)?.let {
                    Log.i("MainActivity", "scan result: $it")
                    LifeUpApi.startApiActivity(this, it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)
        configureSystemBars()
        bindEdgeToEdge()

        initView()
        if (savedInstanceState == null) {
            handleCloudControlIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleCloudControlIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
        updateLocalIpAddress()
        refreshBroadcastStatus()
    }

    private fun initView() {
        lifecycleScope.launch {
            launch {
                KtorService.isRunning.collect { running ->
                    latestRunningState = running
                    if (running == LifeUpService.RunningState.RUNNING) {
                        latestServiceError = null
                    }
                    renderServiceState()
                }
            }

            launch {
                KtorService.errorMessage.collect { throwable ->
                    latestServiceError = throwable
                    renderServiceState()
                }
            }

            // Keep the IP summary aligned with the active server port.
            launch {
                KtorService.port.collect { port ->
                    if (port > 0) {
                        updateLocalIpAddress()
                    }
                }
            }

            binding.tvIntroduction.movementMethod = LinkMovementMethod.getInstance()
            binding.tvIntroduction.setHtmlText(getString(R.string.app_introduction))

            // Save all advanced settings in one action.
            binding.btnSaveAdvanced.setOnClickListener {
                validateAndSaveWakeLockDuration()
                validateAndSavePortSetting()
                validateAndSaveApiToken()
            }

            // Initialize the advanced settings form.
            binding.wakeLockDurationInput.setText(settings.wakeLockDuration.toString())
            binding.wakeLockDurationInput.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    validateAndSaveWakeLockDuration()
                }
            }

            // Initialize the API token input.
            binding.apiTokenInput.setText(settings.apiToken)
            binding.apiTokenInput.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    validateAndSaveApiToken()
                }
            }

            // Initialize the CORS toggle.
            binding.switchCors.isChecked = settings.enableCors
            binding.switchCors.setOnCheckedChangeListener { _, isChecked ->
                settings.enableCors = isChecked
                if (binding.switchStartService.isChecked) {
                    KtorService.restart()
                }
            }

            binding.switchEventWs.isChecked = settings.enableEventWs
            binding.switchEventWs.setOnCheckedChangeListener { _, isChecked ->
                settings.enableEventWs = isChecked
                if (binding.switchStartService.isChecked) {
                    KtorService.restart()
                }
            }

            binding.btnEnableBroadcasts.setOnClickListener {
                lifecycleScope.launch {
                    val ok = withContext(Dispatchers.IO) { BroadcastGate.enable() }
                    renderBroadcastStatus()
                    if (!ok) {
                        Toast.makeText(this@MainActivity, R.string.lifeup_broadcasts_enable_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            binding.btnOpenLab.setOnClickListener {
                LifeUpApi.startApiActivity(this@MainActivity, "lifeup://api/goto?page=lab")
            }

            // Configure the expandable sections.
            setupExpandablePanel(
                binding.advancedHeader,
                binding.btnToggleAdvanced,
                listOf(binding.cardAdvanced),
            )
            setupExpandablePanel(
                binding.aboutHeader,
                binding.btnToggleAbout,
                listOf(binding.cardAbout),
            )

            // Route both documentation entry points to the same action.
            binding.documentHeader.setOnClickListener {
                openLocalizedUrl(DOCUMENT_LINK, DOCUMENT_LINK_CN, DOCUMENT_LINK_CN_HANT)
            }
            binding.btnDocument.setOnClickListener {
                openLocalizedUrl(DOCUMENT_LINK, DOCUMENT_LINK_CN, DOCUMENT_LINK_CN_HANT)
            }
            binding.mcpHeader.setOnClickListener { showMcpQuickStartDialog() }
            binding.btnMcp.setOnClickListener { showMcpQuickStartDialog() }
            val setPermissionsExpanded = setupExpandablePanel(
                binding.permissionHeader,
                binding.btnTogglePermissions,
                listOf(
                    binding.includeOverlayConfig.root,
                    binding.includeBatteryConfig.root,
                    binding.includeContentProviderPermission.root,
                ),
                // Overlay and battery state are cheap synchronous checks; the
                // LifeUp provider check is asynchronous and applied in the loop below.
                expanded = !overlayPermissionReady() || !batteryPermissionReady(),
            )

            var lastPermissionsReadyApplied: Boolean? = null

            lifecycleScope.launchWhenResumed {
                withContext(Dispatchers.IO) {
                    while (true) {
                        val status = checkLifeUpStatus()
                        withContext(Dispatchers.Main) {
                            binding.tvStatusLifeup.text = when (status) {
                                LifeUpStatus.READY -> "✅ ${getString(R.string.lifeup_status_ready)}"
                                LifeUpStatus.NOT_AUTHORIZED -> "❌ ${getString(R.string.lifeup_status_not_authorized)}"
                                LifeUpStatus.NOT_RUNNING -> "⚠️ ${getString(R.string.lifeup_status_not_running)}"
                                LifeUpStatus.NOT_INSTALLED -> "❌ ${getString(R.string.lifeup_status_not_installed)}"
                            }
                            lifeupStatus = status
                            val ready = updatePermissionStatus()
                            if (ready != lastPermissionsReadyApplied) {
                                lastPermissionsReadyApplied = ready
                                setPermissionsExpanded(!ready)
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

            // Initialize the port input.
            binding.portSettingInput.setText(
                if (settings.customPort > 0)
                    settings.customPort.toString()
                else
                    ""
            )
            binding.portSettingInput.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    validateAndSavePortSetting()
                }
            }
        }

        binding.switchStartService.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isUserTriggeredCheckChange()) {
                return@setOnCheckedChangeListener
            }
            latestServiceError = null
            renderServiceState()
            if (isChecked) {
                KtorService.start()
            } else {
                KtorService.stop()
            }
            updateLocalIpAddress()
        }

        binding.btnCopyIp.setOnClickListener {
            updateLocalIpAddress()
            if (binding.btnCopyIp.isVisible) {
                showIpAddressCopyDialog(localIpAddresses, KtorService.port.value)
            }
        }

        binding.tvCloudSchemeStart.text = CloudControl.URL_START
        binding.tvCloudSchemeStop.text = CloudControl.URL_STOP
        binding.tvCloudSchemeStart.setOnClickListener { copyCloudScheme(CloudControl.URL_START) }
        binding.tvCloudSchemeStop.setOnClickListener { copyCloudScheme(CloudControl.URL_STOP) }

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
                    val intent =
                        Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
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
                if (lifeupStatus != LifeUpStatus.READY) {
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

        binding.btnQrcodeHelp.setOnClickListener {
            showQrcodeHelpDialog()
        }

        binding.btnQrcodeScan.setOnClickListener {
            resultLauncher.launch(Intent(this, BarcodeScanningActivity::class.java))
        }

        binding.tvAboutVersion.text = getString(R.string.cloud_version, BuildConfig.VERSION_NAME)
        binding.tvAboutDesc.setHtmlText(getString(R.string.about_text))
    }

    private fun bindEdgeToEdge() {
        val contentTopPadding = binding.contentContainer.paddingTop
        val contentBottomPadding = binding.contentContainer.paddingBottom
        val contentLeftPadding = binding.contentContainer.paddingLeft
        val contentRightPadding = binding.contentContainer.paddingRight
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.contentContainer.updatePadding(
                left = contentLeftPadding + systemBarsInsets.left,
                top = contentTopPadding + systemBarsInsets.top,
                right = contentRightPadding + systemBarsInsets.right,
                bottom = contentBottomPadding + systemBarsInsets.bottom
            )
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)
    }

    private fun configureSystemBars() {
        window.statusBarColor = Color.TRANSPARENT
        val isDarkTheme =
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                    Configuration.UI_MODE_NIGHT_YES
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
            !isDarkTheme
    }

    private fun renderServiceState() {
        val isRunning = latestRunningState == LifeUpService.RunningState.RUNNING
        val isStarting = latestRunningState == LifeUpService.RunningState.STARTING
        val errorMessage = latestServiceError?.let(::getServiceErrorMessage)

        val statusText = when {
            isRunning -> "✅ ${getString(R.string.serverStartedMessage)}"
            isStarting -> "⏳ ${getString(R.string.serverStartingMessage)}"
            errorMessage != null -> getString(R.string.serverStartFailedMessage, errorMessage)
            else -> "❌ ${getString(R.string.server_status)}"
        }

        binding.tvStatusServer.text = statusText
        binding.serverIpDetails.isVisible = isRunning
        binding.btnCopyIp.isVisible = isRunning && localIpAddresses.isNotEmpty()
        binding.switchStartService.isEnabled = !isStarting
        if (binding.switchStartService.isChecked != (isRunning || isStarting)) {
            binding.switchStartService.isChecked = isRunning || isStarting
        }
    }

    private fun getServiceErrorMessage(error: Throwable): String {
        val localizedMessage = error.localizedMessage
        return when {
            error is java.net.BindException && settings.customPort > 0 -> {
                getString(R.string.port_setting_conflict, settings.customPort)
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    error is ForegroundServiceStartNotAllowedException -> {
                getString(R.string.server_error_background_restricted)
            }

            localizedMessage.isNullOrBlank() -> {
                getString(R.string.server_error_unknown)
            }

            else -> {
                localizedMessage
            }
        }
    }

    private fun updateLocalIpAddress() {
        localIpAddresses = getIpAddressListInLocalNetwork().distinct()
        val localIpAddress = localIpAddresses.joinToString("\n") {
            "$it:${KtorService.port.value}"
        }
        if (localIpAddress.isNotBlank()) {
            binding.tvStatusServerIp.text =
                getString(R.string.localIpAddressMessage, localIpAddress)
        } else {
            binding.tvStatusServerIp.text = getString(R.string.ipAddressUnknown)
        }
        binding.tvStatusServerIpHint.isVisible = localIpAddresses.isNotEmpty()
        binding.btnCopyIp.isVisible =
            latestRunningState == LifeUpService.RunningState.RUNNING && localIpAddresses.isNotEmpty()
    }

    private fun updatePermissionStatus(): Boolean {
        val hasOverlayPermission = overlayPermissionReady()
        val hasBatteryPermission = batteryPermissionReady()
        val hasLifeupAccess = lifeupStatus == LifeUpStatus.READY
        // The configure button stays only on rows that still need action, so the
        // missing step is the one that stands out. Battery state cannot be read
        // reliably across vendors, so its button is always kept.
        renderPermissionRow(
            binding.includeOverlayConfig,
            hasOverlayPermission,
            R.string.status_badge_granted,
            R.string.status_badge_missing,
            hideButtonWhenReady = true,
        )
        renderPermissionRow(
            binding.includeBatteryConfig,
            hasBatteryPermission,
            R.string.status_badge_battery_ignored,
            R.string.status_badge_battery_missing,
            hideButtonWhenReady = false,
            // Not a hard failure: vendors may still apply their own limits either way.
            missingEmoji = "⚠️",
        )
        when (lifeupStatus) {
            // Unknown until the first background check finishes.
            null -> Unit
            LifeUpStatus.READY, LifeUpStatus.NOT_AUTHORIZED -> renderPermissionRow(
                binding.includeContentProviderPermission,
                hasLifeupAccess,
                R.string.status_badge_granted,
                R.string.status_badge_missing,
                hideButtonWhenReady = true,
            )
            // LifeUp must be running (or installed) before authorization can be checked.
            LifeUpStatus.NOT_RUNNING, LifeUpStatus.NOT_INSTALLED -> {
                val row = binding.includeContentProviderPermission
                row.tvBadge.isVisible = true
                row.tvBadge.text = "⚠️ ${getString(R.string.status_badge_unknown)}"
                row.tvBadge.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurfaceVariant))
                row.btn.isGone = false
            }
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            binding.includeOverlayConfig.btn.isGone = true
        }

        val doneCount = listOf(hasOverlayPermission, hasBatteryPermission, hasLifeupAccess).count { it }
        binding.tvPermissionSummary.text = getString(R.string.permission_summary, doneCount, 3)
        binding.tvPermissionSummary.setTextColor(
            if (doneCount == 3) colorAttr(androidx.appcompat.R.attr.colorPrimary)
            else colorAttr(com.google.android.material.R.attr.colorOnSurfaceVariant)
        )
        return hasOverlayPermission && hasBatteryPermission && hasLifeupAccess
    }

    private fun renderPermissionRow(
        row: ItemConfigBinding,
        ready: Boolean,
        @StringRes readyText: Int,
        @StringRes missingText: Int,
        hideButtonWhenReady: Boolean,
        missingEmoji: String = "❌",
    ) {
        row.tvBadge.isVisible = true
        row.tvBadge.text = if (ready) {
            "✅ ${getString(readyText)}"
        } else {
            "$missingEmoji ${getString(missingText)}"
        }
        row.tvBadge.setTextColor(
            if (ready) colorAttr(androidx.appcompat.R.attr.colorPrimary)
            else colorAttr(androidx.appcompat.R.attr.colorError)
        )
        row.btn.isGone = ready && hideButtonWhenReady
    }

    private fun colorAttr(@AttrRes attr: Int): Int =
        MaterialColors.getColor(binding.root, attr)

    private fun overlayPermissionReady(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            android.provider.Settings.canDrawOverlays(this)

    private fun batteryPermissionReady(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            powerManager.isIgnoringBatteryOptimizations(packageName)

    /**
     * Distinguishes why LifeUp data is unavailable. Must run off the main thread.
     *
     * - The provider client cannot be acquired when LifeUp is not running and the
     *   system (e.g. vendor auto-start control) refuses to start it.
     * - Once acquired, LifeUp returns a null cursor to callers it has not authorized.
     */
    private fun checkLifeUpStatus(): LifeUpStatus {
        val installed = runCatching {
            packageManager.getPackageInfo(Val.LIFEUP_PACKAGE_NAME, 0)
        }.isSuccess
        if (!installed) return LifeUpStatus.NOT_INSTALLED

        val client = runCatching {
            contentResolver.acquireUnstableContentProviderClient(LIFEUP_PROVIDER_AUTHORITY)
        }.getOrNull() ?: return LifeUpStatus.NOT_RUNNING
        try {
            val info = LifeUpApi.getContentProviderApi<InfoApi>().getInfo().getOrNull()
            return if ((info?.appVersion ?: 0) > 0) LifeUpStatus.READY else LifeUpStatus.NOT_AUTHORIZED
        } finally {
            // close() needs API 24; release() works on every supported version.
            @Suppress("DEPRECATION")
            client.release()
        }
    }

    private fun validateAndSaveWakeLockDuration() {
        val input = binding.wakeLockDurationInput.text.toString()
        val duration = input.toIntOrNull()
        if (duration != null && duration in net.lifeupapp.lifeup.http.utils.Settings.MIN_WAKE_LOCK_DURATION..net.lifeupapp.lifeup.http.utils.Settings.MAX_WAKE_LOCK_DURATION) {
            settings.wakeLockDuration = duration
            binding.wakeLockDurationLayout.error = null
        } else {
            binding.wakeLockDurationLayout.error = getString(R.string.wake_lock_duration_error)
            binding.wakeLockDurationInput.setText(settings.wakeLockDuration.toString())
        }
    }

    private fun openLocalizedUrl(en: String, cn: String, hant: String) {
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales.get(0)
        } else {
            @Suppress("DEPRECATION")
            resources.configuration.locale
        }
        val url = when {
            locale.language == "zh" && locale.country.toLowerCasePreservingASCIIRules() == "cn" -> cn
            locale.language == "zh" -> hant
            else -> en
        }
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun setupExpandablePanel(
        headerView: View,
        toggleButton: AppCompatImageButton,
        contentViews: List<View>,
        expanded: Boolean = false,
    ): (Boolean) -> Unit {
        var isExpanded = expanded

        fun apply(expanded: Boolean) {
            contentViews.forEach { it.isVisible = expanded }
            toggleButton.animate().rotation(if (expanded) 180f else 0f).setDuration(200).start()
        }

        fun togglePanel() {
            isExpanded = !isExpanded
            apply(isExpanded)
        }

        apply(expanded)
        headerView.setOnClickListener { togglePanel() }
        toggleButton.setOnClickListener { togglePanel() }
        return { expanded ->
            isExpanded = expanded
            apply(expanded)
        }
    }
    private fun validateAndSavePortSetting() {
        val input = binding.portSettingInput.text.toString()
        if (input.isEmpty()) {
            settings.customPort = 0
            binding.portSettingLayout.error = null
            return
        }

        val port = input.toIntOrNull()
        if (port != null && port in net.lifeupapp.lifeup.http.utils.Settings.MIN_PORT..net.lifeupapp.lifeup.http.utils.Settings.MAX_PORT) {
            settings.customPort = port
            binding.portSettingLayout.error = null
            // Restart the service so the new port is applied immediately.
            if (binding.switchStartService.isChecked) {
                KtorService.restart()
            }
        } else {
            binding.portSettingLayout.error = getString(R.string.port_setting_error)
            binding.portSettingInput.setText(
                if (settings.customPort > 0)
                    settings.customPort.toString()
                else
                    ""
            )
        }
    }

    private fun validateAndSaveApiToken() {
        val input = binding.apiTokenInput.text.toString().trim()
        settings.apiToken = input
        binding.apiTokenLayout.error = null

        // Restart the service so the new token is applied immediately.
        if (binding.switchStartService.isChecked) {
            KtorService.restart()
        }
    }

    private fun CompoundButton.isUserTriggeredCheckChange(): Boolean {
        return isPressed
    }

    private fun showQrcodeHelpDialog() {
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.qrcode_help_title)
            .setMessage(R.string.qrcode_help_message)
            .setPositiveButton(R.string.qrcode_help_ok, null)
            .show()
        dialog.findViewById<TextView>(com.google.android.material.R.id.message)?.apply {
            setLineSpacing(0f, 1.3f)
        }
    }

    private fun showMcpQuickStartDialog() {
        val prompt = getString(
            R.string.mcp_quick_start_prompt,
            if (isSimplifiedChinese()) Val.MCP_QUICK_START_URL_CN else Val.MCP_QUICK_START_URL,
        )
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.mcp_quick_start_title)
            .setMessage(getString(R.string.mcp_quick_start_message, prompt))
            .setPositiveButton(R.string.mcp_quick_start_copy) { _, _ ->
                copyPlainText(prompt)
            }
            .setNeutralButton(R.string.mcp_quick_start_guide) { _, _ ->
                openLocalizedUrl(DOCUMENT_LINK_MCP, DOCUMENT_LINK_MCP_CN, DOCUMENT_LINK_MCP_HANT)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
        dialog.findViewById<TextView>(android.R.id.message)?.setLineSpacing(0f, 1.3f)
    }

    private fun isSimplifiedChinese(): Boolean {
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales.get(0)
        } else {
            @Suppress("DEPRECATION")
            resources.configuration.locale
        }
        return locale.language == "zh" &&
            locale.country.toLowerCasePreservingASCIIRules() == "cn"
    }

    private fun copyPlainText(value: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("lifeup-mcp", value))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(this, R.string.mcp_quick_start_copied, Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshBroadcastStatus() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) { BroadcastGate.refresh(force = true) }
            renderBroadcastStatus()
        }
    }

    private fun renderBroadcastStatus() {
        if (!::binding.isInitialized) return
        when (BroadcastGate.status) {
            BroadcastGate.Status.On -> {
                binding.tvLifeupBroadcasts.setText(R.string.lifeup_broadcasts_on)
                binding.btnEnableBroadcasts.isGone = true
                binding.btnOpenLab.isGone = true
            }
            BroadcastGate.Status.Off -> {
                binding.tvLifeupBroadcasts.setText(R.string.lifeup_broadcasts_off)
                binding.btnEnableBroadcasts.isVisible = true
                binding.btnOpenLab.isGone = true
            }
            BroadcastGate.Status.Unsupported -> {
                binding.tvLifeupBroadcasts.setText(R.string.lifeup_broadcasts_unknown)
                binding.btnEnableBroadcasts.isGone = true
                binding.btnOpenLab.isVisible = true
            }
            BroadcastGate.Status.Unknown -> {
                binding.tvLifeupBroadcasts.setText(R.string.lifeup_broadcasts_unknown)
                binding.btnEnableBroadcasts.isGone = true
                binding.btnOpenLab.isVisible = true
            }
        }
    }

    private fun handleCloudControlIntent(intent: Intent?) {
        when (CloudControl.parse(intent?.data?.scheme, intent?.data?.host)) {
            CloudControl.Action.START -> {
                latestServiceError = null
                KtorService.start()
            }
            CloudControl.Action.STOP -> KtorService.stop()
            null -> Unit
        }
    }

    private fun copyCloudScheme(url: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("lifeupcloud", url))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(this, getString(R.string.cloud_scheme_copied, url), Toast.LENGTH_SHORT).show()
        }
    }

}

private const val LIFEUP_PROVIDER_AUTHORITY = "net.sarasarasa.lifeup.provider.api"

private enum class LifeUpStatus { READY, NOT_AUTHORIZED, NOT_RUNNING, NOT_INSTALLED }
