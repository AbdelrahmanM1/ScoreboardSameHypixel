package me.scoreboardSameHypixel.systems;

import me.scoreboardSameHypixel.ScoreboardSameHypixel;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FriendMainclass {
    private final ScoreboardSameHypixel plugin;
    private final Map<Player, Set<Player>> playerFriends = new HashMap<>();
    private final Map<Player, Set<Player>> pendingFriendRequests = new HashMap<>();

    public FriendMainclass(ScoreboardSameHypixel plugin) {
        this.plugin = plugin;
    }

    // Adding a friend request
    public void addFriendRequest(Player sender, Player receiver) {
        if (sender.equals(receiver)) {
            sender.sendMessage(ChatColor.RED + "You cannot send a friend request to yourself.");
            return;
        }

        Set<Player> requestsForReceiver = pendingFriendRequests.computeIfAbsent(receiver, k -> new HashSet<>());
        if (requestsForReceiver.contains(sender)) {
            sender.sendMessage(ChatColor.RED + "You have already sent a friend request to this player.");
        } else {
            String rank = plugin.getPlayerPrefix(sender);
            requestsForReceiver.add(sender);

            receiver.sendMessage(ChatColor.GREEN + rank + " " + sender.getName() + " has sent you a friend request!");

            TextComponent acceptButton = new TextComponent("[ACCEPT]");
            acceptButton.setColor(net.md_5.bungee.api.ChatColor.GREEN);
            acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept " + sender.getName()));

            TextComponent declineButton = new TextComponent("[DECLINE]");
            declineButton.setColor(net.md_5.bungee.api.ChatColor.RED);
            declineButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend decline " + sender.getName()));

            receiver.spigot().sendMessage(new ComponentBuilder("Click to respond: ")
                    .append(acceptButton)
                    .append(" ")
                    .append(declineButton)
                    .create());
        }
    }

    // Accept a friend request
    public void acceptFriendRequest(Player player, Player sender) {
        Set<Player> requestsForPlayer = pendingFriendRequests.get(player);
        if (requestsForPlayer == null || !requestsForPlayer.contains(sender)) {
            player.sendMessage(ChatColor.RED + "No friend request from this player.");
            return;
        }

        requestsForPlayer.remove(sender);
        playerFriends.computeIfAbsent(player, k -> new HashSet<>()).add(sender);
        playerFriends.computeIfAbsent(sender, k -> new HashSet<>()).add(player);

        player.sendMessage(ChatColor.GREEN + "You are now friends with " + sender.getName());
        sender.sendMessage(ChatColor.GREEN + "You are now friends with " + player.getName());
    }

    // Decline a friend request
    public void declineFriendRequest(Player player, Player sender) {
        Set<Player> requestsForPlayer = pendingFriendRequests.get(player);
        if (requestsForPlayer == null || !requestsForPlayer.contains(sender)) {
            player.sendMessage(ChatColor.RED + "No friend request from this player.");
            return;
        }

        requestsForPlayer.remove(sender);
        player.sendMessage(ChatColor.RED + "You have declined the friend request from " + sender.getName());
    }

    // Get the player's friends
    public Set<Player> getPlayerFriends(Player player) {
        return playerFriends.getOrDefault(player, new HashSet<>());
    }

    // Notify friends when a player joins
    public void notifyFriendsOnJoin(Player player) {
        Set<Player> friends = getPlayerFriends(player);
        if (!friends.isEmpty()) {  // Checking for non-empty friend list
            for (Player friend : friends) {
                String rank = plugin.getPlayerPrefix(player);
                friend.sendMessage(ChatColor.GREEN + "Friend > " + ChatColor.WHITE + player.getName() + ChatColor.YELLOW + " joined!");
            }
        }
    }

    // Notify friends when a player leaves
    public void notifyFriendsOnLeave(Player player) {
        Set<Player> friends = getPlayerFriends(player);
        if (!friends.isEmpty()) {  // Checking for non-empty friend list
            for (Player friend : friends) {
                String rank = plugin.getPlayerPrefix(player);
                friend.sendMessage(ChatColor.GREEN + "Friend > " + ChatColor.WHITE + player.getName() + ChatColor.RED + " left!");
            }
        }
    }
}
