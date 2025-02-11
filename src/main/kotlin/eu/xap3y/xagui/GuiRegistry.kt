package eu.xap3y.xagui

import org.bukkit.entity.Player

class GuiRegistry<K> where K : Any {
    private val registry: MutableMap<K, MenuWrapper<*>> = HashMap()

    fun <T> register(key: K, guiMenu: VirtualMenu<T>, clazz: Class<T>) {
        registry[key] = MenuWrapper(guiMenu, clazz)
    }

    fun <T> get(key: K, clazz: Class<T>): VirtualMenu<T>? {
        val wrapper = registry[key]

        if (wrapper != null && wrapper.clazz.isAssignableFrom(clazz)) {
            return wrapper.guiMenu as VirtualMenu<T>
        }
        return null
    }

    fun invoke(key: K, player: Player) {
        val wrapper = registry[key]

        wrapper?.guiMenu?.build()?.open(player)
    }

    fun <T> invoke(key: K, player: Player, o: T, clazz: Class<T>) {
        val menu = get(key, clazz)
        menu?.build(o)?.open(player)
    }

    private class MenuWrapper<T>(val guiMenu: VirtualMenu<T>, val clazz: Class<T>)
}
