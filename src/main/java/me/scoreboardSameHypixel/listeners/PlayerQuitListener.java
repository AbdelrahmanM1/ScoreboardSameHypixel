package me.scoreboardSameHypixel.listeners;

import me.scoreboardSameHypixel.ScoreboardSameHypixel;
import me.scoreboardSameHypixel.systems.FriendMainclass;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final ScoreboardSameHypixel plugin;

    public PlayerQuitListener(ScoreboardSameHypixel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        FriendMainclass friendMainClass = plugin.getFriendMainClass();

        // Notify friends that the player has left
        friendMainClass.notifyFriendsOnLeave(player);
    }
}
