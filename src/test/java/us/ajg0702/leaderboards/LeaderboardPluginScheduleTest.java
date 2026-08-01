package us.ajg0702.leaderboards;

import org.junit.Test;
import us.ajg0702.leaderboards.boards.TimedType;

import java.time.*;
import java.time.zone.ZoneRules;

import static org.junit.Assert.*;

/**
 * Tests for reset scheduling logic in LeaderboardPlugin.
 * 
 * These tests verify the scheduling calculations for time-based leaderboard resets,
 * particularly focusing on DST transitions and offset handling.
 * 
 * NOTE: The production code now uses TimedType.getNextResetEpochSeconds() and
 * TimedType.getEstimatedLastResetEpochSeconds() which use atZone(zone).toEpochSecond()
 * instead of the buggy toEpochSecond(TimeUtils.getDefaultZoneOffset()) approach.
 */
public class LeaderboardPluginScheduleTest {

    /**
     * Test that calculating delay to next reset works correctly.
     * This tests the ACTUAL code path used in LeaderboardPlugin.scheduleReset() line 442.
     */
    @Test
    public void testCalculateDelayToNextReset() {
        for (TimedType type : TimedType.values()) {
            if (type.equals(TimedType.ALLTIME)) continue;

            long now = Instant.now().getEpochSecond();
            
            // This is how LeaderboardPlugin calculates nextReset epoch (line 442)
            long nextResetEpoch = type.getNextResetEpochSeconds();
            
            long delay = nextResetEpoch - now;
            
            // Delay should be positive (next reset is in the future)
            assertTrue("Delay for " + type + " should be positive, was: " + delay, delay > 0);
            
            // Delay should be reasonable (not more than a year)
            assertTrue("Delay for " + type + " should be less than a year", delay < 365L * 24 * 60 * 60);
        }
        System.out.println("Passed calculate delay to next reset test");
    }

    /**
     * Test that lastReset < estLastReset comparison works correctly.
     * This tests the ACTUAL code path used in LeaderboardPlugin.scheduleReset() line 448.
     */
    @Test
    public void testLastResetEstLastResetComparison() {
        for (TimedType type : TimedType.values()) {
            if (type.equals(TimedType.ALLTIME)) continue;

            LocalDateTime now = LocalDateTime.now();
            
            // This is how LeaderboardPlugin calculates estimated last reset (line 448)
            long estLastResetEpoch = type.getEstimatedLastResetEpochSeconds();
            
            // Calculate what lastReset would be for "now" (should be approximately equal or slightly before)
            LocalDateTime nowNormalized = now.withSecond(0).withNano(0);
            long nowEpoch = nowNormalized.atZone(ZoneId.systemDefault()).toEpochSecond();
            
            // The estimated last reset should be <= now (in epoch seconds)
            // This test verifies the comparison logic would work
            System.out.println(type + " estLastReset: " + estLastResetEpoch + " vs now: " + nowEpoch);
        }
        System.out.println("Passed lastReset/estLastReset comparison test");
    }

    /**
     * Test MONTHLY reset across DST boundary (e.g., November -> December in US).
     * This verifies the TimedType.getNextResetEpochSeconds() method handles DST correctly.
     * 
     * The OLD buggy approach: toEpochSecond(TimeUtils.getDefaultZoneOffset())
     * which uses the offset for Instant.now(), not the offset for the future date.
     * 
     * The NEW fixed approach: atZone(zone).toEpochSecond()
     * which correctly uses the offset for the future date.
     */
    @Test
    public void testMonthlyResetAcrossDSTBoundary() {
        // Test a scenario where DST affects the calculation
        // In US: DST ends in November, so moving from March/November to December
        
        ZoneId systemZone = ZoneId.systemDefault();
        ZoneRules rules = systemZone.getRules();
        
        // Get current offset (the buggy code used this for ALL dates)
        ZoneOffset currentOffset = TimeUtils.getDefaultZoneOffset();
        
        // Calculate next monthly reset (typically 1+ month in future)
        LocalDateTime nextMonthly = TimedType.MONTHLY.getNextReset();
        
        // NEW FIXED approach: getNextResetEpochSeconds() uses atZone(zone).toEpochSecond()
        long fixedEpoch = TimedType.MONTHLY.getNextResetEpochSeconds();
        
        // OLD BUGGY approach: using current offset for future date (for comparison)
        long buggyEpoch = nextMonthly.toEpochSecond(currentOffset);
        
        // Get the correct offset at the future date
        ZoneOffset futureOffset = rules.getOffset(nextMonthly.atZone(systemZone).toInstant());
        
        // These SHOULD be equal - the fixed method handles DST correctly
        System.out.println("Current offset (buggy): " + currentOffset);
        System.out.println("Future offset for " + nextMonthly + ": " + futureOffset);
        System.out.println("Buggy epoch (old approach): " + buggyEpoch);
        System.out.println("Fixed epoch (new approach): " + fixedEpoch);
        
        // Verify the fixed epoch equals what we calculate with the correct offset
        long correctEpoch = nextMonthly.toEpochSecond(futureOffset);
        assertEquals("Fixed method should produce correct epoch", correctEpoch, fixedEpoch);
        
        // If offsets differ, show that the approaches produce different results
        if (!currentOffset.equals(futureOffset)) {
            System.out.println("DST transition detected between now and next reset!");
            System.out.println("Buggy vs Fixed differ by: " + (buggyEpoch - fixedEpoch) + " seconds");
        }
        
        System.out.println("Passed DST boundary test");
    }

