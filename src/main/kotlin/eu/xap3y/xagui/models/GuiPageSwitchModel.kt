package eu.xap3y.xagui.models

import org.bukkit.entity.Player

/**
 * Model for page switch event
 */
data class GuiPageSwitchModel(
    val player: Player,
    val page: Int,
    val oldPage: Int
)
