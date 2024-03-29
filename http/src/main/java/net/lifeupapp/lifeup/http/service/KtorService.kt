@file:Suppress("ObjectPropertyName")

package net.lifeupapp.lifeup.http.service

import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.response.respondOutputStream
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
import net.lifeupapp.lifeup.api.content.achievements.AchievementApi
import net.lifeupapp.lifeup.api.content.feelings.FeelingsApi
import net.lifeupapp.lifeup.api.content.info.InfoApi
import net.lifeupapp.lifeup.api.content.shop.ItemsApi
import net.lifeupapp.lifeup.api.content.skills.SkillsApi
import net.lifeupapp.lifeup.api.content.tasks.TasksApi
import net.lifeupapp.lifeup.http.base.AppScope
import net.lifeupapp.lifeup.http.base.appCtx
import net.lifeupapp.lifeup.http.utils.WakeLockManager
import net.lifeupapp.lifeup.http.utils.getIpAddressInLocalNetwork
import net.lifeupapp.lifeup.http.vo.CallUrlResult
import net.lifeupapp.lifeup.http.vo.HttpResponse
import net.lifeupapp.lifeup.http.vo.RawQueryVo
import net.lifeupapp.lifeup.http.vo.wrapAsResponse
import org.json.JSONException
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

object KtorService : LifeUpService {

    var port = 13276
        private set

    private val logger = Logger.getLogger("LifeUp-Http")

    private val _isRunning = MutableStateFlow(LifeUpService.RunningState.NOT_RUNNING)
    private val _errorMessage = MutableStateFlow<Throwable?>(null)

    override val isRunning: StateFlow<LifeUpService.RunningState>
        get() = _isRunning

    override val errorMessage: StateFlow<Throwable?>
        get() = _errorMessage

    private val mutex = Mutex()
    private val wakeLockManager = WakeLockManager("KtorService")
    private val mDnsService = MDnsService()

    init {
        AppScope.launch {
            _isRunning.collect {
                logger.info("KtorService is running: $it")
                if (LifeUpService.RunningState.RUNNING == it) {
                    ServerNotificationService.start(appCtx)
                    wakeLockManager.stayAwake(10.minutes.toLong(DurationUnit.MILLISECONDS))
                    // mdnsService.register(port.toString())
                    mDnsService.registerNsdService(port)
                } else {
                    ServerNotificationService.cancel(appCtx)
                    wakeLockManager.release()
                    mDnsService.unregisterNsdService()
                    // mdnsService.unregister()
                }
            }
        }
    }

    private var server: NettyApplicationEngine? = newService
    private var lastRequestTime = 0L

    private val RequestMoreWakeLockPlugin =
        createApplicationPlugin(name = "RequestMoreWakeLockPlugin") {
            onCall { _ ->
                if (SystemClock.elapsedRealtime() - lastRequestTime > 3.minutes.toLong(DurationUnit.MILLISECONDS)) {
                    logger.info("Requesting wake lock")
                    lastRequestTime = SystemClock.elapsedRealtime()
                    wakeLockManager.stayAwake()
                }
            }
        }

    private val newService
        get() = embeddedServer(Netty, port, watchPaths = emptyList()) {
            install(WebSockets)
            install(CallLogging)
            install(ContentNegotiation) {
                json()
            }
            install(RequestMoreWakeLockPlugin)

            routing {
                get("/") {
                    val localAddressIp = getIpAddressInLocalNetwork() ?: "UNKNOWN"
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
                                        +"http://$localAddressIp:$port/api?url=YOUR_ENCODED_API_URL"
                                    }
                                    p {
                                        +"you can send the get request to call in directly, but you need to encode the API like this: "
                                    }
                                    p {
                                        a(
                                            href = "http://$localAddressIp:$port/api?url=lifeup%3A%2F%2Fapi%2Freward%3Ftype%3Dcoin%26content%3DCall%20LifeUp%20API%20from%20HTTP%26number%3D1",
                                            target = "_blank"
                                        ) {
                                            +"http://$localAddressIp:$port/api?url=lifeup%3A%2F%2Fapi%2Freward%3Ftype%3Dcoin%26content%3DCall%20LifeUp%20API%20from%20HTTP%26number%3D1"
                                        }
                                    }
                                    div()
                                    h2 {
                                        +"POST request"
                                    }
                                    p {
                                        +"http://${localAddressIp}$port/api"
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
                            LifeUpApi.call(appCtx, url)
                        }
                        HttpResponse.success("success")
                    }.onSuccess {
                        call.respond(it)
                    }.onFailure {
                        call.respond(HttpResponse.error<String>(it))
                    }
                }

