package us.ajg0702.leaderboards;

import org.junit.Test;

import java.time.*;

public class TimeUtilsTest {

    @Test
    public void testDSTSpringForward() throws Exception {
        ZoneId zone = ZoneId.of("America/New_York");
        
        LocalDateTime beforeDST = LocalDateTime.of(2025, 3, 9, 1, 59, 0);
        ZonedDateTime zdtBefore = ZonedDateTime.of(beforeDST, zone);
        Instant beforeInstant = zdtBefore.toInstant();
        
        ZoneOffset offsetBefore = TimeUtils.convertToZoneOffset(zone);
        
        LocalDateTime afterDST = LocalDateTime.of(2025, 3, 9, 3, 0, 0);
        ZonedDateTime zdtAfter = ZonedDateTime.of(afterDST, zone);
        Instant afterInstant = zdtAfter.toInstant();
        
        ZoneOffset correctOffsetAfter = zone.getRules().getOffset(afterInstant);
        
        ZoneOffset wrongOffset = TimeUtils.convertToZoneOffset(zone);
        
        System.out.println("DST Spring-Forward Test:");
        System.out.println("  Current implementation offset: " + wrongOffset);
        System.out.println("  Offset for future instant: " + correctOffsetAfter);
        
        if (!wrongOffset.equals(correctOffsetAfter)) {
            System.out.println("  BUG CONFIRMED: Offset mismatch during DST spring-forward");
        }
    }

    @Test
    public void testDSTFallBack() throws Exception {
        ZoneId zone = ZoneId.of("America/New_York");
        
        LocalDateTime beforeFallBack = LocalDateTime.of(2025, 11, 2, 0, 59, 0);
        ZonedDateTime zdtBefore = ZonedDateTime.of(beforeFallBack, zone);
        Instant beforeInstant = zdtBefore.toInstant();
        
        ZoneOffset offsetBefore = TimeUtils.convertToZoneOffset(zone);
        
        LocalDateTime afterFallBack = LocalDateTime.of(2025, 11, 2, 2, 0, 0);
        ZonedDateTime zdtAfter = ZonedDateTime.of(afterFallBack, zone);
        Instant afterInstant = zdtAfter.toInstant();
        
        ZoneOffset correctOffsetAfter = zone.getRules().getOffset(afterInstant);
        
        System.out.println("DST Fall-Back Test:");
        System.out.println("  Current implementation offset: " + offsetBefore);
        System.out.println("  Offset for future instant after fall-back: " + correctOffsetAfter);
        
        if (!offsetBefore.equals(correctOffsetAfter)) {
            System.out.println("  BUG CONFIRMED: Offset mismatch during DST fall-back");
        }
    }

    @Test
    public void testOffsetDifferenceForFutureDates() throws Exception {
        ZoneId zone = ZoneId.of("America/New_York");
        
        ZoneOffset currentOffset = TimeUtils.convertToZoneOffset(zone);
        
        LocalDateTime futureTime = LocalDateTime.now(zone).plusMonths(6);
        ZonedDateTime futureZdt = ZonedDateTime.of(futureTime, zone);
        Instant futureInstant = futureZdt.toInstant();
        
        ZoneOffset futureOffset = zone.getRules().getOffset(futureInstant);
        
        System.out.println("Future Date Offset Test:");
        System.out.println("  Current implementation (Instant.now()): " + currentOffset);
        System.out.println("  Offset for future date (" + futureTime.toLocalDate() + "): " + futureOffset);
        
        if (!currentOffset.equals(futureOffset)) {
            System.out.println("  BUG CONFIRMED: Offset differs for future dates due to DST");
        } else {
            System.out.println("  No DST difference (may vary by current date)");
        }
    }

    @Test
    public void testCorrectOffsetWithSpecificInstant() throws Exception {
        ZoneId zone = ZoneId.of("America/New_York");
        
        LocalDateTime targetTime = LocalDateTime.of(2025, 7, 15, 12, 0, 0);
        ZonedDateTime targetZdt = ZonedDateTime.of(targetTime, zone);
        Instant targetInstant = targetZdt.toInstant();
        
        ZoneOffset correctOffset = zone.getRules().getOffset(targetInstant);
        
        System.out.println("Correct Offset Test:");
        System.out.println("  Target date: " + targetTime.toLocalDate());
        System.out.println("  Correct offset: " + correctOffset);
        
        ZoneOffset buggyOffset = TimeUtils.convertToZoneOffset(zone);
        System.out.println("  Buggy implementation offset: " + buggyOffset);
        
        if (!buggyOffset.equals(correctOffset)) {
            System.out.println("  BUG CONFIRMED: TimeUtils returns wrong offset");
        }
    }
}
