package me.scoreboardSameHypixel.commands;

import me.scoreboardSameHypixel.ScoreboardSameHypixel;
import me.scoreboardSameHypixel.systems.FriendMainclass;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FriendCommand implements CommandExecutor {

    private final ScoreboardSameHypixel plugin;
    private final FriendMainclass friendMainClass;

    public FriendCommand(ScoreboardSameHypixel plugin) {
        this.plugin = plugin;
        this.friendMainClass = plugin.getFriendMainClass(); // Access FriendMainclass via ScoreboardSameHypixel
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return false;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /friend <add|accept|decline|list> <player>");
            return false;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "add":
                handleAddCommand(player, args);
                break;

            case "accept":
                handleAcceptCommand(player, args);
                break;

            case "decline":
                handleDeclineCommand(player, args);
                break;

            case "list":
                handleListCommand(player);
                break;

            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /friend <add|accept|decline|list> <player>");
                break;
        }

        return true;
    }

    private void handleAddCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /friend add <player>");
            return;
        }

        // Check if the player is trying to send a friend request to themselves
        if (args[1].equalsIgnoreCase(player.getName())) {
            player.sendMessage(ChatColor.RED + "You cannot send a friend request to yourself.");
            return;
        }

        Player targetAdd = plugin.getServer().getPlayer(args[1]);
        if (targetAdd == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }
        friendMainClass.addFriendRequest(player, targetAdd);
        player.sendMessage(ChatColor.GREEN + "Friend request sent to " + targetAdd.getName());
    }

    private void handleAcceptCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /friend accept <player>");
            return;
        }
        Player targetAccept = plugin.getServer().getPlayer(args[1]);
        if (targetAccept == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }
        friendMainClass.acceptFriendRequest(player, targetAccept);
        player.sendMessage(ChatColor.GREEN + "You are now friends with " + targetAccept.getName());
    }

    private void handleDeclineCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /friend decline <player>");
            return;
        }
        Player targetDecline = plugin.getServer().getPlayer(args[1]);
        if (targetDecline == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }
        friendMainClass.declineFriendRequest(player, targetDecline);
        player.sendMessage(ChatColor.YELLOW + "You declined the friend request from " + targetDecline.getName());
    }

    private void handleListCommand(Player player) {
        player.sendMessage(ChatColor.GREEN + "Your friends:");
        friendMainClass.getPlayerFriends(player).forEach(friend ->
                player.sendMessage(ChatColor.YELLOW + friend.getName())
        );
    }
}