                get("/api/contentprovider") {
                    kotlin.runCatching {
                        call.request.queryParameters.getAll("url")?.map { url ->
                            logger.info("Got url: $url")
                            CallUrlResult(url, LifeUpApi.callApiWithContentProvider(url)?.toJson())
                        }?.wrapAsResponse() ?: HttpResponse.error("No url found")
                    }.onSuccess {
                        call.respond(it)
                    }.onFailure {
                        call.respond(HttpResponse.error<String>(it))
                    }
                }

                post<RawQueryVo>("/api") {
                    kotlin.runCatching {
                        logger.info("Got url: ${it.url}")
                        it.urls?.forEach {
                            logger.info("Got url: $it")
                            LifeUpApi.call(appCtx, it)
                        }
                        it.url?.let { it1 -> LifeUpApi.call(appCtx, it1) }
                        HttpResponse.success("success")
                    }.onSuccess {
                        call.respond(it)
                    }.onFailure {
                        call.respond(HttpResponse.error<String>(it))
                    }
                }

                post<RawQueryVo>("/api/contentprovider") {
                    kotlin.runCatching {
                        logger.info("Got url: ${it.url}")
                        val resultList = (
                            it.urls?.map {
                                logger.info("Got url: $it")
                                CallUrlResult(it, LifeUpApi.callApiWithContentProvider(it)?.toJson())
                            } ?: emptyList()
                            ).toMutableList()
                        it.url?.let { url ->
                            resultList.add(
                                CallUrlResult(
                                    url,
                                    LifeUpApi.callApiWithContentProvider(url)?.toJson()
                                )
                            )
                        }
                        resultList.wrapAsResponse()
                    }.onSuccess {
                        call.respond(it)
                    }.onFailure {
                        call.respond(HttpResponse.error<String>(it))
                    }
                }

                get("/files/{url}") {
                    call.parameters["url"]!!.let { url ->
                        logger.info("Got url: $url")
                        call.respondOutputStream {
                            appCtx.contentResolver.openInputStream(Uri.parse(url))?.use {
                                it.copyTo(this)
                            }
                        }
                    }
                }

                route("/tasks") {
                    // get all tasks
                    get {
                        LifeUpApi.getContentProviderApi<TasksApi>().listTasks(null).onSuccess {
                            call.respond(it.wrapAsResponse())
                        }.onFailure {
                            call.respond(HttpResponse.error<String>(it))
                        }
                    }
                    route("/{id}") {
                        // get tasks in a specific category
                        get {
                            LifeUpApi.getContentProviderApi<TasksApi>()
                                .listTasks(call.parameters["id"]?.toLongOrNull()).onSuccess {
                                    call.respond(it.wrapAsResponse())
                                }.onFailure {
                                    call.respond(HttpResponse.error<String>(it))
                                }
                        }
                    }
                }

                route("/history") {
                    get {
                        val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
                        LifeUpApi.getContentProviderApi<TasksApi>().listHistory(offset, limit)
                            .onSuccess {
                                call.respond(it.wrapAsResponse())
                            }.onFailure {
                                call.respond(HttpResponse.error<String>(it))
                            }
                    }
                }

                route("/items") {
                    get {
                        LifeUpApi.getContentProviderApi<ItemsApi>().listItems(null).onSuccess {
                            call.respond(it.wrapAsResponse())
                        }.onFailure {
                            call.respond(HttpResponse.error<String>(it))
                        }
                    }
                    route("/{id}") {
                        get {
                            val id = call.parameters["id"]?.toLongOrNull()

                            LifeUpApi.getContentProviderApi<ItemsApi>().listItems(id).onSuccess {
                                call.respond(it.wrapAsResponse())
                            }.onFailure {
                                call.respond(HttpResponse.error<String>(it))
                            }
                        }
                    }
                }

