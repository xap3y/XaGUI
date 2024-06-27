package eu.xap3y.xagui.listeners

import eu.xap3y.xagui.GuiMenu
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class GuiMenuListener(private val plugin: JavaPlugin): Listener {

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        if (e.clickedInventory == null || e.clickedInventory?.holder !is GuiMenu) return

        val clickedInventory = (e.clickedInventory?.holder ?: return) as GuiMenu

        val owner = clickedInventory.getOwner()
        if (!Objects.equals(owner, plugin)) return

        if (!clickedInventory.unlockedSlots.contains(e.slot)) {
            e.result = Event.Result.DENY
        }

        val button = clickedInventory.getSlot(e.slot) ?: return

        button.listener?.onClick(e)
    }

    @EventHandler
    fun onInventoryClose(e: InventoryClickEvent) {
        if (e.clickedInventory == null || e.clickedInventory?.holder !is GuiMenu) return

        val clickedInventory = (e.clickedInventory?.holder ?: return) as GuiMenu

        val owner = clickedInventory.getOwner()
        if (!Objects.equals(owner, plugin)) return

        clickedInventory.onCloseAction?.invoke()
    }
}