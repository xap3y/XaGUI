package eu.xap3y.xagui

import eu.xap3y.xagui.listeners.GuiMenuListener
import org.bukkit.plugin.java.JavaPlugin


class XaGui(private val plugin: JavaPlugin) {

    init {
        plugin.server.pluginManager.registerEvents(GuiMenuListener(plugin), plugin)
    }

    fun createMenu(name: String, rows: Int): GuiMenu {
        return GuiMenu(plugin, name, rows)
    }

}