                route("/tasks_categories") {
                    get {
                        LifeUpApi.getContentProviderApi<TasksApi>().listCategories().onSuccess {
                            call.respond(it.wrapAsResponse())
                        }.onFailure {
                            call.respond(HttpResponse.error<String>(it))
                        }
                    }
                }

                route("/achievement_categories") {
                    get {
                        LifeUpApi.getContentProviderApi<AchievementApi>().listCategories()
                            .onSuccess {
                                call.respond(it.wrapAsResponse())
                            }.onFailure {
                                call.respond(HttpResponse.error<String>(it))
                            }
                    }
                }

                route("/items_categories") {
                    get {
                        LifeUpApi.getContentProviderApi<ItemsApi>().listCategories().onSuccess {
                            call.respond(it.wrapAsResponse())
                        }.onFailure {
                            call.respond(HttpResponse.error<String>(it))
                        }
                    }
                }

                get("/info") {
                    LifeUpApi.getContentProviderApi<InfoApi>().getInfo().onSuccess {
                        call.respond(it.wrapAsResponse())
                    }.onFailure {
                        call.respond(HttpResponse.error<String>(it))
                    }
                }

                route("/skills") {
                    get {
                        LifeUpApi.getContentProviderApi<SkillsApi>().listSkills().onSuccess {
                            call.respond(it.wrapAsResponse())
                        }.onFailure {
                            call.respond(HttpResponse.error<String>(it))
                        }
                    }
                }

                route("/achievements") {
                    get {
                        LifeUpApi.getContentProviderApi<AchievementApi>().listAchievements()
                            .onSuccess {
                                call.respond(it.wrapAsResponse())
                            }.onFailure {
                                call.respond(HttpResponse.error<String>(it))
                            }
                    }
                    route("/{id}") {
                        get {
                            LifeUpApi.getContentProviderApi<AchievementApi>()
                                .listAchievements(call.parameters["id"]?.toLongOrNull()).onSuccess {
                                    call.respond(it.wrapAsResponse())
                                }.onFailure {
                                    call.respond(HttpResponse.error<String>(it))
                                }
                        }
                    }
                }

                route("/feelings") {
                    get {
                        val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100

                        LifeUpApi.getContentProviderApi<FeelingsApi>().listFeelings(offset, limit)
                            .onSuccess {
                                call.respond(it.wrapAsResponse())
                            }.onFailure {
                                logger.log(Level.WARNING, "Failed to get feelings", it)
                                call.respond(HttpResponse.error<String>(it))
                            }
                    }
                }

                route("/coin") {
                    get {
                        kotlin.runCatching {
                            (LifeUpApi.callApiWithContentProvider("query", "key=coin")?.toJson())
                                ?: JsonObject(
                                    emptyMap()
                                )
                        }.onSuccess {
                            call.respond(it.wrapAsResponse())
                        }.onFailure {
                            call.respond(HttpResponse.error<String>(it))
                        }
                    }
                }
            }
        }

    override fun start() {
        AppScope.launch(Dispatchers.IO) {
            logger.info("Starting server...")
            mutex.withLock {
                if (_isRunning.value != LifeUpService.RunningState.NOT_RUNNING) {
                    logger.info("Server is already running")
                    return@launch
                }
                _errorMessage.value = null
                _isRunning.value = LifeUpService.RunningState.STARTING
                _isRunning.value = LifeUpService.RunningState.RUNNING
                if (server == null) {
                    server = newService
                }
            }
            kotlin.runCatching {
                server!!.start(wait = true)
            }.onFailure {
                _isRunning.value = LifeUpService.RunningState.NOT_RUNNING
                _errorMessage.value = it
                port++
                if (port > 65535) {
                    port = 1024
                }
            }
        }
    }

    override fun stop() {
        AppScope.launch(Dispatchers.IO) {
            mutex.withLock {
                if (_isRunning.value != LifeUpService.RunningState.RUNNING) {
                    logger.info("Server is not running")
                    return@launch
                }

                logger.info("Stopping server")
                server?.stop(1_000, 2_000)
                server = null
                _isRunning.value = LifeUpService.RunningState.NOT_RUNNING
            }
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
