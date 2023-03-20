package me.hUwUtao.dynport

import com.google.inject.Inject
import com.velocitypowered.api.command.CommandMeta
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.ConnectionHandshakeEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyReloadEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.ProxyServer
import me.hUwUtao.jutil.Context
import me.hUwUtao.vourte.Router
import org.slf4j.Logger
import java.nio.file.Path

@Plugin(
    id = "dynport",
    name = "DynPort",
    version = "0.1.0",
    url = "https://hUwUtao.me",
    description = "Reroute",
    authors = ["hUwUtao"]
)
class Plugin @Inject constructor(
    private val server: ProxyServer,
    private val logger: Logger,
    @DataDirectory private val path: Path
) {
    private lateinit var message: Config.Messages
    private lateinit var config: Config
    private val context = Context(this, server, logger, path)
    val router: Router = Router(context)
    private fun load() {
        config = Config(context)
        message = Config.Messages(config)
        logger.info("router loaded")
    }

    init {
        logger.info("started")
        load()
    }

    private var hosts: HostGC? = null

    private class Command (private val context: Context): SimpleCommand {
        override fun execute(invocation: SimpleCommand.Invocation?) {
            val args = invocation!!.arguments()
            if(args[0] != null) when(args[0]) {
                "add" -> if(args.size == 3) {
                    context.plugin.router.add(args[1], args[2])
                }
                "rem" -> if(args.size == 2) context.plugin.router.remove(args[1])
            }
        }
        override fun hasPermission(invocation: SimpleCommand.Invocation): Boolean {
            return invocation.source() is ConsoleCommandSource
        }
    }
    @Subscribe
    private fun init(event: ProxyInitializeEvent) {
        hosts = HostGC(context)
        router.load()

        val meta = server.commandManager.metaBuilder("dynport").plugin(this).build()
        val command = Command(context)
        server.commandManager.register(meta, command)
    }

    @Subscribe
    private fun down(event: ProxyShutdownEvent) {
        router.unload()
    }

    @Subscribe
    private fun reload(event: ProxyReloadEvent) {
        hosts = null
        router.unload()
    }

//    @Subscribe
//    private fun command(event: CommandExecuteEvent) {
//        val sl = event.command.split(" ")
//        if (event.commandSource is ConsoleCommandSource && //
//            sl[0] == "dynport" && sl.size > 3
//        )
//            when (sl[1]) {
//                "mk" -> if(sl.size > 4) router.add(sl[2], sl[3])
//                "rm" -> router.remove(sl[2])
//            }
//    }

    @Subscribe
    private fun handshake(event: ConnectionHandshakeEvent) {
        val vh = event.connection.virtualHost
        val ra = event.connection.remoteAddress
        if (vh != null) {
            val hn = vh.get().hostName
            hosts!!.repository[ra] = HostGC.Ticket(hn)
            logger.info(String.format("%s -> %s", ra, hn))
        }
    }

    @Subscribe
    fun login(event: LoginEvent) {
        val ra = event.player.remoteAddress
        val ho = hosts!!.repository[ra]
        if (ho != null) {
            val destination = router.query(ho.host)
            logger.info(String.format("%s -> %s", ra, destination?.serverInfo?.address.toString()))
            if (destination == null) {
                event.player.disconnect(message.disconnect)
            } else {
                event.player.createConnectionRequest(destination)
            }
        }
    }
}