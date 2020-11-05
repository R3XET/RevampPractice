package eu.revamp.practice.util.misc;

import eu.revamp.spigot.utils.chat.color.CC;
import org.bukkit.Bukkit;

public class Logger {

    public static void success(String message) {
        Bukkit.getConsoleSender().sendMessage(CC.translate("&7(&bRevampPractice&7) &8» &a" + message));
    }

    public static void error(String message) {
        Bukkit.getConsoleSender().sendMessage(CC.translate("&7(&bRevampPractice&7) &8» &c" + message));
    }
}
