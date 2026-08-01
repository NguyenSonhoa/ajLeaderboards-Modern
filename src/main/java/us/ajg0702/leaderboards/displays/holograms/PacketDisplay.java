package us.ajg0702.leaderboards.displays.holograms;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Quaternion4f;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

final class PacketDisplay {
    private static final AtomicInteger IDS = new AtomicInteger(-700000);

    private PacketDisplay() {}

    static boolean available() {
        try {
            return PacketEvents.getAPI() != null && PacketEvents.getAPI().getPlayerManager() != null;
        } catch (IllegalStateException | NoClassDefFoundError error) {
            return false;
        }
    }

    static int nextId() {
        return IDS.decrementAndGet();
    }

    static void spawnText(Player player, int id, Location location, String text, float scale,
                          Display.Billboard billboard, boolean shadow, boolean seeThrough) {
        spawn(player, id, location, EntityTypes.TEXT_DISPLAY);
        text(player, id, text, scale, billboard, shadow, seeThrough, 0, 0, (byte) -1);
    }

    static void spawnMountedText(Player player, int id, Location location, String text, float scale,
                                 Display.Billboard billboard, boolean shadow, boolean seeThrough,
                                 float offsetX, float offsetY, float offsetZ) {
        spawn(player, id, location, EntityTypes.TEXT_DISPLAY);
        List<EntityData<?>> data = display(0, offsetX, offsetY, offsetZ,
                new Vector3f(scale, scale, scale), billboard);
        data.add(new EntityData<>(23, EntityDataTypes.COMPONENT, jsonText(text)));
        data.add(new EntityData<>(24, EntityDataTypes.INT, 2000));
        data.add(new EntityData<>(25, EntityDataTypes.INT, 0));
        data.add(new EntityData<>(26, EntityDataTypes.BYTE, (byte) -1));
        data.add(new EntityData<>(27, EntityDataTypes.BYTE,
                (byte) ((shadow ? 1 : 0) | (seeThrough ? 2 : 0))));
        send(player, new WrapperPlayServerEntityMetadata(id, data));
    }

    static void text(Player player, int id, String text, float scale, Display.Billboard billboard,
                     boolean shadow, boolean seeThrough, int interpolationTicks,
                     float translationX, byte opacity) {
        List<EntityData<?>> data = display(interpolationTicks, translationX,
                new Vector3f(scale, scale, scale), billboard);
        data.add(new EntityData<>(23, EntityDataTypes.COMPONENT, jsonText(text)));
        data.add(new EntityData<>(24, EntityDataTypes.INT, 2000));
        data.add(new EntityData<>(25, EntityDataTypes.INT, 0));
        data.add(new EntityData<>(26, EntityDataTypes.BYTE, opacity));
        data.add(new EntityData<>(27, EntityDataTypes.BYTE,
                (byte) ((shadow ? 1 : 0) | (seeThrough ? 2 : 0))));
        send(player, new WrapperPlayServerEntityMetadata(id, data));
    }

    static void textTransform(Player player, int id, String text, float scale,
                              Display.Billboard billboard, boolean shadow, boolean seeThrough,
                              byte opacity) {
        List<EntityData<?>> data = display(0, 0,
                new Vector3f(scale, scale, scale), billboard);
        data.add(new EntityData<>(23, EntityDataTypes.COMPONENT, jsonText(text)));
        data.add(new EntityData<>(24, EntityDataTypes.INT, 2000));
        data.add(new EntityData<>(25, EntityDataTypes.INT, 0));
        data.add(new EntityData<>(26, EntityDataTypes.BYTE, opacity));
        data.add(new EntityData<>(27, EntityDataTypes.BYTE,
                (byte) ((shadow ? 1 : 0) | (seeThrough ? 2 : 0))));
        send(player, new WrapperPlayServerEntityMetadata(id, data));
    }

    static void spawnBar(Player player, int id, Location location, ItemStack item, Display.Billboard billboard,
                         float width, float height, float depth) {
        spawn(player, id, location, EntityTypes.ITEM_DISPLAY);
        bar(player, id, item, billboard, width, height, depth, 0);
    }