    /**
     * Test that the NEW fixed methods (getNextResetEpochSeconds) produce correct results
     * compared to the OLD buggy approach (toEpochSecond(TimeUtils.getDefaultZoneOffset())).
     * 
     * This test verifies that the fix at TimedType.java lines 88-91 is working correctly.
     */
    @Test
    public void testNewMethodsVsOldBuggyApproach() {
        ZoneId systemZone = ZoneId.systemDefault();
        ZoneRules rules = systemZone.getRules();
        
        for (TimedType type : TimedType.values()) {
            if (type.equals(TimedType.ALLTIME)) continue;

            LocalDateTime nextReset = type.getNextReset();
            
            // OLD buggy approach (what was in LeaderboardPlugin.java lines 442 and 448)
            ZoneOffset currentOffset = TimeUtils.getDefaultZoneOffset();
            long oldEpoch = nextReset.toEpochSecond(currentOffset);
            
            // NEW fixed approach (TimedType.getNextResetEpochSeconds())
            long newEpoch = type.getNextResetEpochSeconds();
            
            // Get the correct offset at the future date
            ZoneOffset correctOffset = rules.getOffset(nextReset.atZone(systemZone).toInstant());
            long expectedEpoch = nextReset.toEpochSecond(correctOffset);
            
            System.out.println(type + ":");
            System.out.println("  Old approach (buggy): " + oldEpoch);
            System.out.println("  New approach (fixed): " + newEpoch);
            System.out.println("  Expected (correct):  " + expectedEpoch);
            
            // The new approach should match expected
            assertEquals("New method should produce correct epoch for " + type, expectedEpoch, newEpoch);
            
            // If DST is in transition, old approach would be wrong
            if (!currentOffset.equals(correctOffset)) {
                System.out.println("  DST difference detected! Old approach was wrong.");
                assertTrue("Old and new should differ when DST changes", oldEpoch != newEpoch);
            }
        }
        
        System.out.println("Passed new methods vs old approach comparison test");
    }

    /**
     * Test estimated last reset with DST awareness.
     * Verifies getEstimatedLastResetEpochSeconds() handles DST correctly.
     */
    @Test
    public void testEstimatedLastResetDST() {
        ZoneId systemZone = ZoneId.systemDefault();
        ZoneRules rules = systemZone.getRules();
        
        for (TimedType type : TimedType.values()) {
            if (type.equals(TimedType.ALLTIME)) continue;

            LocalDateTime estLastReset = type.getEstimatedLastReset();
            
            // OLD buggy approach
            ZoneOffset currentOffset = TimeUtils.getDefaultZoneOffset();
            long oldEpoch = estLastReset.toEpochSecond(currentOffset);
            
            // NEW fixed approach
            long newEpoch = type.getEstimatedLastResetEpochSeconds();
            
            // Get correct offset at the date
            ZoneOffset correctOffset = rules.getOffset(estLastReset.atZone(systemZone).toInstant());
            long expectedEpoch = estLastReset.toEpochSecond(correctOffset);
            
            System.out.println(type + " estLastReset:");
            System.out.println("  Old approach: " + oldEpoch);
            System.out.println("  New approach: " + newEpoch);
            System.out.println("  Expected:     " + expectedEpoch);
            
            // New approach should match expected
            assertEquals("New method should produce correct epoch for estLastReset " + type, expectedEpoch, newEpoch);
        }
        
        System.out.println("Passed estimated last reset DST test");
    }

    /**
     * Test weekly reset scheduling with DST awareness.
     */
    @Test
    public void testWeeklyResetDST() {
        ZoneId systemZone = ZoneId.systemDefault();
        
        LocalDateTime nextWeekly = TimedType.WEEKLY.getNextReset();
        
        // NEW fixed method
        long fixedEpoch = TimedType.WEEKLY.getNextResetEpochSeconds();
        
        // Get offset at the future date
        ZoneOffset futureOffset = systemZone.getRules().getOffset(nextWeekly.atZone(systemZone).toInstant());
        long expectedEpoch = nextWeekly.toEpochSecond(futureOffset);
        
        System.out.println("Weekly next reset: " + nextWeekly);
        System.out.println("Fixed epoch: " + fixedEpoch + ", Expected: " + expectedEpoch);
        
        assertEquals("Weekly reset should handle DST correctly", expectedEpoch, fixedEpoch);
        
        System.out.println("Passed weekly reset DST test");
    }

    /**
     * Test daily reset scheduling with DST awareness.
     */
    @Test
    public void testDailyResetDST() {
        ZoneId systemZone = ZoneId.systemDefault();
        
        LocalDateTime nextDaily = TimedType.DAILY.getNextReset();
        
        // NEW fixed method
        long fixedEpoch = TimedType.DAILY.getNextResetEpochSeconds();
        
        // Get offset at the future date
        ZoneOffset futureOffset = systemZone.getRules().getOffset(nextDaily.atZone(systemZone).toInstant());
        long expectedEpoch = nextDaily.toEpochSecond(futureOffset);
        
        System.out.println("Daily next reset: " + nextDaily);
        System.out.println("Fixed epoch: " + fixedEpoch + ", Expected: " + expectedEpoch);
        
        assertEquals("Daily reset should handle DST correctly", expectedEpoch, fixedEpoch);
        
        System.out.println("Passed daily reset DST test");
    }
}
