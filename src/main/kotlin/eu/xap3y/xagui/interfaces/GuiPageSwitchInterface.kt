package eu.xap3y.xagui.interfaces

import eu.xap3y.xagui.models.GuiPageSwitchModel

/**
 * Interface for page switch event listeners
 *
 * @see GuiInterface
 */
interface GuiPageSwitchInterface {

    /**
     * Called when the page is switched
     * @param data The data of the switch
     */
    fun onPageSwitch(data: GuiPageSwitchModel)
}