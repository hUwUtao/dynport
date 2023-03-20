package me.hUwUtao.vourte

import com.github.luben.zstd.ZstdInputStream
import com.github.luben.zstd.ZstdOutputStream
import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.api.proxy.server.ServerInfo
import com.velocitypowered.api.scheduler.ScheduledTask
import me.hUwUtao.jutil.Context
import java.io.*
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.TimeUnit

class Router(val context: Context) {
    private lateinit var repository: RouterRepository
    private var ckptQuartz: ScheduledTask? = null

    companion object {
        fun b64e(string: String) = Base64.getEncoder().encodeToString(string.toByteArray()).toString()
        fun b64d(b64: String) = Base64.getDecoder().decode(b64).toString()
        fun routePairlize(route: ServerInfo): HostEntry {
            return Pair(b64d(route.name), Pair(route.address.hostString, route.address.port))
        }
        fun pairRoutify(pair: HostEntry): ServerInfo {
            return ServerInfo(b64e(pair.first), InetSocketAddress(pair.second.first, pair.second.second))
        }
    }
    private class RouterRepository(private val router: Router, private val db: File) {
        fun commit() {
            try {
                val fs = FileOutputStream(db)
                val zs = ZstdOutputStream(fs)
                val os = ObjectOutputStream(zs)
                os.writeObject(router.context.server.allServers.map {
                    routePairlize(it.serverInfo)
                })
                os.close()
            } catch (e: IOException) {
                router.context.logger.error("While commit DB", e)
            } finally {
                router.context.logger.info("DB commited")
            }
        }

        fun existName(host: String): RegisteredServer? {
            val b64ho = b64e(host)
            val rr = router.context.server.getServer(b64ho)
            return rr.orElse(null)
        }

        private fun pull() {
            if (db.exists()) {
                try {
                    val fs = FileInputStream(db)
                    val zs = ZstdInputStream(fs)
                    val os = ObjectInputStream(zs)
                    try {
                        (os.readObject() as Collection<HostEntry>).forEach {
                            val route = pairRoutify(it)
                            router.context.server.registerServer(
                                route
                            )
                        }
                    } catch (e: TypeCastException) {
                        router.context.logger.error("While pull DB (possibly data malformed, delete the db file for god sake T-T)", e)
                    }
                } catch (e: IOException) {
                    router.context.logger.error("While pull DB", e)
                } finally {
                    router.context.logger.info("DB pulled")
                }
            } else {
                if (db.isDirectory)
                    db.deleteRecursively()
                db.writeText("")
                commit()
            }
        }

        init {
            pull()
        }
    }

    /*
     * Load DB and create a quartz for checkpoint
     */
    fun load() {
        repository = RouterRepository(this, context.resolve("hosts._db"))
        ckptQuartz = context.server.scheduler.buildTask(
            context.plugin
        ) {
            repository.commit()
        }.repeat(1L, TimeUnit.MINUTES).schedule()
    }

    /*
     * Gently save and stop all quartz
     */
    fun unload() {
        ckptQuartz?.cancel()
        repository.commit()
    }

    /*
     * Query static information. Not routing yet
     */
    fun query(host: String): RegisteredServer? = repository.existName(host)
    fun add(k: String, v: String): Boolean {
        val ln = b64e(k)
        val spl = v.split(":")
        if(spl.size != 2)
            return false
        remove(k)
        context.server.registerServer(ServerInfo(ln, InetSocketAddress(spl[0], spl[1].toInt().or(25565))))
        return true
    }

    fun remove(k: String): Boolean {
        val ln = b64e(k)
        val cs = context.server.getServer(ln)
        if(cs.isPresent)context.server.unregisterServer(cs.get().serverInfo)
        return true
    }
}

//typealias HostnameKeyed = Collection<ServerInfo>
typealias HostEntry = Pair<String, Pair<String, Int>>