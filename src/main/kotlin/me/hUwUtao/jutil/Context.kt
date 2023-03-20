package me.hUwUtao.jutil

import com.velocitypowered.api.proxy.ProxyServer
import me.hUwUtao.dynport.Plugin
import org.slf4j.Logger
import java.io.File
import java.nio.file.Path

class Context(val plugin: Plugin, val server: ProxyServer, val logger: Logger, val path: Path) {
    fun resolve(file: String): File {
        val file = File(path.resolve(file).toString())
        if(file.isDirectory)
            file.deleteRecursively()
        if (!file.exists())
            file.writeText("")
        return file
    }
}