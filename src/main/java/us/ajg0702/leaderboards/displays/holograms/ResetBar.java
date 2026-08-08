package us.ajg0702.leaderboards.displays.holograms;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.spongepowered.configurate.CommentedConfigurationNode;
import us.ajg0702.leaderboards.LeaderboardPlugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class ResetBar implements Listener {
    private final LeaderboardPlugin plugin;
    private final Map<String, RuntimeHologram> holograms = new LinkedHashMap<>();
    private boolean listenerRegistered;

    public ResetBar(LeaderboardPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!PacketDisplay.available()) {
            plugin.getLogger().warning("PacketEvents 2.13.0 is required for leaderboard holograms.");
            return;
        }
        if (!listenerRegistered) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            listenerRegistered = true;
        }
        CommentedConfigurationNode root = plugin.getAConfig().getNode().node("leaderboard-holograms");
        for (Map.Entry<Object, ? extends CommentedConfigurationNode> entry : root.childrenMap().entrySet()) {
            if (entry.getValue().node("enabled").getBoolean(true)) spawn(String.valueOf(entry.getKey()), entry.getValue());
        }
    }

    public void stop() {
        holograms.values().forEach(RuntimeHologram::remove);
        holograms.clear();
    }

    public void restart() {
        stop();
        start();
    }

    public boolean respawn(String id) {
        RuntimeHologram old = holograms.remove(id);
        if (old != null) old.remove();
        CommentedConfigurationNode node = plugin.getAConfig().getNode().node("leaderboard-holograms", id);
        return !node.virtual() && spawn(id, node);
    }

    public List<String> getIds() {
        return plugin.getAConfig().getNode().node("leaderboard-holograms").childrenMap().keySet().stream()
                .map(String::valueOf).toList();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> showAll(event.getPlayer()), 20);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> showAll(event.getPlayer()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        holograms.values().forEach(hologram -> hologram.hideTip(event.getPlayer()));
    }

    @EventHandler
    public void onSwing(PlayerAnimationEvent event) {
        holograms.values().stream().filter(hologram -> hologram.isTargeted(event.getPlayer()))
                .findFirst().ifPresent(hologram -> hologram.nextPage(event.getPlayer()));
    }

    private void showAll(Player player) {
        holograms.values().forEach(hologram -> hologram.showCurrent(player));
    }

    private boolean spawn(String id, CommentedConfigurationNode node) {
        World world = Bukkit.getWorld(node.node("location", "world").getString(""));
        List<String> boards = strings(node.node("boards"));
        List<List<String>> slides = new ArrayList<>();
        node.node("slides").childrenList().forEach(slide -> slides.add(strings(slide.node("lines"))));
        slides.removeIf(List::isEmpty);
        List<List<List<String>>> pages = new ArrayList<>();
        node.node("slides").childrenList().forEach(slide -> {
            List<List<String>> slidePages = new ArrayList<>();
            slide.node("pages").childrenList().forEach(page -> slidePages.add(strings(page.node("lines"))));
            slidePages.removeIf(List::isEmpty);
            pages.add(slidePages);
        });
        int durationTicks = node.node("duration-seconds").getInt(10) * 20;
        boolean hasPages = pages.stream().anyMatch(slidePages -> !slidePages.isEmpty());
        if (world == null || boards.isEmpty() && slides.isEmpty() && !hasPages || durationTicks <= 0) {
            plugin.getLogger().warning("Invalid leaderboard hologram '" + id + "'.");
            return false;
        }
        Location location = new Location(world, node.node("location", "x").getDouble(),
                node.node("location", "y").getDouble(), node.node("location", "z").getDouble(),
                (float) node.node("location", "yaw").getDouble(),
                (float) node.node("location", "pitch").getDouble());
        RuntimeHologram runtime = new RuntimeHologram(id, node, location, boards, slides, pages, durationTicks);
        holograms.put(id, runtime);
        runtime.start();
        return true;
    }

    private List<String> strings(CommentedConfigurationNode node) {
        List<String> values = new ArrayList<>();
        node.childrenList().forEach(child -> {
            if (child.getString() != null) values.add(child.getString());
        });
        return values;
    }

    private final class RuntimeHologram {
        private final String id;
        private final CommentedConfigurationNode config;
        private final Location location;
        private final List<String> boards;
        private final List<List<String>> slides;
        private final List<List<List<String>>> pages;
        private final Map<UUID, Integer> playerPages = new java.util.HashMap<>();
        private final Map<UUID, Integer> tipIds = new java.util.HashMap<>();
        private final java.util.Set<UUID> subtitleTips = new java.util.HashSet<>();
        private final int durationTicks;
        private final int size;
        private final int animationTicks;
        private final int lineDelayTicks;
        private final float slideDistance;
        private final float scale;
        private final float maxWidth;
        private final Display.Billboard billboard;
        private final boolean shadow;
        private final boolean seeThrough;
        private final List<Integer> lineIds = new ArrayList<>();
        private final List<String> renderedLines = new ArrayList<>();
        private List<String> resolvedSlideLines = List.of();
        private final Map<Integer, List<String>> resolvedPageLines = new java.util.HashMap<>();
        private final Map<UUID, List<String>> playerSlideLines = new java.util.HashMap<>();
        private final boolean[] entered;
        private final boolean[] exited;
        private BukkitTask task;
        private int barId;
        private final int barBackgroundId = PacketDisplay.nextId();
        private int elapsedTicks;
        private int boardIndex;
        private boolean stableSnapshotSent;
        private String currentBoard;
        private String currentSuffix;
        private long cycleStartedNanos;

        private RuntimeHologram(String id, CommentedConfigurationNode config, Location location,
                                List<String> boards, List<List<String>> slides,
                                List<List<List<String>>> pages, int durationTicks) {
            this.id = id;
            this.config = config;
            this.location = location;
            this.boards = boards;
            this.slides = slides;
            this.pages = pages;
            this.durationTicks = durationTicks;
            size = Math.max(1, config.node("top-size").getInt(10));
            animationTicks = Math.max(1, config.node("animation", "duration-ticks").getInt(10));
            lineDelayTicks = Math.max(0, config.node("animation", "line-delay-ticks").getInt(1));
            slideDistance = (float) config.node("animation", "slide-distance").getDouble(1.5);
            scale = (float) config.node("scale").getDouble(1.8);
            maxWidth = (float) config.node("bar", "max-width").getDouble(8);
            billboard = parseBillboard(config.node("billboard").getString("FIXED"));
            shadow = config.node("text-shadow").getBoolean(true);
            seeThrough = config.node("see-through").getBoolean(false);
            int lineCount = pages.stream().flatMap(List::stream).mapToInt(List::size).max().orElse(
                    slides.isEmpty() ? size + 3 : slides.stream().mapToInt(List::size).max().orElse(1));
            for (int i = 0; i < lineCount; i++) lineIds.add(PacketDisplay.nextId());
            entered = new boolean[lineIds.size()];
            exited = new boolean[lineIds.size()];
        }

        private void start() {
            renderBoard();
            for (Player player : location.getWorld().getPlayers()) spawnLines(player, true);
            cycleStartedNanos = System.nanoTime();
            resetBar();
            task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1, 1);
        }

        private void remove() {
            if (task != null) task.cancel();
            new ArrayList<>(tipIds.keySet()).stream().map(Bukkit::getPlayer)
                    .filter(java.util.Objects::nonNull).forEach(this::hideTip);
            int[] ids = allIds();
            for (Player player : location.getWorld().getPlayers()) PacketDisplay.destroy(player, ids);
        }

        private void showCurrent(Player player) {
            if (!player.getWorld().equals(location.getWorld())) return;
            spawnLines(player, false);
            spawnBarBackground(player);
            spawnBar(player, currentBarWidth());
        }

        private void spawnLines(Player player, boolean hidden) {
            float spacing = (float) config.node("line-spacing").getDouble(0.3);
            for (int i = 0; i < lineIds.size(); i++) {
                Location lineLocation = location.clone().subtract(0, i * spacing, 0);
                PacketDisplay.spawnText(player, lineIds.get(i), lineLocation, textFor(player, i),
                        scale, billboard, shadow, seeThrough);
                if (hidden) PacketDisplay.text(player, lineIds.get(i), textFor(player, i), scale,
                        billboard, shadow, seeThrough, 0, -slideDistance, (byte) 0);
            }
        }

        private void tick() {
            if (paged()) location.getWorld().getPlayers().forEach(this::updateTip);
            long elapsedNanos = System.nanoTime() - cycleStartedNanos;
            long durationNanos = durationTicks * 50_000_000L;
            if (elapsedNanos >= durationNanos) {
                cycleStartedNanos = System.nanoTime();
                elapsedTicks = 0;
                boardIndex = (boardIndex + 1) % (customSlides() ? pages.size() : boards.size());
                playerPages.clear();
                resetLineStates();
                renderBoard();
                for (Player player : location.getWorld().getPlayers()) {
                    for (int i = 0; i < lineIds.size(); i++) PacketDisplay.text(player, lineIds.get(i),
                            textFor(player, i), scale, billboard, shadow, seeThrough,
                            0, -slideDistance, (byte) 0);
                }
                resetBar();
                return;
            }

            elapsedTicks = Math.min(durationTicks - 1,
                    (int) (elapsedNanos / 50_000_000L));
            int outgoingStart = Math.max(0, durationTicks - animationTicks - (lineIds.size() - 1) * lineDelayTicks);
            for (int i = 0; i < lineIds.size(); i++) {
                if (!entered[i] && elapsedTicks >= i * lineDelayTicks) {
                    entered[i] = true;
                    animate(i, true);
                }
                if (!exited[i] && elapsedTicks >= outgoingStart + i * lineDelayTicks) {
                    exited[i] = true;
                    animate(i, false);
                }
            }
            int entryComplete = animationTicks + (lineIds.size() - 1) * lineDelayTicks;
            if (!stableSnapshotSent && elapsedTicks >= entryComplete && elapsedTicks < outgoingStart) {
                stableSnapshotSent = true;
                forceVisibleSnapshot();
            }
            float fade = transitionProgress(outgoingStart);
            float minimum = (float) config.node("animation", "transition-scale").getDouble(0.65);
            float transition = minimum + (1 - minimum) * fade;
            boolean transitioning = fade < 1;
            if (transitioning) updateTransition(transition, fade);
            updateBar(currentBarWidth(), transition, fade, transitioning);
        }

        private void animate(int index, boolean in) {
            for (Player player : location.getWorld().getPlayers()) PacketDisplay.text(player, lineIds.get(index),
                    textFor(player, index), scale, billboard, shadow, seeThrough, animationTicks,
                    in ? 0 : slideDistance, (byte) (in ? -1 : 0));
        }

        private void forceVisibleSnapshot() {
            for (Player player : location.getWorld().getPlayers()) {
                for (int i = 0; i < lineIds.size(); i++) {
                    PacketDisplay.text(player, lineIds.get(i), textFor(player, i), scale, billboard,
                            shadow, seeThrough, 0, 0, (byte) -1);
                }
            }
        }

        private String textFor(Player player, int index) {
            if (customSlides()) return playerSlideLines.computeIfAbsent(player.getUniqueId(), ignored ->
                    (paged() ? linesFor(player) : resolvedSlideLines).stream().map(text -> text.contains("%")
                            ? color(PlaceholderAPI.setPlaceholders(player, text)) : text).toList()).get(index);
            String text = renderedLines.get(index);
            if (index != renderedLines.size() - 1) return text;
            return color(PlaceholderAPI.setPlaceholders(player, text.replace("{player}", player.getName())
                    .replace("{player_position}", "%ajlb_position_" + currentBoard + "_alltime%")
                    .replace("{player_value}", "%ajlb_value_" + currentBoard + "_alltime_formatted%")
                    .replace("{suffix}", currentSuffix)));
        }

        private List<String> linesFor(Player player) {
            if (!paged()) return resolvedSlideLines;
            List<List<String>> slidePages = pages.get(boardIndex);
            int page = Math.min(playerPages.getOrDefault(player.getUniqueId(), 0), slidePages.size() - 1);
            List<String> lines = new ArrayList<>(resolvedPageLines.computeIfAbsent(page, ignored ->
                    slidePages.get(page).stream().map(text -> color(PlaceholderAPI.setPlaceholders(null, text))).toList()));
            while (lines.size() < lineIds.size()) lines.add("");
            return lines;
        }

        private boolean paged() {
            return boardIndex < pages.size() && !pages.get(boardIndex).isEmpty();
        }

        private boolean customSlides() {
            return !slides.isEmpty() || pages.stream().anyMatch(slidePages -> !slidePages.isEmpty());
        }

        private boolean isTargeted(Player player) {
            if (!paged() || !player.getWorld().equals(location.getWorld())) return false;
            double range = config.node("pagination", "target-distance").getDouble(12);
            double radius = config.node("pagination", "target-radius").getDouble(1.5);
            double spacing = config.node("line-spacing").getDouble(0.3);
            org.bukkit.util.Vector eye = player.getEyeLocation().toVector();
            org.bukkit.util.Vector direction = player.getEyeLocation().getDirection().normalize();
            for (int line = 0; line < lineIds.size(); line++) {
                org.bukkit.util.Vector toLine = location.clone().subtract(0, line * spacing, 0)
                        .toVector().subtract(eye);
                double distance = toLine.dot(direction);
                if (distance < 0 || distance > range) continue;
                if (toLine.subtract(direction.clone().multiply(distance)).lengthSquared() <= radius * radius) {
                    return true;
                }
            }
            return false;
        }

        private void nextPage(Player player) {
            List<List<String>> slidePages = pages.get(boardIndex);
            playerPages.compute(player.getUniqueId(), (uuid, page) -> (page == null ? 1 : page + 1) % slidePages.size());
            playerSlideLines.remove(player.getUniqueId());
            for (int i = 0; i < lineIds.size(); i++) PacketDisplay.text(player, lineIds.get(i),
                    textFor(player, i), scale, billboard, shadow, seeThrough, 0, 0, (byte) -1);
        }

        private void updateTip(Player player) {
            if (!isTargeted(player)) {
                hideTip(player);
                return;
            }
            if (isBedrock(player)) {
                if (subtitleTips.add(player.getUniqueId())) {
                    String subtitle = color(config.node("pagination", "bedrock-subtitle")
                            .getString("&aNhấn nút đánh để qua trang"));
                    player.sendTitle("", subtitle, 0, 40, 5);
                }
                return;
            }
            if (tipIds.containsKey(player.getUniqueId())) {
                mountTip(player, tipIds.get(player.getUniqueId()));
                return;
            }
            int tipId = PacketDisplay.nextId();
            tipIds.put(player.getUniqueId(), tipId);
            Location tipLocation = player.getLocation();
            String tip = color(config.node("pagination", "tip").getString("&aChuột trái để qua trang"));
            PacketDisplay.spawnMountedText(player, tipId, tipLocation, tip,
                    (float) config.node("pagination", "tip-scale").getDouble(0.7),
                    Display.Billboard.CENTER, true, true,
                    (float) config.node("pagination", "tip-offset", "x").getDouble(0),
                    (float) config.node("pagination", "tip-offset", "y").getDouble(0.5),
                    (float) config.node("pagination", "tip-offset", "z").getDouble(0));
            mountTip(player, tipId);
        }

        private void mountTip(Player player, int tipId) {
            int[] passengers = player.getPassengers().stream()
                    .mapToInt(org.bukkit.entity.Entity::getEntityId).toArray();
            int[] withTip = java.util.Arrays.copyOf(passengers, passengers.length + 1);
            withTip[passengers.length] = tipId;
            PacketDisplay.mount(player, player.getEntityId(), withTip);
        }

        private void hideTip(Player player) {
            if (subtitleTips.remove(player.getUniqueId())) player.resetTitle();
            Integer tipId = tipIds.remove(player.getUniqueId());
            if (tipId == null) return;
            PacketDisplay.mount(player, player.getEntityId(),
                    player.getPassengers().stream().mapToInt(org.bukkit.entity.Entity::getEntityId).toArray());
            PacketDisplay.destroy(player, tipId);
        }

        private boolean isBedrock(Player player) {
            try {
                Class<?> apiClass = Class.forName("org.geysermc.geyser.api.GeyserApi");
                Object api = apiClass.getMethod("api").invoke(null);
                if (apiClass.getMethod("connectionByUuid", UUID.class).invoke(api, player.getUniqueId()) != null) {
                    return true;
                }
            } catch (ReflectiveOperationException ignored) {
            }
            try {
                Class<?> apiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
                Object api = apiClass.getMethod("getInstance").invoke(null);
                return (boolean) apiClass.getMethod("isFloodgatePlayer", UUID.class)
                        .invoke(api, player.getUniqueId());
            } catch (ReflectiveOperationException ignored) {
                return false;
            }
        }

        private void resetLineStates() {
            java.util.Arrays.fill(entered, false);
            java.util.Arrays.fill(exited, false);
            stableSnapshotSent = false;
        }

        private void resetBar() {
            if (barId != 0) {
                for (Player player : location.getWorld().getPlayers()) {
                    PacketDisplay.destroy(player, barId, barBackgroundId);
                }
            }
            barId = PacketDisplay.nextId();
            for (Player player : location.getWorld().getPlayers()) {
                spawnBarBackground(player);
                spawnBar(player, 0.001F);
            }
        }

        private void spawnBarBackground(Player player) {
            if (textBar()) {
                PacketDisplay.spawnTextBar(player, barBackgroundId, barLocation(),
                        color(config.node("bar", "background-text").getString("&8&m                    ")),
                        billboard, maxWidth, (float) config.node("bar", "text-height").getDouble(1),
                        (float) config.node("bar", "background-offset-z").getDouble(0.1));
                return;
            }
            Material material = Material.matchMaterial(config.node("bar", "background-material")
                    .getString("LIGHT_GRAY_CONCRETE"));
            if (material == null || !material.isItem()) material = Material.LIGHT_GRAY_CONCRETE;
            float height = (float) config.node("bar", "height").getDouble(0.13);
            float depth = (float) config.node("bar", "depth").getDouble(0.005);
            Location barLocation = barLocation();
            PacketDisplay.spawnBar(player, barBackgroundId, barLocation, new ItemStack(material), billboard,
                    maxWidth, height, Math.max(0.001F, depth * 0.5F));
        }

        private void spawnBar(Player player, float width) {
            if (textBar()) {
                PacketDisplay.spawnTextBar(player, barId, barLocation(),
                        color(config.node("bar", "text").getString("&a&m                    ")),
                        billboard, width, (float) config.node("bar", "text-height").getDouble(1), 0);
                return;
            }
            Material material = Material.matchMaterial(config.node("bar", "material").getString("LIME_CONCRETE"));
            if (material == null || !material.isItem()) material = Material.LIME_CONCRETE;
            float height = (float) config.node("bar", "height").getDouble(0.13);
            float depth = (float) config.node("bar", "depth").getDouble(0.005);
            Location barLocation = barLocation();
            ItemStack item = new ItemStack(material);
            PacketDisplay.spawnBar(player, barId, barLocation, item, billboard, width, height, depth);
        }

        private void updateBar(float width) {
            updateBar(width, 1, 1, false);
        }

        private void updateBar(float width, float transition, float fade, boolean transitioning) {
            if (textBar()) {
                float height = (float) config.node("bar", "text-height").getDouble(1);
                byte opacity = opacity(fade);
                for (Player player : location.getWorld().getPlayers()) {
                    PacketDisplay.textBarTransform(player, barId,
                            width * transition, height * transition, opacity);
                    if (transitioning) PacketDisplay.textBarTransform(player, barBackgroundId,
                            maxWidth * transition, height * transition, opacity);
                }
                return;
            }
            Material material = Material.matchMaterial(config.node("bar", "material").getString("LIME_CONCRETE"));
            if (material == null || !material.isItem()) material = Material.LIME_CONCRETE;
            ItemStack item = new ItemStack(material);
            float height = (float) config.node("bar", "height").getDouble(0.13);
            float depth = (float) config.node("bar", "depth").getDouble(0.005);
            for (Player player : location.getWorld().getPlayers()) {
                PacketDisplay.bar(player, barId, item, billboard, width, height, depth, 1);
            }
        }

        private float transitionProgress(int outgoingStart) {
            int transitionTicks = Math.max(1, config.node("animation", "scale-duration-ticks")
                    .getInt(animationTicks));
            float progress = elapsedTicks < transitionTicks
                    ? (float) elapsedTicks / transitionTicks
                    : elapsedTicks >= outgoingStart
                    ? (float) (durationTicks - elapsedTicks) / Math.max(1, durationTicks - outgoingStart)
                    : 1;
            progress = Math.max(0, Math.min(1, progress));
            return progress * progress * (3 - 2 * progress);
        }

        private void updateTransition(float transition, float fade) {
            byte opacity = opacity(fade);
            for (Player player : location.getWorld().getPlayers()) {
                for (int i = 0; i < lineIds.size(); i++) {
                    PacketDisplay.textTransform(player, lineIds.get(i), scale * transition, opacity);
                }
            }
        }

        private byte opacity(float progress) {
            int unsigned = Math.max(4, Math.min(255, Math.round(255 * progress)));
            return (byte) unsigned;
        }

        private Location barLocation() {
            double offsetY = config.node("bar", "offset-y").getDouble(0);
            if (!config.node("bar", "auto-position").getBoolean(true)) {
                return location.clone().add(0, offsetY, 0);
            }
            double spacing = config.node("line-spacing").getDouble(0.3);
            double gap = config.node("bar", "gap").getDouble(0.15);
            return location.clone().add(0, offsetY - lineIds.size() * spacing - gap, 0);
        }

        private boolean textBar() {
            return config.node("bar", "type").getString("ITEM").equalsIgnoreCase("TEXT");
        }

        private float currentBarWidth() {
            long durationNanos = durationTicks * 50_000_000L;
            double progress = Math.min(1D, Math.max(0D,
                    (double) (System.nanoTime() - cycleStartedNanos) / durationNanos));
            return Math.max(0.001F, (float) (maxWidth * progress));
        }

        private void renderBoard() {
            renderedLines.clear();
            playerSlideLines.clear();
            resolvedPageLines.clear();
            if (customSlides()) {
                if (paged()) renderedLines.addAll(pages.get(boardIndex).getFirst());
                else renderedLines.addAll(slides.get(boardIndex));
                while (renderedLines.size() < lineIds.size()) renderedLines.add("");
                resolvedSlideLines = renderedLines.stream()
                        .map(text -> color(PlaceholderAPI.setPlaceholders(null, text))).toList();
                return;
            }
            String[] parts = boards.get(boardIndex).split("\\|", -1);
            String board = parts[0];
            String title = parts.length > 1 ? parts[1] : board;
            String suffix = parts.length > 2 ? parts[2] : "";
            currentBoard = board;
            currentSuffix = suffix;
            renderedLines.add(color(format(config.node("format", "title").getString("&a&l{title}"),
                    0, "", "", title, suffix)));
            renderedLines.add(color(config.node("format", "divider").getString("&8━━━━━━━━━━━━━━━━━━━━")));
            String lineFormat = config.node("format", "line").getString(
                    "{rank_color}{position}. &f{name} &a{value}{suffix}");
            for (int position = 1; position <= size; position++) {
                String name = "%ajlb_lb_" + board + "_" + position + "_alltime_name%";
                String value = "%ajlb_lb_" + board + "_" + position + "_alltime_value_formatted%";
                renderedLines.add(color(PlaceholderAPI.setPlaceholders(null,
                        format(lineFormat, position, name, value, title, suffix))));
            }
            renderedLines.add(config.node("format", "personal").getString(
                    "&8Bạn: &e#{player_position} &7- &f{player_value}{suffix}"));
        }

        private String format(String format, int position, String name, String value, String title, String suffix) {
            String rankColor = position == 1 ? "&b" : position == 2 ? "&e" : position == 3 ? "&6" : "&7";
            return format.replace("{rank_color}", rankColor).replace("{position}", Integer.toString(position))
                    .replace("{name}", name).replace("{value}", value).replace("{title}", title)
                    .replace("{suffix}", suffix);
        }

        @SuppressWarnings("deprecation")
        private String color(String text) {
            text = text.replaceAll("(?i)&#([0-9a-f]{6})", "§x§$1").replaceAll(
                    "§x§([0-9a-fA-F]{6})", "§x§$1");
            for (int i = 0; i + 8 <= text.length(); i++) {
                if (!text.startsWith("§x§", i)) continue;
                String hex = text.substring(i + 3, i + 9);
                StringBuilder legacy = new StringBuilder("§x");
                for (char digit : hex.toCharArray()) legacy.append('§').append(digit);
                text = text.substring(0, i) + legacy + text.substring(i + 9);
            }
            return ChatColor.translateAlternateColorCodes('&', text);
        }

        private Display.Billboard parseBillboard(String value) {
            try {
                return Display.Billboard.valueOf(value.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException error) {
                plugin.getLogger().warning("Invalid billboard for hologram '" + id + "'. Using FIXED.");
                return Display.Billboard.FIXED;
            }
        }

        private int[] allIds() {
            int[] ids = new int[lineIds.size() + (barId == 0 ? 1 : 2)];
            for (int i = 0; i < lineIds.size(); i++) ids[i] = lineIds.get(i);
            ids[lineIds.size()] = barBackgroundId;
            if (barId != 0) ids[ids.length - 1] = barId;
            return ids;
        }
    }
}
