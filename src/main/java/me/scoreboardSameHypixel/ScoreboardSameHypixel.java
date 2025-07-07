package me.scoreboardSameHypixel;

import fr.mrmicky.fastboard.FastBoard;
import me.clip.placeholderapi.PlaceholderAPI;
import me.scoreboardSameHypixel.commands.FriendCommand;
import me.scoreboardSameHypixel.listeners.PlayerJoinListener;
import me.scoreboardSameHypixel.listeners.PlayerQuitListener;
import me.scoreboardSameHypixel.systems.FriendMainclass;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ScoreboardSameHypixel extends JavaPlugin {

    private String[] animatedTitle;
    private int titleIndex = 0;
    private final Map<Player, FastBoard> playerBoards = new HashMap<>();
    private final Map<Player, Integer> playerGold = new HashMap<>(); // Gold storage
    private final Map<Player, Integer> playerLevels = new HashMap<>(); // Level storage
    private FriendMainclass friendMainClass;

    private List<String> disabledWorlds;
    private Map<String, String> sounds;

    @Override
    public void onEnable() {
        // Load configuration and initialize components
        saveDefaultConfig();
        loadConfigValues();

        friendMainClass = new FriendMainclass(this);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);

        // Register commands
        getCommand("friend").setExecutor(new FriendCommand(this));
        getCommand("f").setExecutor(new FriendCommand(this));
        getCommand("gold").setExecutor(this);
        getCommand("level").setExecutor(this);

        // Schedule Gold and Scoreboard updates
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (shouldDisplayScoreboard(player)) {
                        updateScoreboard(player);
                        addGold(player); // Add gold every 5 minutes
                    }
                }
                titleIndex = (titleIndex + 1) % animatedTitle.length;
            }
        }.runTaskTimer(this, 0, 6000); // Runs every 5 minutes (6000 ticks)
    }

    private void loadConfigValues() {
        // Load disabled worlds and sounds from the config
        disabledWorlds = getConfig().getStringList("disabled-worlds");
        sounds = new HashMap<>();
        sounds.put("level-up", getConfig().getString("sounds.level-up-sound", "ENTITY_PLAYER_LEVELUP"));
        sounds.put("earn-gold", getConfig().getString("sounds.earn-gold-sound", "ENTITY_PLAYER_LEVELUP"));

        // Load title animation
        animatedTitle = getConfig().getStringList("scoreboard.title-animation").toArray(new String[0]);
    }

    private boolean shouldDisplayScoreboard(Player player) {
        String worldName = player.getWorld().getName();
        return !disabledWorlds.contains(worldName);
    }

    private void addGold(Player player) {
        int gold = playerGold.getOrDefault(player, 0);
        int newGold = gold + 10; // Add 10 gold every 5 minutes
        playerGold.put(player, newGold);

        // Check if the player leveled up
        int currentLevel = playerLevels.getOrDefault(player, 0);
        if (newGold >= (currentLevel + 1) * 100) { // Level up every 100 gold
            playerLevels.put(player, currentLevel + 1);
            player.sendMessage(ChatColor.GREEN + "[LevelSystem] You leveled up to Level " + (currentLevel + 1) + "!");
            player.playSound(player.getLocation(), sounds.get("level-up"), 1.0f, 1.0f);
        }
    }

    public void updateScoreboard(Player player) {
        // Check if the player already has a FastBoard
        FastBoard board = playerBoards.computeIfAbsent(player, FastBoard::new);

        // Update title with animation
        board.updateTitle(ChatColor.translateAlternateColorCodes('&', animatedTitle[titleIndex]));

        // Update content
        List<String> lines = new ArrayList<>();
        String rank = getPlayerPrefix(player);
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        String friendsCount = String.valueOf(getOnlineFriends(player).size());
        int gold = playerGold.getOrDefault(player, 0);
        int level = playerLevels.getOrDefault(player, 0);

        // Populate scoreboard lines
        lines.add(ChatColor.GRAY + LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) + ChatColor.DARK_GRAY + " L1ML");
        lines.add("");
        lines.add(ChatColor.BOLD + "" + ChatColor.WHITE + "Rank: " + rank);
        lines.add(ChatColor.BOLD + "" + ChatColor.WHITE + "Lobby: " + ChatColor.GREEN + getLobbyNumber(player));
        lines.add(ChatColor.BOLD + "" + ChatColor.WHITE + "Online: " + ChatColor.GREEN + onlinePlayers); // Local online count
        lines.add("");
        lines.add(ChatColor.BOLD + "" + ChatColor.WHITE + "Gold: " + ChatColor.YELLOW + gold);
        lines.add(ChatColor.BOLD + "" + ChatColor.WHITE + "Level: " + ChatColor.AQUA + level);
        lines.add(ChatColor.YELLOW + "www.hypixel.net");

        // Apply updates
        board.updateLines(lines);
    }

    private List<String> getOnlineFriends(Player player) {
        Set<Player> friends = friendMainClass.getPlayerFriends(player); // Assume this returns Set<Player>
        if (friends == null) {
            return Collections.emptyList();
        }

        List<String> onlineFriends = new ArrayList<>();
        for (Player friend : friends) {
            if (friend.isOnline()) {
                onlineFriends.add(friend.getName());
            }
        }
        return onlineFriends;
    }

    private String getLobbyNumber(Player player) {
        return String.valueOf(player.getWorld().getName().hashCode());
    }

    public String getPlayerPrefix(Player player) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                String prefix = PlaceholderAPI.setPlaceholders(player, "%luckperms_prefix%");
                return prefix != null ? ChatColor.translateAlternateColorCodes('&', prefix) : "Default";
            } catch (Exception e) {
                getLogger().warning("Error while fetching placeholders: " + e.getMessage());
            }
        }

        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                return user.getCachedData()
                        .getMetaData()
                        .getPrefix() != null ? ChatColor.translateAlternateColorCodes('&', user.getCachedData().getMetaData().getPrefix()) : "Default";
            }
        } catch (Exception e) {
            getLogger().warning("LuckPerms API error: " + e.getMessage());
        }

        return "Default";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("gold")) {
            int gold = playerGold.getOrDefault(player, 0);
            player.sendMessage(ChatColor.GOLD + "[GoldSystem] You have " + gold + " Gold.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("level")) {
            int level = playerLevels.getOrDefault(player, 0);
            player.sendMessage(ChatColor.AQUA + "[LevelSystem] You are Level " + level + ".");
            return true;
        }

        return false;
    }
}
