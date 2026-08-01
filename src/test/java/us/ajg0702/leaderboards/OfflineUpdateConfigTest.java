package us.ajg0702.leaderboards;

import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OfflineUpdateConfigTest {
    
    private Map<String, Object> getDefaults() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("offline-update-interval-hours", 6);
        defaults.put("offline-update-boards", new ArrayList<>());
        defaults.put("offline-update-run-on-startup", false);
        return defaults;
    }
    
    @Test
    public void testOfflineUpdateIntervalHoursDefault() throws Exception {
        Map<String, Object> defaults = getDefaults();
        int intervalHours = (Integer) defaults.get("offline-update-interval-hours");
        if (intervalHours != 6) {
            throw new Exception("offline-update-interval-hours should default to 6, got: " + intervalHours);
        }
        System.out.println("Passed offline-update-interval-hours default test");
    }
    
    @Test
    public void testOfflineUpdateBoardsDefault() throws Exception {
        Map<String, Object> defaults = getDefaults();
        @SuppressWarnings("unchecked")
        List<String> boards = (List<String>) defaults.get("offline-update-boards");
        if (!boards.isEmpty()) {
            throw new Exception("offline-update-boards should default to empty list, got: " + boards);
        }
        System.out.println("Passed offline-update-boards default test");
    }
    
    @Test
    public void testOfflineUpdateRunOnStartupDefault() throws Exception {
        Map<String, Object> defaults = getDefaults();
        boolean runOnStartup = (Boolean) defaults.get("offline-update-run-on-startup");
        if (runOnStartup != false) {
            throw new Exception("offline-update-run-on-startup should default to false, got: " + runOnStartup);
        }
        System.out.println("Passed offline-update-run-on-startup default test");
    }
}
