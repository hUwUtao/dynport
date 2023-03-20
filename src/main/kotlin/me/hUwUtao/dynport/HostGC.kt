package me.hUwUtao.dynport

import me.hUwUtao.jutil.Context
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

class HostGC(context: Context) {
    companion object {
        fun snowflake() = System.currentTimeMillis() / 1000L
    }

    class Ticket(val host: String) {
        val death = snowflake() + 5
    }

    val repository: HashMap<InetSocketAddress, Ticket?> = HashMap()
    fun gc() {
        repository.filter { snowflake() > it.value?.death ?: 0L }
    }

    init {
        context.server.scheduler.buildTask(context.plugin) { gc() }.repeat(10L, TimeUnit.SECONDS).schedule()
    }
}