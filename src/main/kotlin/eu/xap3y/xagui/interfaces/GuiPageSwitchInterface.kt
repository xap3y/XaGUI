package eu.xap3y.xagui.interfaces

import eu.xap3y.xagui.events.GuiPageSwitchEvent

/**
 * Interface for page switch event listeners
 * @see GuiInterface
 */
interface GuiPageSwitchInterface {

    /**
     * Called when the page is switched
     * @param event The event of the page switch
     */
    fun onPageSwitch(event: GuiPageSwitchEvent)
}