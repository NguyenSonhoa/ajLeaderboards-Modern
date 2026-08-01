package us.ajg0702.leaderboards.boards;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.Assert.*;

public class TimedTypeTest {

    @Test
    public void testHourlyGetNextReset() throws Exception {
        TimedType type = TimedType.HOURLY;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextReset = type.getNextReset();

        assertTrue("Next reset should be after now", nextReset.isAfter(now) || nextReset.isEqual(now));
        assertTrue("Next reset should be within 1 hour", nextReset.isBefore(now.plusHours(1)));
        assertEquals("Minutes should be 0", 0, nextReset.getMinute());
        assertEquals("Seconds should be 0", 0, nextReset.getSecond());
        assertEquals("Nanos should be 0", 0, nextReset.getNano());
    }

    @Test
    public void testHourlyGetEstimatedLastReset() throws Exception {
        TimedType type = TimedType.HOURLY;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastReset = type.getEstimatedLastReset();

        assertTrue("Last reset should be before or equal to now", lastReset.isBefore(now) || lastReset.isEqual(now));
        assertTrue("Last reset should be within current hour", lastReset.isAfter(now.minusHours(1)));
        assertEquals("Minutes should be 0", 0, lastReset.getMinute());
        assertEquals("Seconds should be 0", 0, lastReset.getSecond());
        assertEquals("Nanos should be 0", 0, lastReset.getNano());
    }

    @Test
    public void testDailyGetNextReset() throws Exception {
        TimedType type = TimedType.DAILY;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextReset = type.getNextReset();

        assertTrue("Next reset should be after now", nextReset.isAfter(now));
        assertEquals("Hour should be 0", 0, nextReset.getHour());
        assertEquals("Minute should be 0", 0, nextReset.getMinute());
        assertEquals("Second should be 0", 0, nextReset.getSecond());
        assertEquals("Nanos should be 0", 0, nextReset.getNano());
        assertTrue("Next reset should be within 25 hours", nextReset.isBefore(now.plusHours(25)));
    }

    @Test
    public void testDailyGetEstimatedLastReset() throws Exception {
        TimedType type = TimedType.DAILY;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastReset = type.getEstimatedLastReset();

        assertTrue("Last reset should be before or equal to now", lastReset.isBefore(now) || lastReset.isEqual(now));
        assertEquals("Hour should be 0", 0, lastReset.getHour());
        assertEquals("Minute should be 0", 0, lastReset.getMinute());
        assertEquals("Second should be 0", 0, lastReset.getSecond());
        assertEquals("Nanos should be 0", 0, lastReset.getNano());
    }

    @Test
    public void testWeeklyGetNextReset() throws Exception {
        TimedType type = TimedType.WEEKLY;
        TimedType.setWeeklyResetDay(java.time.DayOfWeek.SUNDAY);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextReset = type.getNextReset();

        assertEquals("Day of week should be Sunday", java.time.DayOfWeek.SUNDAY, nextReset.getDayOfWeek());
        assertEquals("Hour should be 0", 0, nextReset.getHour());
        assertEquals("Minute should be 0", 0, nextReset.getMinute());
        assertEquals("Second should be 0", 0, nextReset.getSecond());
        assertTrue("Next reset should be after now", nextReset.isAfter(now) || nextReset.isEqual(now));
    }

    @Test
    public void testWeeklyGetEstimatedLastReset() throws Exception {
        TimedType type = TimedType.WEEKLY;
        TimedType.setWeeklyResetDay(java.time.DayOfWeek.SUNDAY);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastReset = type.getEstimatedLastReset();

        assertEquals("Day of week should be Sunday", java.time.DayOfWeek.SUNDAY, lastReset.getDayOfWeek());
        assertEquals("Hour should be 0", 0, lastReset.getHour());
        assertEquals("Minute should be 0", 0, lastReset.getMinute());
        assertEquals("Second should be 0", 0, lastReset.getSecond());
        assertTrue("Last reset should be before or equal to now", lastReset.isBefore(now) || lastReset.isEqual(now));
    }

