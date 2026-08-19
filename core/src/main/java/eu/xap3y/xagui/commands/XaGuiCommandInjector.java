package eu.xap3y.xagui.commands;

import eu.xap3y.xagui.XaGui;
import eu.xap3y.xagui.XaGuiPlugin;
import org.bukkit.command.PluginCommand;

public class XaGuiCommandInjector {

    public static void injectCommand() {

        if (!XaGui.isPaper()) {
            PluginCommand command = XaGuiPlugin.getXaGui().getPlugin().getCommand("xagui");
            if (command != null) {
                command.setExecutor(new XaGuiCommand());
            }
        } else {
            try {
                Class.forName("eu.xap3y.xagui.paper.XaGuiCommandPaper").getMethod("injectPaper").invoke(null);
            } catch (Exception e) {}
        }


        XaGuiPlugin.getXaGui().injectPermissions();
    }
}
