@file:Suppress("ObjectPropertyName")

package net.lifeupapp.lifeup.http.service

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondOutputStream
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.html
import kotlinx.html.i
import kotlinx.html.p
import kotlinx.html.stream.appendHTML
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import net.lifeupapp.lifeup.api.LifeUpApi
import net.lifeupapp.lifeup.api.Val
import net.lifeupapp.lifeup.api.content.achievements.AchievementApi
import net.lifeupapp.lifeup.api.content.data.DataApi
import net.lifeupapp.lifeup.api.content.feelings.FeelingsApi
import net.lifeupapp.lifeup.api.content.info.InfoApi
import net.lifeupapp.lifeup.api.content.pomodoro.PomodoroApi
import net.lifeupapp.lifeup.api.content.records.CoinRecordsApi
import net.lifeupapp.lifeup.api.content.records.ExpRecordsApi
import net.lifeupapp.lifeup.api.content.records.InventoryRecordsApi
import net.lifeupapp.lifeup.api.content.records.LevelDefinesApi
import net.lifeupapp.lifeup.api.content.records.StatisticsApi
import net.lifeupapp.lifeup.api.content.records.StepRecordsApi
import net.lifeupapp.lifeup.api.content.shop.ItemsApi
import net.lifeupapp.lifeup.api.content.skills.SkillsApi
import net.lifeupapp.lifeup.api.content.syntheis.SynthesisApi
import net.lifeupapp.lifeup.api.content.tasks.SmartListType
import net.lifeupapp.lifeup.api.content.tasks.Task
import net.lifeupapp.lifeup.api.content.tasks.TasksApi
import net.lifeupapp.lifeup.http.base.AppScope
import net.lifeupapp.lifeup.http.BuildConfig
import net.lifeupapp.lifeup.http.base.appCtx
import net.lifeupapp.lifeup.http.utils.Settings
import net.lifeupapp.lifeup.http.utils.WakeLockManager
import net.lifeupapp.lifeup.http.utils.getIpAddressInLocalNetwork
import net.lifeupapp.lifeup.http.utils.getUriForFile
import net.lifeupapp.lifeup.http.vo.HttpResponse
import net.lifeupapp.lifeup.http.vo.RawQueryVo
import net.lifeupapp.lifeup.http.vo.wrapAsResponse
import org.json.JSONException
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

internal fun resolveStartPort(customPort: Int, retryPortOverride: Int?): Int {
    return retryPortOverride ?: if (customPort > 0) customPort else Settings.DEFAULT_PORT
}

internal fun nextAutoRetryPort(currentPort: Int): Int {
    return if (currentPort + 1 > Settings.MAX_PORT) {
        Settings.MIN_PORT
    } else {
        currentPort + 1
    }
}

internal fun buildServerBaseUrl(host: String, port: Int): String {
    return "http://$host:$port"
}

internal fun parseSmartListType(raw: String?): SmartListType {
    return SmartListType.fromPathSegment(raw)
        ?: throw IllegalArgumentException("invalid parameter")
}

/** Provider default includes sub-tasks. Only an exact false, ignoring case, turns that off. */
internal fun includeSubTasks(raw: String?): Boolean {
    return !raw.equals("false", ignoreCase = true)
}

internal fun Route.registerTaskListRoutes(
    execution: ProviderExecution = sharedProviderExecution,
    queryTasks: (categoryId: Long?, withSubTasks: Boolean) -> Result<List<Task>>,
) {
    route("/tasks") {
        get {
            val withSubTasks = includeSubTasks(call.request.queryParameters["withSubTasks"])
            call.respondProviderResult(
                "GET /tasks",
                block = { queryTasks(null, withSubTasks) },
                execution = execution,
            ) {
                call.respondResource(it.wrapAsResponse())
            }
        }
        route("/{id}") {
            get {
                val categoryId = call.parameters["id"]?.toLongOrNull()
                val withSubTasks = includeSubTasks(call.request.queryParameters["withSubTasks"])
                call.respondProviderResult(
                    "GET /tasks/{id}",
                    block = { queryTasks(categoryId, withSubTasks) },
                    execution = execution,
                ) {
                    call.respondResource(it.wrapAsResponse())
                }
            }
        }
    }
}