    @Test
    public void testMonthlyGetNextReset() throws Exception {
        TimedType type = TimedType.MONTHLY;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextReset = type.getNextReset();

        assertEquals("Day of month should be 1", 1, nextReset.getDayOfMonth());
        assertEquals("Hour should be 0", 0, nextReset.getHour());
        assertEquals("Minute should be 0", 0, nextReset.getMinute());
        assertEquals("Second should be 0", 0, nextReset.getSecond());
        assertEquals("Nanos should be 0", 0, nextReset.getNano());
        assertTrue("Next reset should be after now", nextReset.isAfter(now));
    }

    @Test
    public void testMonthlyGetEstimatedLastReset() throws Exception {
        TimedType type = TimedType.MONTHLY;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastReset = type.getEstimatedLastReset();

        assertEquals("Day of month should be 1", 1, lastReset.getDayOfMonth());
        assertEquals("Hour should be 0", 0, lastReset.getHour());
        assertEquals("Minute should be 0", 0, lastReset.getMinute());
        assertEquals("Second should be 0", 0, lastReset.getSecond());
        assertEquals("Nanos should be 0", 0, lastReset.getNano());
        assertTrue("Last reset should be before or equal to now", lastReset.isBefore(now) || lastReset.isEqual(now));
    }

    @Test
    public void testYearlyGetNextReset() throws Exception {
        TimedType type = TimedType.YEARLY;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextReset = type.getNextReset();

        assertEquals("Month should be January", 1, nextReset.getMonthValue());
        assertEquals("Day of month should be 1", 1, nextReset.getDayOfMonth());
        assertEquals("Hour should be 0", 0, nextReset.getHour());
        assertEquals("Minute should be 0", 0, nextReset.getMinute());
        assertEquals("Second should be 0", 0, nextReset.getSecond());
        assertEquals("Nanos should be 0", 0, nextReset.getNano());
        assertTrue("Next reset should be after now", nextReset.isAfter(now));
    }

    @Test
    public void testYearlyGetEstimatedLastReset() throws Exception {
        TimedType type = TimedType.YEARLY;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastReset = type.getEstimatedLastReset();

        assertEquals("Month should be January", 1, lastReset.getMonthValue());
        assertEquals("Day of month should be 1", 1, lastReset.getDayOfMonth());
        assertEquals("Hour should be 0", 0, lastReset.getHour());
        assertEquals("Minute should be 0", 0, lastReset.getMinute());
        assertEquals("Second should be 0", 0, lastReset.getSecond());
        assertEquals("Nanos should be 0", 0, lastReset.getNano());
        assertTrue("Last reset should be before or equal to now", lastReset.isBefore(now) || lastReset.isEqual(now));
    }

    @Test(expected = IllegalStateException.class)
    public void testAllTimeGetNextResetThrows() throws Exception {
        TimedType.ALLTIME.getNextReset();
    }

    @Test(expected = IllegalStateException.class)
    public void testAllTimeGetEstimatedLastResetThrows() throws Exception {
        TimedType.ALLTIME.getEstimatedLastReset();
    }

    @Test
    public void testGetNextResetEpochSecondsHourly() throws Exception {
        TimedType type = TimedType.HOURLY;
        ZoneId zone = ZoneId.systemDefault();
        long expected = type.getNextReset().atZone(zone).toEpochSecond();
        long actual = type.getNextResetEpochSeconds();
        assertEquals("Epoch seconds should match getNextReset() converted to epoch", expected, actual);
    }

    @Test
    public void testGetNextResetEpochSecondsDaily() throws Exception {
        TimedType type = TimedType.DAILY;
        ZoneId zone = ZoneId.systemDefault();
        long expected = type.getNextReset().atZone(zone).toEpochSecond();
        long actual = type.getNextResetEpochSeconds();
        assertEquals("Epoch seconds should match getNextReset() converted to epoch", expected, actual);
    }