    static void bar(Player player, int id, ItemStack item, Display.Billboard billboard,
                    float width, float height, float depth, int interpolationTicks) {
        List<EntityData<?>> data = display(interpolationTicks, 0,
                new Vector3f(width, height, depth), billboard);
        data.add(new EntityData<>(23, EntityDataTypes.ITEMSTACK,
                SpigotConversionUtil.fromBukkitItemStack(item)));
        data.add(new EntityData<>(24, EntityDataTypes.BYTE, (byte) 8));
        send(player, new WrapperPlayServerEntityMetadata(id, data));
    }

    static void spawnTextBar(Player player, int id, Location location, String text,
                             Display.Billboard billboard, float width, float height, float translationZ) {
        spawn(player, id, location, EntityTypes.TEXT_DISPLAY);
        textBar(player, id, text, billboard, width, height, translationZ, 0);
    }

    static void textBar(Player player, int id, String text, Display.Billboard billboard,
                        float width, float height, float translationZ, int interpolationTicks) {
        List<EntityData<?>> data = display(interpolationTicks, 0, translationZ,
                new Vector3f(width, height, 1), billboard);
        data.add(new EntityData<>(23, EntityDataTypes.COMPONENT, jsonText(text)));
        data.add(new EntityData<>(24, EntityDataTypes.INT, 2000));
        data.add(new EntityData<>(25, EntityDataTypes.INT, 0));
        data.add(new EntityData<>(26, EntityDataTypes.BYTE, (byte) -1));
        data.add(new EntityData<>(27, EntityDataTypes.BYTE, (byte) 0));
        send(player, new WrapperPlayServerEntityMetadata(id, data));
    }

    static void textBarTransform(Player player, int id, String text, Display.Billboard billboard,
                                 float width, float height, float translationZ, byte opacity) {
        List<EntityData<?>> data = display(0, 0, translationZ,
                new Vector3f(width, height, 1), billboard);
        data.add(new EntityData<>(23, EntityDataTypes.COMPONENT, jsonText(text)));
        data.add(new EntityData<>(24, EntityDataTypes.INT, 2000));
        data.add(new EntityData<>(25, EntityDataTypes.INT, 0));
        data.add(new EntityData<>(26, EntityDataTypes.BYTE, opacity));
        data.add(new EntityData<>(27, EntityDataTypes.BYTE, (byte) 0));
        send(player, new WrapperPlayServerEntityMetadata(id, data));
    }

    static void destroy(Player player, int... ids) {
        if (player.isOnline()) send(player, new WrapperPlayServerDestroyEntities(ids));
    }

    static void mount(Player viewer, int vehicleId, int... passengerIds) {
        send(viewer, new WrapperPlayServerSetPassengers(vehicleId, passengerIds));
    }

    private static void spawn(Player player, int id, Location location, EntityType type) {
        if (!player.isOnline() || !player.getWorld().equals(location.getWorld())) return;
        com.github.retrooper.packetevents.protocol.world.Location packetLocation =
                new com.github.retrooper.packetevents.protocol.world.Location(location.getX(), location.getY(),
                        location.getZ(), location.getYaw(), location.getPitch());
        send(player, new WrapperPlayServerSpawnEntity(id, UUID.randomUUID(), type,
                packetLocation, 0, 0, null));
    }

    private static List<EntityData<?>> display(int interpolationTicks, float translationX,
                                                 Vector3f scale, Display.Billboard billboard) {
        return display(interpolationTicks, translationX, 0, scale, billboard);
    }

    private static List<EntityData<?>> display(int interpolationTicks, float translationX, float translationZ,
                                                Vector3f scale, Display.Billboard billboard) {
        return display(interpolationTicks, translationX, 0, translationZ, scale, billboard);
    }

