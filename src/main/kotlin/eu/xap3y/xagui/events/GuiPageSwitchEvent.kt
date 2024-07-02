package eu.xap3y.xagui.events

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Event for page switch
 * @param player The player who switched the page
 * @param newPage The new page number
 * @param oldPage The old page number
 */
class GuiPageSwitchEvent(val player: Player, val newPage: Int, val oldPage: Int): Event() {

    companion object {
        private val handlerList = HandlerList()
        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

}