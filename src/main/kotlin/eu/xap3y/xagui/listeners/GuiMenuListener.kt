package eu.xap3y.xagui.listeners

import eu.xap3y.xagui.GuiMenu
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

/**
 * The listener for the menus
 * @param plugin The plugin instance
 */
class GuiMenuListener(private val plugin: JavaPlugin): Listener {

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        if (e.clickedInventory == null || e.clickedInventory?.holder !is GuiMenu) return

        val clickedInventory = (e.clickedInventory?.holder ?: return) as GuiMenu

        val owner = clickedInventory.getOwner()
        if (!Objects.equals(owner, plugin)) return

        if (clickedInventory.unlockedSlots[clickedInventory.getCurrentPage()]?.contains(e.slot)?.not() == true) {
            e.result = Event.Result.DENY
        }

        clickedInventory.onClickAction?.onClick(e)

        val button = clickedInventory.getSlot(e.slot) ?: return

        button.listener?.onClick(e)

    }

    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {
        if (e.inventory.holder !is GuiMenu) return

        val clickedInventory = (e.inventory.holder ?: return) as GuiMenu

        val owner = clickedInventory.getOwner()
        if (!Objects.equals(owner, plugin)) return

        clickedInventory.onCloseAction?.onClose(e)
    }

    @EventHandler
    fun onInventoryOpen(e: InventoryOpenEvent) {
        if (e.inventory.holder !is GuiMenu) return

        val clickedInventory = (e.inventory.holder ?: return) as GuiMenu

        val owner = clickedInventory.getOwner()
        if (!Objects.equals(owner, plugin)) return

        clickedInventory.onOpenAction?.onOpen(e)
    }
}