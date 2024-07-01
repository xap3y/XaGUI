public class Example {

    public static void openGui(Player player, XaGui instance) {

        GuiMenu menu = instance.createMenu("&a&lExample 1", 3);

        menu.fillBorder(3);

        GuiButton mainItem = new GuiButton(new ItemStack(Material.PAPER, 64)).setName("&a&lMain Item");

        // When player click on the main item, it will send a message to the player and update the main item to a diamond
        mainItem.setListener((event) -> {
            player.sendMessage("You clicked on the main item!");

            if (event.isShiftClick() && event.isLeftClick())
                menu.updateSlot(4, new ItemStack(Material.DIAMOND, 1));
        });

        Integer[] unlockedSlots = {10, 11, 12, 13, 14, 15, 16};

        // Unlock all slots
        for (Integer unlockedSlot : unlockedSlots) {
            menu.setSlot(unlockedSlot, new ItemStack(Material.DIRT, unlockedSlot));
            menu.unlockButton(unlockedSlot);
        }

        menu.setSlot(4, mainItem);

        menu.open(player);
    }
}
