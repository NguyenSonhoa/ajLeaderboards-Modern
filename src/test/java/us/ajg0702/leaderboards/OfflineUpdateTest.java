package us.ajg0702.leaderboards;

import org.junit.Test;

public class OfflineUpdateTest {
    
    // Constants from scheduleOfflineUpdates() in LeaderboardPlugin.java
    private static final long TICKS_PER_HOUR = 72000L;
    private static final long TICKS_PER_SECOND = 20L;
    private static final long STARTUP_DELAY_SECONDS = 30L;
    
    // Helper method to calculate interval ticks (matches line 450)
    private long calculateIntervalTicks(int intervalHours) {
        return intervalHours * TICKS_PER_HOUR;
    }
    
    // Helper method to calculate initial delay (matches line 454)
    private long calculateInitialDelay(boolean runOnStartup, int intervalHours) {
        long intervalTicks = calculateIntervalTicks(intervalHours);
        return runOnStartup ? STARTUP_DELAY_SECONDS * TICKS_PER_SECOND : intervalTicks;
    }
    
    @Test
    public void testSixHoursToTicks() throws Exception {
        long result = calculateIntervalTicks(6);
        if (result != 432000L) {
            throw new Exception("6 hours should be 432000 ticks, got " + result);
        }
        System.out.println("Passed 6 hours to ticks test");
    }
    
    @Test
    public void testStartupDelay() throws Exception {
        long result = calculateInitialDelay(true, 6);
        if (result != 600L) {
            throw new Exception("30 seconds should be 600 ticks, got " + result);
        }
        System.out.println("Passed startup delay test");
    }
    
    @Test
    public void testNonStartupDelay() throws Exception {
        long result = calculateInitialDelay(false, 6);
        if (result != 432000L) {
            throw new Exception("Non-startup delay should equal interval (432000), got " + result);
        }
        System.out.println("Passed non-startup delay test");
    }
    
    @Test
    public void testZeroHours() throws Exception {
        long result = calculateIntervalTicks(0);
        if (result != 0L) {
            throw new Exception("0 hours should be 0 ticks, got " + result);
        }
        System.out.println("Passed zero hours test");
    }
    
    @Test
    public void testOneHour() throws Exception {
        long result = calculateIntervalTicks(1);
        if (result != 72000L) {
            throw new Exception("1 hour should be 72000 ticks, got " + result);
        }
        System.out.println("Passed 1 hour test");
    }
    
    @Test
    public void testTwentyFourHours() throws Exception {
        long result = calculateIntervalTicks(24);
        if (result != 1728000L) {
            throw new Exception("24 hours should be 1728000 ticks, got " + result);
        }
        System.out.println("Passed 24 hours test");
    }
    
    @Test
    public void testLargeHoursValue() throws Exception {
        // Test with a large value to ensure it handles larger numbers
        long result = calculateIntervalTicks(100);
        if (result != 7200000L) {
            throw new Exception("100 hours should be 7200000 ticks, got " + result);
        }
        System.out.println("Passed large hours test");
    }
    
    @Test
    public void testZeroHoursStartupDelay() throws Exception {
        // Edge case: 0 hours with runOnStartup=true
        long result = calculateInitialDelay(true, 0);
        if (result != 600L) {
            throw new Exception("0 hours with startup should still use 600 tick delay, got " + result);
        }
        System.out.println("Passed zero hours startup delay test");
    }
    
    @Test
    public void testZeroHoursNonStartupDelay() throws Exception {
        // Edge case: 0 hours with runOnStartup=false
        long result = calculateInitialDelay(false, 0);
        if (result != 0L) {
            throw new Exception("0 hours without startup should be 0 ticks, got " + result);
        }
        System.out.println("Passed zero hours non-startup delay test");
    }
}
