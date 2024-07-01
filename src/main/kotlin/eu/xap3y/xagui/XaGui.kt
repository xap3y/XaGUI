package eu.xap3y.xagui

import eu.xap3y.xagui.listeners.GuiMenuListener
import org.bukkit.plugin.java.JavaPlugin


/**
 * The main class of the library
 * @param plugin The plugin instance
 */
class XaGui(private val plugin: JavaPlugin) {

    init {
        plugin.server.pluginManager.registerEvents(GuiMenuListener(plugin), plugin)
    }


    /**
     * Create a new menu
     * @param name The name of the menu
     * @param rows The number of rows in the menu
     * @return The created menu
     * @see GuiMenu
     */
    fun createMenu(name: String, rows: Int): GuiMenu {
        return GuiMenu(plugin, name, rows)
    }

}
