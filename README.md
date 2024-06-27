# Minecraft GUI library

### Installation with jitpack

> [!NOTE]\
> Replace the `LATEST-COMMIT-HASH` with the latest commit hash

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.xap3y</groupId>
    <artifactId>xagui</artifactId>
    <version>LATEST-COMMIT-HASH</version>
    <scope>compile</scope>
</dependency>
```

### Example

add this to your onEnable method

```kotlin
val xaGui = XaGui(this)
```

### Create a new GUI

```kotlin

// GUI with 6 rows
val gui = xaGui.createMenu("&7GUI TITLE", 6)

// Fill borders with glass panes
gui.fillBorder(6)

// Set slot 4 to barrier, and when player click it, it will close the GUI
gui.setSlot(4, GuiButton(ItemStack(Material.BARRIER)).setListener {
    p.closeInventory()
})

// This will fill the slots with OAK_BUTTON and when player click it, it will update the slot to STONE
val slots = setOf(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43)
gui.fillSlots(slots, GuiButton(ItemStack(Material.OAK_BUTTON)).setListener {
    gui.updateSlot(it.slot, ItemStack(Material.STONE))
})

gui.setOnClose {
    // Do something when the player closes the GUI
    it.player.sendMessage("You closed the GUI")
}

gui.setOnOpen {
    // Do something when the player opens the GUI
    it.player.sendMessage("You opened the GUI")
}

// Opens the GUI. Synchronous
gui.open(player)
```