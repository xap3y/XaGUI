package eu.xap3y.xagui

import eu.xap3y.xagui.interfaces.GuiInterface
import eu.xap3y.xagui.interfaces.VirtualMenuInterface
import org.bukkit.entity.Player

abstract class VirtualMenu<T> : VirtualMenuInterface<T> {
    val gui: GuiInterface

    constructor(name: String, rows: Int, instance: XaGui) {
        this.gui = instance.createMenu(name, rows)
    }

    constructor(gui: GuiInterface) {
        this.gui = gui
    }

    fun open(player: Player) {
        gui.open(player)
    }

    fun open(page: Int, player: Player) {
        gui.open(page, player)
    }

    fun build(): GuiInterface {
        return gui
    }

    abstract override fun build(o: T): GuiInterface?

    fun open(player: Player, o: T) {
        val gui = build(o)
        gui?.open(player)
    }
}