internal fun Route.registerSmartListTasksRoute(
    execution: ProviderExecution = sharedProviderExecution,
    queryTasks: (SmartListType, Boolean) -> Result<List<Task>>,
) {
    route("/smart_lists") {
        route("/{type}") {
            route("/tasks") {
                get {
                    val type = try {
                        parseSmartListType(call.parameters["type"])
                    } catch (_: IllegalArgumentException) {
                        call.respond(HttpResponse.error<String>("invalid parameter"))
                        return@get
                    }
                    val withSubTasks = includeSubTasks(call.request.queryParameters["withSubTasks"])
                    call.respondProviderResult(
                        "GET /smart_lists/{type}/tasks",
                        block = { queryTasks(type, withSubTasks) },
                        execution = execution,
                    ) {
                        call.respondResource(it.wrapAsResponse())
                    }
                }
            }
        }
    }
}
object KtorService : LifeUpService {

    private val _port = MutableStateFlow(Settings.DEFAULT_PORT)
    val port: StateFlow<Int> = _port

    private val logger = Logger.getLogger("LifeUp-Http")
    private val scope = AppScope
    private val mutex = Mutex()
    private val wakeLockManager = WakeLockManager("KtorService")
    private val mDnsService = MDnsService()

    private val _isRunning = MutableStateFlow(LifeUpService.RunningState.NOT_RUNNING)
    private val _errorMessage = MutableStateFlow<Throwable?>(null)
    private var server: NettyApplicationEngine? = null
    private var lastRequestTime = 0L
    private var isStarting = false
    private var isStopping = false

    override val isRunning: StateFlow<LifeUpService.RunningState>
        get() = _isRunning

    override val errorMessage: StateFlow<Throwable?>
        get() = _errorMessage

    private val RequestMoreWakeLockPlugin =
        createApplicationPlugin(name = "RequestMoreWakeLockPlugin") {
            onCall { _ ->
                if (SystemClock.elapsedRealtime() - lastRequestTime > 3.minutes.toLong(DurationUnit.MILLISECONDS)) {
                    logger.info("Requesting wake lock")
                    lastRequestTime = SystemClock.elapsedRealtime()
                    val duration = Settings.getInstance(appCtx).wakeLockDuration
                    wakeLockManager.stayAwake(duration.minutes.toLong(DurationUnit.MILLISECONDS))
                }
            }
        }

    override fun start() {
        scope.launch(Dispatchers.IO) { startInternal() }
    }

    fun restart() {
        scope.launch(Dispatchers.IO) {
            stopInternal()
            startInternal()
        }
    }

    private suspend fun startInternal(portOverride: Int? = null) {
            var retryWithNextPort: Int? = null
            mutex.withLock {
                if (_isRunning.value != LifeUpService.RunningState.NOT_RUNNING || isStarting) {
                    logger.info("Server is already running or starting")
                    return
                }
                isStarting = true
                _errorMessage.value = null
                _isRunning.value = LifeUpService.RunningState.STARTING
            }
            val customPort = Settings.getInstance(appCtx).customPort
            val startPort = resolveStartPort(customPort = customPort, retryPortOverride = portOverride)

            try {
                ServerNotificationService.start(appCtx)

                // Fully stop any previous server instance before recreating it.
                server?.stop(1000, 2000)
                server = null

                // Give the OS a moment to release the previous port binding.
                delay(500)

                // Prefer the retry override so automatic retries do not fall back to the default port.
                _port.value = startPort

                // Create and start the new server instance.
                server = createServer()
                server?.start(wait = false)

                // Poll briefly until the server starts responding.
                var retryCount = 0
                while (!isServerResponding() && retryCount < 5) {
                    delay(500)
                    retryCount++
                }

                if (isServerResponding()) {
                    _errorMessage.value = null
                    val duration = Settings.getInstance(appCtx).wakeLockDuration
                    wakeLockManager.stayAwake(duration.minutes.toLong(DurationUnit.MILLISECONDS))
                    EventHub.start(appCtx)
                    mDnsService.registerNsdService(port.value)
                    _isRunning.value = LifeUpService.RunningState.RUNNING
                } else {
                    throw Exception("Server failed to start after multiple retries")
                }
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "Failed to start server", e)
                _errorMessage.value = e
                _isRunning.value = LifeUpService.RunningState.NOT_RUNNING
                server?.stop(1000, 2000)
                server = null
                ServerNotificationService.cancel(appCtx)
                wakeLockManager.release()
                EventHub.stop(appCtx)
                mDnsService.unregisterNsdService()

                // Retry on the next port only when automatic port selection is in use.
                if (e is java.net.BindException && customPort == 0) {
                    retryWithNextPort = nextAutoRetryPort(startPort)
                }
            } finally {
                isStarting = false
            }