    private static List<EntityData<?>> display(int interpolationTicks, float translationX, float translationY,
                                                float translationZ, Vector3f scale,
                                                Display.Billboard billboard) {
        List<EntityData<?>> data = new ArrayList<>();
        data.add(new EntityData<>(8, EntityDataTypes.INT, 0));
        data.add(new EntityData<>(9, EntityDataTypes.INT, interpolationTicks));
        data.add(new EntityData<>(10, EntityDataTypes.INT, 1));
        data.add(new EntityData<>(11, EntityDataTypes.VECTOR3F,
                new Vector3f(translationX, translationY, translationZ)));
        data.add(new EntityData<>(12, EntityDataTypes.VECTOR3F, scale));
        data.add(new EntityData<>(13, EntityDataTypes.QUATERNION, new Quaternion4f(0, 0, 0, 1)));
        data.add(new EntityData<>(14, EntityDataTypes.QUATERNION, new Quaternion4f(0, 0, 0, 1)));
        data.add(new EntityData<>(15, EntityDataTypes.BYTE, billboard(billboard)));
        data.add(new EntityData<>(16, EntityDataTypes.INT, 0x00F000F0));
        data.add(new EntityData<>(17, EntityDataTypes.FLOAT, 64F));
        return data;
    }

    private static byte billboard(Display.Billboard billboard) {
        return (byte) switch (billboard) {
            case FIXED -> 0;
            case VERTICAL -> 1;
            case HORIZONTAL -> 2;
            case CENTER -> 3;
        };
    }

    private static String jsonText(String text) {
        StringBuilder json = new StringBuilder("{\"text\":\"\",\"extra\":[");
        String color = "white";
        boolean bold = false;
        boolean strikethrough = false;
        StringBuilder part = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current == '§' && i + 1 < text.length()) {
                if (part.length() > 0) {
                    first = appendPart(json, part.toString(), color, bold, strikethrough, first);
                    part.setLength(0);
                }
                char code = Character.toLowerCase(text.charAt(++i));
                if (code == 'x' && i + 12 < text.length()) {
                    StringBuilder hex = new StringBuilder("#");
                    boolean valid = true;
                    for (int digit = 0; digit < 6; digit++) {
                        int marker = i + 1 + digit * 2;
                        if (text.charAt(marker) != '§') {
                            valid = false;
                            break;
                        }
                        hex.append(text.charAt(marker + 1));
                    }
                    if (valid) {
                        color = hex.toString();
                        bold = false;
                        strikethrough = false;
                        i += 12;
                        continue;
                    }
                }
                String nextColor = legacyColor(code);
                if (nextColor != null) {
                    color = nextColor;
                    bold = false;
                    strikethrough = false;
                } else if (code == 'l') {
                    bold = true;
                } else if (code == 'm') {
                    strikethrough = true;
                } else if (code == 'r') {
                    color = "white";
                    bold = false;
                    strikethrough = false;
                }
            } else {
                part.append(current);
            }
        }
        if (part.length() > 0) appendPart(json, part.toString(), color, bold, strikethrough, first);
        return json.append("]}").toString();
    }

    private static boolean appendPart(StringBuilder json, String text, String color, boolean bold,
                                      boolean strikethrough, boolean first) {
        if (!first) json.append(',');
        json.append("{\"text\":\"").append(escape(text)).append("\",\"color\":\"")
                .append(color).append("\",\"bold\":").append(bold)
                .append(",\"strikethrough\":").append(strikethrough).append('}');
        return false;
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private static String legacyColor(char code) {
        return switch (code) {
            case '0' -> "black"; case '1' -> "dark_blue"; case '2' -> "dark_green";
            case '3' -> "dark_aqua"; case '4' -> "dark_red"; case '5' -> "dark_purple";
            case '6' -> "gold"; case '7' -> "gray"; case '8' -> "dark_gray";
            case '9' -> "blue"; case 'a' -> "green"; case 'b' -> "aqua";
            case 'c' -> "red"; case 'd' -> "light_purple"; case 'e' -> "yellow";
            case 'f' -> "white"; default -> null;
        };
    }

    private static void send(Player player, PacketWrapper<?> packet) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }
}
