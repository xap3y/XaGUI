package eu.xap3y.xagui.interfaces

interface VirtualMenuInterface<T> {
    fun build(o: T): GuiInterface?
}