    @Test
    public void testGetNextResetEpochSecondsWeekly() throws Exception {
        TimedType type = TimedType.WEEKLY;
        TimedType.setWeeklyResetDay(java.time.DayOfWeek.SUNDAY);
        ZoneId zone = ZoneId.systemDefault();
        long expected = type.getNextReset().atZone(zone).toEpochSecond();
        long actual = type.getNextResetEpochSeconds();
        assertEquals("Epoch seconds should match getNextReset() converted to epoch", expected, actual);
    }

    @Test
    public void testGetNextResetEpochSecondsMonthly() throws Exception {
        TimedType type = TimedType.MONTHLY;
        ZoneId zone = ZoneId.systemDefault();
        long expected = type.getNextReset().atZone(zone).toEpochSecond();
        long actual = type.getNextResetEpochSeconds();
        assertEquals("Epoch seconds should match getNextReset() converted to epoch", expected, actual);
    }

    @Test
    public void testGetNextResetEpochSecondsYearly() throws Exception {
        TimedType type = TimedType.YEARLY;
        ZoneId zone = ZoneId.systemDefault();
        long expected = type.getNextReset().atZone(zone).toEpochSecond();
        long actual = type.getNextResetEpochSeconds();
        assertEquals("Epoch seconds should match getNextReset() converted to epoch", expected, actual);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetNextResetEpochSecondsAllTimeThrows() throws Exception {
        TimedType.ALLTIME.getNextResetEpochSeconds();
    }

    @Test
    public void testGetEstimatedLastResetEpochSecondsHourly() throws Exception {
        TimedType type = TimedType.HOURLY;
        ZoneId zone = ZoneId.systemDefault();
        long expected = type.getEstimatedLastReset().atZone(zone).toEpochSecond();
        long actual = type.getEstimatedLastResetEpochSeconds();
        assertEquals("Epoch seconds should match getEstimatedLastReset() converted to epoch", expected, actual);
    }

    @Test
    public void testGetEstimatedLastResetEpochSecondsDaily() throws Exception {
        TimedType type = TimedType.DAILY;
        ZoneId zone = ZoneId.systemDefault();
        long expected = type.getEstimatedLastReset().atZone(zone).toEpochSecond();
        long actual = type.getEstimatedLastResetEpochSeconds();
        assertEquals("Epoch seconds should match getEstimatedLastReset() converted to epoch", expected, actual);
    }

    @Test
    public void testGetEstimatedLastResetEpochSecondsWeekly() throws Exception {
        TimedType type = TimedType.WEEKLY;
        TimedType.setWeeklyResetDay(java.time.DayOfWeek.SUNDAY);
        ZoneId zone = ZoneId.systemDefault();
        long expected = type.getEstimatedLastReset().atZone(zone).toEpochSecond();
        long actual = type.getEstimatedLastResetEpochSeconds();
        assertEquals("Epoch seconds should match getEstimatedLastReset() converted to epoch", expected, actual);
    }

    @Test
    public void testGetEstimatedLastResetEpochSecondsMonthly() throws Exception {
        TimedType type = TimedType.MONTHLY;
        ZoneId zone = ZoneId.systemDefault();
        long expected = type.getEstimatedLastReset().atZone(zone).toEpochSecond();
        long actual = type.getEstimatedLastResetEpochSeconds();
        assertEquals("Epoch seconds should match getEstimatedLastReset() converted to epoch", expected, actual);
    }

    @Test
    public void testGetEstimatedLastResetEpochSecondsYearly() throws Exception {
        TimedType type = TimedType.YEARLY;
        ZoneId zone = ZoneId.systemDefault();
        long expected = type.getEstimatedLastReset().atZone(zone).toEpochSecond();
        long actual = type.getEstimatedLastResetEpochSeconds();
        assertEquals("Epoch seconds should match getEstimatedLastReset() converted to epoch", expected, actual);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetEstimatedLastResetEpochSecondsAllTimeThrows() throws Exception {
        TimedType.ALLTIME.getEstimatedLastResetEpochSeconds();
    }
}
