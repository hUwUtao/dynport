package me.hUwUtao.dynport

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver


class Parser {
    companion object {
        fun parse(text: String) = Parser().parse(text)
    }

    private val mm = MiniMessage.builder()
        .tags(
            TagResolver.standard()
        )
        .build()

    fun parse(text: String) = mm.deserialize(text)

}