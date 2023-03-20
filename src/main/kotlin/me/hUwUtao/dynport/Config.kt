package me.hUwUtao.dynport

import me.hUwUtao.jutil.Context
import java.io.File

class Config(context: Context) {
    private val disconnectMessageFile = context.resolve("disconnect.txt")

    class Messages(config: Config) {
        var disconnect = config.getMessage(MessagesType.Disconnect)
    }

    companion object {
        enum class MessagesType {
            Disconnect
        }
        private fun msgFile(file: File) = file.readText(Charsets.UTF_8).replace("\\r\\n", "\n")
    }

    private fun str(file: File) = Parser.parse(msgFile(file))

    fun getMessage(message: MessagesType) = when (message) {
        MessagesType.Disconnect -> str(disconnectMessageFile)
    }

}