            retryWithNextPort?.let { nextPort ->
                startInternal(nextPort)
            }
    }

    override fun stop() {
        scope.launch(Dispatchers.IO) { stopInternal() }
    }

    private suspend fun stopInternal() {
        mutex.withLock {
            if (_isRunning.value == LifeUpService.RunningState.NOT_RUNNING || isStopping) {
                logger.info("Server is already stopped or stopping")
                return
            }
            isStopping = true
        }

        try {
            EventHub.stop(appCtx)
            server?.stop(1000, 2000)
            server = null
            ServerNotificationService.cancel(appCtx)
            wakeLockManager.release()
            mDnsService.unregisterNsdService()
            _errorMessage.value = null
            _isRunning.value = LifeUpService.RunningState.NOT_RUNNING

            // Give the runtime a moment to release networking resources.
            delay(500)
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Error stopping server", e)
            _errorMessage.value = e
        } finally {
            isStopping = false
        }
    }


    private fun createServer() = embeddedServer(Netty, port.value, watchPaths = emptyList()) {
        install(WebSockets)
        install(CallLogging)
        install(ContentNegotiation) {
            json(CloudJson)
        }
        installResourceConditionalGet()
        install(RequestMoreWakeLockPlugin)

        if (Settings.getInstance(appCtx).enableCors) {
            Log.i("KtorService", "enableCors is true")
            install(CORS) {
                configureCloudCors()
            }
        } else {
            Log.i("KtorService", "enableCors is false")
        }

        install(StatusPages) {
            exception<Throwable> { call, cause ->
                logger.log(Level.WARNING, "Unhandled exception", cause)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    HttpResponse.error<String>(cause)
                )
            }
        }

        // Enforce the optional API token before dispatching request handlers.
        installApiTokenValidation(Settings.getInstance(appCtx).apiToken)

        routing {
            registerDiagnosticsRoute(
                cloudVersion = BuildConfig.VERSION_CODE,
                cloudVersionName = BuildConfig.VERSION_NAME,
                androidApi = android.os.Build.VERSION.SDK_INT,
            )
            get("/") {
                val localAddressIp = getIpAddressInLocalNetwork() ?: "UNKNOWN"
                val serverBaseUrl = buildServerBaseUrl(localAddressIp, port.value)
                call.respondText(ContentType.Text.Html) {
                    buildString {
                        appendHTML().html {
                            body {
                                h1 {
                                    +"Hello from "
                                    i {
                                        +"LifeUp Cloud!"
                                    }
                                }
                                p { +"Now you can call LifeUp api on your computers (until the app is killed by the Android)." }
                                p { +"take the 'lifeup://api/reward?type=coin&content=Call LifeUp API from HTTP&number=1' as a example" }
                                h2 {
                                    +"GET request"
                                }
                                p {
                                    +"$serverBaseUrl/api?url=YOUR_ENCODED_API_URL"
                                }
                                p {
                                    +"you can send the get request to call in directly, but you need to encode the API like this: "
                                }
                                p {
                                    a(
                                        href = "$serverBaseUrl/api?url=lifeup%3A%2F%2Fapi%2Freward%3Ftype%3Dcoin%26content%3DCall%20LifeUp%20API%20from%20HTTP%26number%3D1",
                                        target = "_blank"
                                    ) {
                                        +"$serverBaseUrl/api?url=lifeup%3A%2F%2Fapi%2Freward%3Ftype%3Dcoin%26content%3DCall%20LifeUp%20API%20from%20HTTP%26number%3D1"
                                    }
                                }
                                div()
                                h2 {
                                    +"POST request"
                                }
                                p {
                                    +"$serverBaseUrl/api"
                                }
                                p {
                                    +"or you can POST it the below URL with 'application/json' content type and the body is a json string like this: "
                                }
                                p {
                                    +"{\n"
                                    +"  \"url\": \"lifeup://api/reward?type=coin&content=Call LifeUp API from HTTP&number=1\"\n"
                                    +"}"
                                }
                            }
                        }
                    }
                }
            }

            get("/api") {
                kotlin.runCatching {
                    call.request.queryParameters.getAll("url")?.forEach { url ->
                        logger.info("Got url: $url")
                        LifeUpApi.startApiActivity(appCtx, url)
                    }
                    HttpResponse.success("success")
                }.onSuccess {
                    call.respond(it)
                }.onFailure {
                    call.respond(HttpResponse.error<String>(it))
                }
            }

            get("/api/contentprovider") {
                val urls = call.request.queryParameters.getAll("url")
                call.respondContentProviderGet(urls) { url ->
                    LifeUpApi.callApiWithContentProvider(url)?.toJson()
                }
            }

            post<RawQueryVo>("/api") {
                kotlin.runCatching {
                    logger.info("Got url: ${it.url}")
                    it.urls?.forEach {
                        logger.info("Got url: $it")
                        LifeUpApi.startApiActivity(appCtx, it)
                    }
                    it.url?.let { it1 -> LifeUpApi.startApiActivity(appCtx, it1) }
                    HttpResponse.success("success")
                }.onSuccess {
                    call.respond(it)
                }.onFailure {
                    call.respond(HttpResponse.error<String>(it))
                }
            }

            post<RawQueryVo>("/api/contentprovider") { body ->
                call.respondContentProviderPost(body) { url ->
                    LifeUpApi.callApiWithContentProvider(url)?.toJson()
                }
            }

            get("/files/{url}") {
                val param = call.parameters["url"]
                if (param.isNullOrBlank()) {
                    call.respond(HttpResponse.error<String>(IllegalArgumentException("url is required")))
                } else {
                    logger.info("Got url: $param")
                    call.respondOutputStream {
                        appCtx.contentResolver.openInputStream(Uri.parse(param))?.use {
                            it.copyTo(this)
                        }
                    }
                }
            }

            post("/files/upload") {
                val multipart = call.receiveMultipart()
                val uploadedFiles = mutableListOf<Uri>()

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            val fileName = part.originalFileName
                                ?: "uploaded_image_${System.currentTimeMillis()}.jpg"
                            val fileBytes = part.streamProvider().readBytes()
                            val tempFile = File(appCtx.externalCacheDir, fileName)
                            tempFile.writeBytes(fileBytes)

                            // Use FileProvider so external apps can read the exported file safely.
                            val contentUri = tempFile.getUriForFile()
                            uploadedFiles.add(contentUri)
                        }

                        else -> {}
                    }
                    part.dispose()
                }

                if (uploadedFiles.isNotEmpty()) {
                    call.respond(HttpResponse.success(uploadedFiles.map { it.toString() }))
                } else {
                    call.respond(HttpResponse.error<List<String>>("No files received"))
                }
            }

            registerTaskListRoutes { categoryId, withSubTasks ->
                LifeUpApi.getContentProviderApi<TasksApi>().listTasks(categoryId, withSubTasks)
            }

            registerSmartListTasksRoute { type, withSubTasks ->
                LifeUpApi.getContentProviderApi<TasksApi>().listSmartListTasks(type, withSubTasks)
            }
            route("/history") {
                get {
                    val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
                    val filterGid = call.request.queryParameters["gid"]?.toLongOrNull()
                    call.respondProviderResult("GET /history", {
                        LifeUpApi.getContentProviderApi<TasksApi>()
                            .listHistory(offset, limit, filterGid)
                    }) {
                        call.respondResource(it.wrapAsResponse())
                    }
                }
            }

            route("/items") {
                get {
                    val ids = call.parameters.getAll("id")?.mapNotNull { it.toLongOrNull() }
                    call.respondProviderResult("GET /items", {
                        if (ids != null) {
                            LifeUpApi.getContentProviderApi<ItemsApi>().listItemsByIds(ids)
                        } else {
                            LifeUpApi.getContentProviderApi<ItemsApi>().listItems(null)
                        }
                    }) {
                        call.respondResource(it.wrapAsResponse())
                    }
                }
                route("/{listId}") {
                    get {
                        val listId = call.parameters["listId"]?.toLongOrNull()
                        call.respondProviderResult("GET /items/{listId}", {
                            LifeUpApi.getContentProviderApi<ItemsApi>().listItems(listId)
                        }) {
                            call.respondResource(it.wrapAsResponse())
                        }
                    }
                }
            }

            route("/tasks_categories") {
                get {
                    call.respondProviderResult("GET /tasks_categories", {
                        LifeUpApi.getContentProviderApi<TasksApi>().listCategories()
                    }) {
                        call.respondResource(it.wrapAsResponse())
                    }
                }
            }

            route("/achievement_categories") {
                get {
                    call.respondProviderResult("GET /achievement_categories", {
                        LifeUpApi.getContentProviderApi<AchievementApi>().listCategories()
                    }) {
                        call.respondResource(it.wrapAsResponse())
                    }
                }
            }

            route("/items_categories") {
                get {
                    val includeHidden =
                        call.request.queryParameters["include_hidden"]?.toBooleanStrictOrNull() ?: false
                    call.respondProviderResult("GET /items_categories", {
                        LifeUpApi.getContentProviderApi<ItemsApi>().listCategories(includeHidden)
                    }) {
                        call.respondResource(it.wrapAsResponse())
                    }
                }
            }

            get("/info") {
                call.respondProviderResult("GET /info", {
                    LifeUpApi.getContentProviderApi<InfoApi>().getInfo()
                }) { info ->
                    call.respondResource(
                        info.copy(
                            cloudVersion = BuildConfig.VERSION_CODE,
                            cloudVersionName = BuildConfig.VERSION_NAME,
                        ).wrapAsResponse(),
                    )
                }
            }

            route("/skills") {
                get {
                    call.respondProviderResult("GET /skills", {
                        LifeUpApi.getContentProviderApi<SkillsApi>().listSkills()
                    }) {
                        call.respondResource(it.wrapAsResponse())
                    }
                }
            }

            route("/achievements") {
                get {
                    call.respondProviderResult("GET /achievements", {
                        LifeUpApi.getContentProviderApi<AchievementApi>().listAchievements()
                    }) {
                        call.respondResource(it.wrapAsResponse())
                    }
                }
                route("/{id}") {
                    get {
                        val categoryId = call.parameters["id"]?.toLongOrNull()
                        call.respondProviderResult("GET /achievements/{id}", {
                            LifeUpApi.getContentProviderApi<AchievementApi>()
                                .listAchievements(categoryId)
                        }) {
                            call.respondResource(it.wrapAsResponse())
                        }
                    }
                }
            }

            route("/feelings") {
                get {
                    val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
                    call.respondProviderResult(
                        "GET /feelings",
                        {
                            LifeUpApi.getContentProviderApi<FeelingsApi>().listFeelings(offset, limit)
                        },
                        onFailure = { error ->
                            logger.log(Level.WARNING, "Failed to get feelings", error)
                        },
                    ) {
                        call.respondResource(it.wrapAsResponse())
                    }
                }
            }

            route("/synthesis") {
                get {
                    call.respondProviderResult(
                        "GET /synthesis",
                        {
                            LifeUpApi.getContentProviderApi<SynthesisApi>().listSynthesis(null)
                        },
                        onFailure = { error ->
                            Log.e("LifeUp-Http", "Failed to get synthesis", error)
                        },
                    ) {
                        call.respondResource(it.wrapAsResponse())
                    }
                }
                route("/{id}") {
                    get {
                        val id = call.parameters["id"]?.toLongOrNull()
                        call.respondProviderResult("GET /synthesis/{id}", {
                            LifeUpApi.getContentProviderApi<SynthesisApi>().listSynthesis(id)
                        }) {
                            call.respondResource(it.wrapAsResponse())
                        }
                    }
                }
            }

            route("/synthesis_categories") {
                get {
                    val includeHidden =
                        call.request.queryParameters["include_hidden"]?.toBooleanStrictOrNull() ?: false
                    call.respondProviderResult("GET /synthesis_categories", {
                        LifeUpApi.getContentProviderApi<SynthesisApi>()
                            .listCategories(includeHidden = includeHidden)
                    }) {
                        call.respondResource(it.wrapAsResponse())
                    }
                }
                route("/{id}") {
                    get {
                        val id = call.parameters["id"]?.toLongOrNull()
                        val includeHidden =
                            call.request.queryParameters["include_hidden"]?.toBooleanStrictOrNull() ?: false
                        call.respondProviderResult("GET /synthesis_categories/{id}", {
                            LifeUpApi.getContentProviderApi<SynthesisApi>()
                                .listCategories(id, includeHidden)
                        }) {
                            call.respondResource(it.wrapAsResponse())
                        }
                    }
                }
            }

            route("/skill_groups") {
                get {
                    val includeHidden =
                        call.request.queryParameters["include_hidden"]?.toBooleanStrictOrNull() ?: false
                    call.respondProviderResult("GET /skill_groups", {
                        LifeUpApi.getContentProviderApi<SkillsApi>().listSkillGroups(includeHidden)
                    }) {
                        call.respondResource(it.wrapAsResponse())
                    }
                }
            }

            route("/achievement_conditions") {
                route("/{id}") {
                    get {
                        val id = call.parameters["id"]?.toLongOrNull()
                            ?: return@get call.respond(
                                HttpResponse.error<String>(IllegalArgumentException("id")),
                            )
                        call.respondProviderResult("GET /achievement_conditions/{id}", {
                            LifeUpApi.getContentProviderApi<AchievementApi>().listConditions(id)
                        }) {
                            call.respondResource(it.wrapAsResponse())
                        }
                    }
                }
            }

            route("/pomodoro_records") {
                get {
                    val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
                    val timeRangeStart =
                        call.request.queryParameters["time_range_start"]?.toLongOrNull()
                    val timeRangeEnd =
                        call.request.queryParameters["time_range_end"]?.toLongOrNull()
                    call.respondProviderResult(
                        "GET /pomodoro_records",
                        {
                            LifeUpApi.getContentProviderApi<PomodoroApi>()
                                .listRecords(offset, limit, timeRangeStart, timeRangeEnd)
                        },
                        onFailure = { error ->
                            logger.log(Level.WARNING, "Failed to get pomodoro records", error)
                        },
                    ) {
                        call.respondResource(it.wrapAsResponse())
                    }
                }
            }

            route("/coin_records") {
                get {
                    val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
                    val timeRangeStart = call.request.queryParameters["time_range_start"]?.toLongOrNull()
                    val timeRangeEnd = call.request.queryParameters["time_range_end"]?.toLongOrNull()
                    call.respondProviderResult(
                        "GET /coin_records",
                        {
                            LifeUpApi.getContentProviderApi<CoinRecordsApi>()
                                .listRecords(offset, limit, timeRangeStart, timeRangeEnd)
                        },
                        onFailure = { error ->
                            logger.log(Level.WARNING, "Failed to get coin records", error)
                        },
                    ) {
                        call.respondResource(it.wrapAsResponse())
                    }
                }
            }
            route("/inventory_records") {
                get {
                    val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
                    val timeRangeStart = call.request.queryParameters["time_range_start"]?.toLongOrNull()
                    val timeRangeEnd = call.request.queryParameters["time_range_end"]?.toLongOrNull()
                    call.respondProviderResult(
                        "GET /inventory_records",
                        {
                            LifeUpApi.getContentProviderApi<InventoryRecordsApi>()
                                .listRecords(offset, limit, timeRangeStart, timeRangeEnd)
                        },
                        onFailure = { error ->
                            logger.log(Level.WARNING, "Failed to get inventory records", error)
                        },
                    ) {
                        call.respondResource(it.wrapAsResponse())
                    }
                }
            }
            route("/exp_records") {
                get {
                    val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
                    val timeRangeStart = call.request.queryParameters["time_range_start"]?.toLongOrNull()
                    val timeRangeEnd = call.request.queryParameters["time_range_end"]?.toLongOrNull()
                    call.respondProviderResult(
                        "GET /exp_records",
                        {
                            LifeUpApi.getContentProviderApi<ExpRecordsApi>()
                                .listRecords(offset, limit, timeRangeStart, timeRangeEnd)
                        },
                        onFailure = { error ->
                            logger.log(Level.WARNING, "Failed to get exp records", error)
                        },
                    ) {
                        call.respondResource(it.wrapAsResponse())
                    }
                }
            }

            route("/step_records") {
                get {
                    val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
                    val timeRangeStart = call.request.queryParameters["time_range_start"]?.toLongOrNull()
                    val timeRangeEnd = call.request.queryParameters["time_range_end"]?.toLongOrNull()
                    call.respondProviderResult(
                        "GET /step_records",
                        {
                            LifeUpApi.getContentProviderApi<StepRecordsApi>()
                                .listRecords(offset, limit, timeRangeStart, timeRangeEnd)
                        },
                        onFailure = { error ->
                            logger.log(Level.WARNING, "Failed to get step records", error)
                        },
                    ) {
                        call.respondResource(it.wrapAsResponse())
                    }
                }
            }
            route("/level_defines") {
                get {
                    call.respondProviderResult(
                        "GET /level_defines",
                        {
                            LifeUpApi.getContentProviderApi<LevelDefinesApi>().getDefines()
                        },
                        onFailure = { error ->
                            logger.log(Level.WARNING, "Failed to get level defines", error)
                        },
                    ) {
                        call.respondResource(it.wrapAsResponse())
                    }
                }
            }
            route("/statistics") {
                get {
                    val timeRangeStart = call.request.queryParameters["time_range_start"]?.toLongOrNull()
                    val timeRangeEnd = call.request.queryParameters["time_range_end"]?.toLongOrNull()
                    call.respondProviderResult(
                        "GET /statistics",
                        {
                            LifeUpApi.getContentProviderApi<StatisticsApi>()
                                .getStatistics(timeRangeStart, timeRangeEnd)
                        },
                        onFailure = { error ->
                            logger.log(Level.WARNING, "Failed to get statistics", error)
                        },
                    ) {
                        call.respondResource(it.wrapAsResponse())
                    }
                }
            }

            route("/data/export") {
                get {
                    val withMedia =
                        call.request.queryParameters["withMedia"]?.toBoolean() ?: true
                    call.respondProviderResult(
                        "GET /data/export",
                        {
                            LifeUpApi.getContentProviderApi<DataApi>().exportBackup(withMedia)
                        },
                        onFailure = { error ->
                            logger.log(Level.WARNING, "Failed to export backup", error)
                        },
                    ) { exported ->
                        if (exported == null) {
                            call.respond(
                                HttpResponse.error<String>(
                                    IllegalArgumentException(
                                        "Failed to export backup"
                                    )
                                )
                            )
                        } else {
                            call.respond(exported.toJson().wrapAsResponse())
                        }
                    }
                }
            }

            route("/data/import") {
                post {
                    val multipart = call.receiveMultipart()
                    var fileDescription = ""
                    var fileName = ""
                    var fileBytes: ByteArray? = null

                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FormItem -> {
                                if (part.name == "description") {
                                    fileDescription = part.value
                                }
                            }

                            is PartData.FileItem -> {
                                fileName = part.originalFileName ?: "backup.lfbak"
                                fileBytes = part.streamProvider().readBytes()
                            }

                            else -> {}
                        }
                        part.dispose()
                    }

                    val finalFileBytes = fileBytes
                    if (finalFileBytes != null) {
                        val tempFile = File(appCtx.externalCacheDir, "temp_$fileName")
                        tempFile.writeBytes(finalFileBytes)

                        val contentUri = tempFile.getUriForFile()
                        val intent = Intent().apply {
                            action = Intent.ACTION_VIEW
                            setDataAndType(contentUri, "application/octet-stream")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            setPackage(Val.LIFEUP_PACKAGE_NAME) // Target the LifeUp app explicitly.
                        }
                        appCtx.startActivity(intent)
                        call.respond(HttpResponse.success("File uploaded and BackupActivity launched"))
                    } else {
                        call.respond(HttpResponse.error<String>("No file received"))
                    }

                }
            }

            get("/events") {
                val after = call.request.queryParameters["after"]?.toLongOrNull() ?: 0L
                val limit = (call.request.queryParameters["limit"]?.toIntOrNull() ?: 50).coerceIn(1, EventBuffer.CAPACITY)
                call.respond(
                    HttpResponse.success(
                        EventsPage(
                            latestId = EventHub.latestId,
                            eventWs = Settings.getInstance(appCtx).enableEventWs,
                            events = EventHub.since(after, limit),
                            broadcasts = BroadcastGate.refresh(),
                        )
                    )
                )
            }

            if (Settings.getInstance(appCtx).enableEventWs) {
                webSocket("/events") {
                    val duration = Settings.getInstance(appCtx).wakeLockDuration
                    wakeLockManager.stayAwake(duration.minutes.toLong(DurationUnit.MILLISECONDS))
                    val after = call.request.queryParameters["after"]?.toLongOrNull() ?: 0L
                    val channel = Channel<CloudEvent>(Channel.UNLIMITED)
                    val replayed = mutableSetOf<Long>()
                    val listener: (CloudEvent) -> Unit = { event ->
                        if (replayed.add(event.id)) channel.trySend(event)
                    }
                    EventHub.addListener(listener)
                    try {
                        EventHub.since(after).forEach { event ->
                            if (replayed.add(event.id)) {
                                send(Frame.Text(Json.encodeToString(event)))
                            }
                        }
                        launch {
                            while (isActive) {
                                delay(30_000)
                                send(Frame.Ping(ByteArray(0)))
                                wakeLockManager.stayAwake(duration.minutes.toLong(DurationUnit.MILLISECONDS))
                            }
                        }
                        for (event in channel) {
                            send(Frame.Text(Json.encodeToString(event)))
                        }
                    } finally {
                        EventHub.removeListener(listener)
                        channel.close()
                    }
                }
            }

            route("/coin") {
                get {
                    call.respondProviderResult(
                        "GET /coin",
                        block = {
                            catchProviderFailure {
                                LifeUpApi.callApiWithContentProvider("query", "key=coin")?.toJson()
                                    ?: JsonObject(emptyMap())
                            }
                        },
                    ) {
                        call.respondResource(it.wrapAsResponse())
                    }
                }
            }
        }
    }

    private suspend fun isServerResponding(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val socket = java.net.Socket()
            socket.connect(java.net.InetSocketAddress("localhost", port.value), 500)
            socket.close()
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun Bundle.toJson(): JsonObject {
        val map = mutableMapOf<String, JsonElement>()
        val keys: Set<String> = keySet()
        for (key in keys) {
            try {
                when (val value = get(key)) {
                    is Int -> {
                        map[key] = JsonPrimitive(value)
                    }

                    is Long -> {
                        map[key] = JsonPrimitive(value)
                    }

                    is Double -> {
                        map[key] = JsonPrimitive(value)
                    }

                    is String -> {
                        map[key] = JsonPrimitive(value)
                    }

                    is Boolean -> {
                        map[key] = JsonPrimitive(value)
                    }

                    is Bundle -> {
                        map[key] = value.toJson()
                    }

                    is Array<*> -> {
                        map[key] = JsonArray(value.map { JsonPrimitive(it.toString()) })
                    }

                    else -> {
                        map[key] = JsonPrimitive(value.toString())
                    }
                }
            } catch (e: ClassCastException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return JsonObject(map)
    }
}
