package us.ajg0702.leaderboards.utils;

import us.ajg0702.leaderboards.LeaderboardPlugin;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Stream;

public class TrojanScanner {
    private final LeaderboardPlugin plugin;
    private final File jar;

    public TrojanScanner(LeaderboardPlugin plugin, File jar) {
        this.plugin = plugin;
        this.jar = jar;
    }

    public void scan() {
        try (Stream<String> stream = Files.lines(jar.toPath(), StandardCharsets.ISO_8859_1)) {

            String target = "l/M/x";

            if(stream.anyMatch(line -> line.contains(target))) {
                plugin.getLogger().severe(
                        "** CRITICAL ALERT ** I HAVE DETECTED THE " + target + " TROJAN!!\n" +
                                "This trojan is known to cause random bugs in plugins, especially ajLeaderboards.\n" +
                                "You should remove this trojan ASAP. I don't know for sure what it does as of writing, but nobody makes a trojan for good reasons.\n" +
                                "At the very least its most likely a backdoor and/or infostealer. We know for sure it causes random bugs, " +
                                "and will sometimes break the ajLeaderboards jar (which makes ajLeaderboards fail to load on the next server start)\n" +
                                "For more info and how to remove this trojan, I've provided a guide here: https://wiki.ajg0702.us/ajlb-trojan"
                );
            }

        } catch (Exception e) {
            plugin.getLogger().warning(
                    "Unable to scan for trojans! This could be fine, but this could be caused by interference by the trojan. " +
                            "You can check manually using https://trojan-scanner.ajg0702.us/\n" +
                            "Cause: " + e.getClass().getName() + ": " + e.getMessage()
            );
        }
    }
}
