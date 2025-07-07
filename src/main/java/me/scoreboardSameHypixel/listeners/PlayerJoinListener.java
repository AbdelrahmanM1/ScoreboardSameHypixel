package me.scoreboardSameHypixel.listeners;

import me.scoreboardSameHypixel.ScoreboardSameHypixel;
import me.scoreboardSameHypixel.systems.FriendMainclass;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final ScoreboardSameHypixel plugin;

    public PlayerJoinListener(ScoreboardSameHypixel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FriendMainclass friendMainClass = plugin.getFriendMainClass();

        // Notify friends that the player has joined
        friendMainClass.notifyFriendsOnJoin(player);
    }
}
