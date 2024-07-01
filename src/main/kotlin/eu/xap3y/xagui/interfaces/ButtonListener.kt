package eu.xap3y.xagui.interfaces

import org.bukkit.event.inventory.InventoryClickEvent

interface ButtonListener {

    fun onClick(event: InventoryClickEvent)
}