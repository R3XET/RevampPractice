package eu.revamp.practice.util;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("Duplicates")
public class DurationUtil {

    private static DecimalFormat decimalFormat = new DecimalFormat("00");

    public static String getDuration(long startTime) {
        int difference = (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);

        int h = difference / 3600;
        int s = difference % 60;
        int m = difference / 60 % 60;

        return (startTime > 0) ? (h > 0 ? decimalFormat.format(h) + ":" : "") + decimalFormat.format(m) + ":" + decimalFormat.format(s) : "00:00";
    }

    public static String getDuration(long startTime, long endTime) {
        int difference = (int) TimeUnit.MILLISECONDS.toSeconds(endTime - startTime);

        int h = difference / 3600;
        int s = difference % 60;
        int m = difference / 60 % 60;

        return (startTime > 0) ? (h > 0 ? decimalFormat.format(h) + ":" : "") + decimalFormat.format(m) + ":" + decimalFormat.format(s) : "00:00";
    }

    public static String getDurationDown(long start, int i) {
        long time = i - (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start));

        long seconds = time % 60;
        long minutes = time / 60 % 60;
        long hours = (time / 60) / 60;
        DecimalFormat decimalFormat = new DecimalFormat("00");

        String toReturn = decimalFormat.format(minutes) + ":" + decimalFormat.format(seconds);

        if (hours > 0) {
            toReturn = decimalFormat.format(hours) + ":" + toReturn;
        }
        return toReturn;
    }
}
