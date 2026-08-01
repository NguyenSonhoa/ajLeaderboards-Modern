package us.ajg0702.leaderboards.commands.main.subcommands;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.CommentedConfigurationNode;
import us.ajg0702.commands.CommandSender;
import us.ajg0702.commands.SubCommand;
import us.ajg0702.leaderboards.LeaderboardPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static us.ajg0702.leaderboards.LeaderboardPlugin.message;

public final class Hologram extends SubCommand {
    private final LeaderboardPlugin plugin;

    public Hologram(LeaderboardPlugin plugin) {
        super("hologram", Collections.singletonList("holo"), "ajleaderboards.use",
                "Manage native leaderboard holograms");
        this.plugin = plugin;
    }

    @Override
    public List<String> autoComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return filterCompletion(Arrays.asList("create", "list", "movehere", "remove", "respawn"), args[0]);
        if (args.length == 2 && !args[0].equalsIgnoreCase("create")) return filterCompletion(plugin.getResetBar().getIds(), args[1]);
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args, String label) {
        if (args.length == 0) {
            sender.sendMessage(message("<yellow>Usage: /" + label + " hologram <create|list|movehere|remove|respawn> [id] [board]"));
            return;
        }
        String action = args[0].toLowerCase(Locale.ROOT);
        if (action.equals("list")) {
            sender.sendMessage(message("<gold>Holograms: <yellow>" + String.join(", ", plugin.getResetBar().getIds())));
            return;
        }
        String id = args.length > 1 ? args[1].toLowerCase(Locale.ROOT) : "main";

        if (action.equals("respawn")) {
            boolean success = args.length > 1 ? plugin.getResetBar().respawn(id) : restartAll();
            sender.sendMessage(message(success ? "<green>Leaderboard hologram respawned." : "<red>Hologram not found or invalid."));
            return;
        }
        if (action.equals("remove")) {
            CommentedConfigurationNode node = plugin.getAConfig().getNode().node("leaderboard-holograms", id);
            if (node.virtual()) {
                sender.sendMessage(message("<red>Hologram not found."));
                return;
            }
            try {
                node.set(null);
                saveAndReload();
                plugin.getResetBar().restart();
                sender.sendMessage(message("<green>Leaderboard hologram removed."));
            } catch (ConfigurateException e) {
                fail(sender, e);
            }
            return;
        }
        if (!action.equals("create") && !action.equals("movehere")) {
            sender.sendMessage(message("<red>Unknown action."));
            return;
        }
        if (!sender.isPlayer()) {
            sender.sendMessage(message("<red>You must use this action in game."));
            return;
        }
        if (action.equals("create") && args.length < 3) {
            sender.sendMessage(message("<yellow>Usage: /" + label + " hologram create <id> <board>"));
            return;
        }

        try {
            plugin.getAConfig().reload();
        } catch (ConfigurateException e) {
            fail(sender, e);
            return;
        }
        CommentedConfigurationNode node = plugin.getAConfig().getNode().node("leaderboard-holograms", id);
        if (action.equals("create") && !node.virtual()) {
            sender.sendMessage(message("<red>A hologram with that ID already exists."));
            return;
        }
        if (action.equals("movehere") && node.virtual()) {
            sender.sendMessage(message("<red>Hologram not found."));
            return;
        }

        try {
            if (action.equals("create")) defaults(node, args[2]);
            Location target = ((Player) sender.getHandle()).getLocation();
            setLocation(node.node("location"), target);
            saveAndReload();
            CommentedConfigurationNode saved = plugin.getAConfig().getNode()
                    .node("leaderboard-holograms", id, "location");
            if (!sameLocation(saved, target)) {
                throw new ConfigurateException("Saved hologram location did not match the target location");
            }
            plugin.getResetBar().respawn(id);
            sender.sendMessage(message("<green>Leaderboard hologram " + (action.equals("create") ? "created." : "moved here.")));
        } catch (ConfigurateException e) {
            fail(sender, e);
        }
    }

    private boolean restartAll() {
        plugin.getResetBar().restart();
        return true;
    }

    private void defaults(CommentedConfigurationNode node, String board) throws ConfigurateException {
        node.node("enabled").set(true);
        node.node("duration-seconds").set(10);
        node.node("top-size").set(10);
        node.node("scale").set(1.8);
        node.node("line-spacing").set(0.3);
        node.node("billboard").set("FIXED");
        node.node("text-shadow").set(true);
        node.node("see-through").set(false);
        node.node("format", "title").set("&a&l{title}");
        node.node("format", "divider").set("&8━━━━━━━━━━━━━━━━━━━━");
        node.node("format", "line").set("{rank_color}{position}. &f{name} &a{value}{suffix}");
        node.node("format", "personal").set("&8Bạn: &e#{player_position} &7- &f{player_value}{suffix}");
        node.node("animation", "duration-ticks").set(10);
        node.node("animation", "line-delay-ticks").set(1);
        node.node("animation", "slide-distance").set(1.5);
        node.node("animation", "scale-duration-ticks").set(10);
        node.node("animation", "transition-scale").set(0.65);
        node.node("bar", "material").set("LIME_CONCRETE");
        node.node("bar", "background-material").set("LIGHT_GRAY_CONCRETE");
        node.node("bar", "type").set("ITEM");
        node.node("bar", "text").set("&a&m                    ");
        node.node("bar", "background-text").set("&8&m                    ");
        node.node("bar", "background-offset-z").set(0.1);
        node.node("bar", "text-height").set(1.0);
        node.node("bar", "max-width").set(8.0);
        node.node("bar", "height").set(0.13);
        node.node("bar", "depth").set(0.005);
        node.node("bar", "auto-position").set(true);
        node.node("bar", "gap").set(0.15);
        node.node("bar", "offset-y").set(-0.38);
        node.node("boards").setList(String.class, Collections.singletonList(board + "|TOP " + board.toUpperCase(Locale.ROOT) + "|"));
    }

    private void setLocation(CommentedConfigurationNode node, Location location) throws ConfigurateException {
        node.node("world").set(location.getWorld().getName());
        node.node("x").set(location.getX());
        node.node("y").set(location.getY());
        node.node("z").set(location.getZ());
        node.node("yaw").set(location.getYaw());
        node.node("pitch").set(location.getPitch());
    }

    private boolean sameLocation(CommentedConfigurationNode node, Location location) {
        return location.getWorld().getName().equals(node.node("world").getString())
                && Double.compare(location.getX(), node.node("x").getDouble()) == 0
                && Double.compare(location.getY(), node.node("y").getDouble()) == 0
                && Double.compare(location.getZ(), node.node("z").getDouble()) == 0
                && Float.compare(location.getYaw(), node.node("yaw").getFloat()) == 0
                && Float.compare(location.getPitch(), node.node("pitch").getFloat()) == 0;
    }

    private void saveAndReload() throws ConfigurateException {
        plugin.getAConfig().getConfigFile().getLoader().save(plugin.getAConfig().getNode());
        plugin.getAConfig().reload();
    }

    private void fail(CommandSender sender, ConfigurateException error) {
        plugin.getLogger().warning("Unable to save hologram: " + error.getMessage());
        sender.sendMessage(message("<red>Unable to save hologram. Check console."));
    }
}
