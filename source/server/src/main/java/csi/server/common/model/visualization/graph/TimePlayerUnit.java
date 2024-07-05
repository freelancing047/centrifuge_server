package csi.server.common.model.visualization.graph;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum TimePlayerUnit implements IsSerializable {
    YEAR() {

        public long toMillis(long d) {
            return x(d, C10 / C2, MAX / (C10 / C2));
        }

        public long toSeconds(long d) {
            return x(d, C10 / C3, MAX / (C10 / C3));
        }

        public long toMinutes(long d) {
            return x(d, C10 / C4, MAX / (C10 / C4));
        }

        public long toHours(long d) {
            return x(d, C10 / C5, MAX / (C10 / C5));
        }

        public long toDays(long d) {
            return x(d, C10 / C6, MAX / (C10 / C6));
        }

        public long toWeeks(long d) {
            return x(d, C10 / C7, MAX / (C10 / C7));
        }

        public long toMonths(long d) {
            return x(d, C10 / C8, MAX / (C10 / C8));
        }

        public long toQuarters(long d) {
            return x(d, C10 / C9, MAX / (C10 / C9));
        }

        public long toYears(long d) {
            return d;
        }

        public long convert(long d, TimePlayerUnit u) {
            return u.toYears(d);
        }
    }, //
    QUARTER() {

        public long toMillis(long d) {
            return x(d, C9 / C2, MAX / (C9 / C2));
        }

        public long toSeconds(long d) {
            return x(d, C9 / C3, MAX / (C9 / C3));
        }

        public long toMinutes(long d) {
            return x(d, C9 / C4, MAX / (C9 / C4));
        }

        public long toHours(long d) {
            return x(d, C9 / C5, MAX / (C9 / C5));
        }

        public long toDays(long d) {
            return x(d, C9 / C6, MAX / (C9 / C6));
        }

        public long toWeeks(long d) {
            return x(d, C9 / C7, MAX / (C9 / C7));
        }

        public long toMonths(long d) {
            return x(d, C9 / C8, MAX / (C9 / C8));
        }

        public long toQuarters(long d) {
            return d;
        }

        public long toYears(long d) {
            return d / (C10 / C9);
        }

        public long convert(long d, TimePlayerUnit u) {
            return u.toQuarters(d);
        }
    }, //
    MONTH() {

        public long toMillis(long d) {
            return x(d, C8 / C2, MAX / (C8 / C2));
        }

        public long toSeconds(long d) {
            return x(d, C8 / C3, MAX / (C8 / C3));
        }

        public long toMinutes(long d) {
            return x(d, C8 / C4, MAX / (C8 / C4));
        }

        public long toHours(long d) {
            return x(d, C8 / C5, MAX / (C8 / C5));
        }

        public long toDays(long d) {
            return x(d, C8 / C6, MAX / (C8 / C6));
        }

        public long toWeeks(long d) {
            return x(d, C8 / C7, MAX / (C8 / C7));
        }

        public long toMonths(long d) {
            return d;
        }

        public long toQuarters(long d) {
            return d / (C9 / C8);
        }

        public long toYears(long d) {
            return d / (C10 / C8);
        }

        public long convert(long d, TimePlayerUnit u) {
            return u.toMonths(d);
        }
    }, //
    WEEK() {

        public long toMillis(long d) {
            return x(d, C7 / C2, MAX / (C7 / C2));
        }

        public long toSeconds(long d) {
            return x(d, C7 / C3, MAX / (C7 / C3));
        }

        public long toMinutes(long d) {
            return x(d, C7 / C4, MAX / (C7 / C4));
        }

        public long toHours(long d) {
            return x(d, C7 / C5, MAX / (C7 / C5));
        }

        public long toDays(long d) {
            return x(d, C7 / C6, MAX / (C7 / C6));
        }

        public long toWeeks(long d) {
            return d;
        }

        public long toMonths(long d) {
            return d / (C8 / C7);
        }

        public long toQuarters(long d) {
            return d / (C9 / C7);
        }

        public long toYears(long d) {
            return d / (C10 / C7);
        }

        public long convert(long d, TimePlayerUnit u) {
            return u.toWeeks(d);
        }
    }, //
    DAY() {

        public long toMillis(long d) {
            return x(d, C6 / C2, MAX / (C6 / C2));
        }

        public long toSeconds(long d) {
            return x(d, C6 / C3, MAX / (C6 / C3));
        }

        public long toMinutes(long d) {
            return x(d, C6 / C4, MAX / (C6 / C4));
        }

        public long toHours(long d) {
            return x(d, C6 / C5, MAX / (C6 / C5));
        }

        public long toDays(long d) {
            return d;
        }

        public long toWeeks(long d) {
            return d / (C7 / C6);
        }

        public long toMonths(long d) {
            return d / (C8 / C6);
        }

        public long toQuarters(long d) {
            return d / (C9 / C6);
        }

        public long toYears(long d) {
            return d / (C10 / C6);
        }

        public long convert(long d, TimePlayerUnit u) {
            return u.toDays(d);
        }
    }, //
    HOUR() {

        public long toMillis(long d) {
            return x(d, C5 / C2, MAX / (C5 / C2));
        }

        public long toSeconds(long d) {
            return x(d, C5 / C3, MAX / (C5 / C3));
        }

        public long toMinutes(long d) {
            return x(d, C5 / C4, MAX / (C5 / C4));
        }

        public long toHours(long d) {
            return d;
        }

        public long toDays(long d) {
            return d / (C6 / C5);
        }

        public long toWeeks(long d) {
            return d / (C7 / C5);
        }

        public long toMonths(long d) {
            return d / (C8 / C5);
        }

        public long toQuarters(long d) {
            return d / (C9 / C5);
        }

        public long toYears(long d) {
            return d / (C10 / C5);
        }

        public long convert(long d, TimePlayerUnit u) {
            return u.toHours(d);
        }

    }, //
    MINUTE() {

        public long toMillis(long d) {
            return x(d, C4 / C2, MAX / (C4 / C2));
        }

        public long toSeconds(long d) {
            return x(d, C4 / C3, MAX / (C4 / C3));
        }

        public long toMinutes(long d) {
            return d;
        }

        public long toHours(long d) {
            return d / (C5 / C4);
        }

        public long toDays(long d) {
            return d / (C6 / C4);
        }

        public long toWeeks(long d) {
            return d / (C7 / C4);
        }

        public long toMonths(long d) {
            return d / (C8 / C4);
        }

        public long toQuarters(long d) {
            return d / (C9 / C4);
        }

        public long toYears(long d) {
            return d / (C10 / C4);
        }

        public long convert(long d, TimePlayerUnit u) {
            return u.toMinutes(d);
        }

    }, //
    SECOND() {

        public long toMillis(long d) {
            return x(d, C3 / C2, MAX / (C3 / C2));
        }

        public long toSeconds(long d) {
            return d;
        }

        public long toMinutes(long d) {
            return d / (C4 / C3);
        }

        public long toHours(long d) {
            return d / (C5 / C3);
        }

        public long toDays(long d) {
            return d / (C6 / C3);
        }

        public long toWeeks(long d) {
            return d / (C7 / C3);
        }

        public long toMonths(long d) {
            return d / (C8 / C3);
        }

        public long toQuarters(long d) {
            return d / (C9 / C3);
        }

        public long toYears(long d) {
            return d / (C10 / C3);
        }

        public long convert(long d, TimePlayerUnit u) {
            return u.toSeconds(d);
        }

    }, //
    MILLISECOND() {

        public long toMillis(long d) {
            return d;
        }

        public long toSeconds(long d) {
            return d / (C3 / C2);
        }

        public long toMinutes(long d) {
            return d / (C4 / C2);
        }

        public long toHours(long d) {
            return d / (C5 / C2);
        }

        public long toDays(long d) {
            return d / (C6 / C2);
        }

        public long toWeeks(long d) {
            return d / (C7 / C2);
        }

        public long toMonths(long d) {
            return d / (C8 / C2);
        }

        public long toQuarters(long d) {
            return d / (C9 / C2);
        }

        public long toYears(long d) {
            return d / (C10 / C2);
        }

        public long convert(long d, TimePlayerUnit u) {
            return u.toMillis(d);
        }
    };

    TimePlayerUnit() {
    }

    private static long x(long d, long m, long over) {
        if (d > over)
            return Long.MAX_VALUE;
        if (d < -over)
            return Long.MIN_VALUE;
        return d * m;
    }

    private static final List<TimePlayerUnit> timePlayerUnitsSmallToLarge;

    static {
       timePlayerUnitsSmallToLarge = Arrays.asList(MILLISECOND, SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, QUARTER, YEAR);
    }

    // see java.util.concurrent.TimeUnit
    // Handy constants for conversion methods
    private static final long C0 = 1L;
    private static final long C1 = C0 * 1000L;
    private static final long C2 = C1 * 1000L;
    private static final long C3 = C2 * 1000L; // millis per second
    private static final long C4 = C3 * 60L; // seconds per minute
    private static final long C5 = C4 * 60L; // minutes per hour
    private static final long C6 = C5 * 24L; // hours per day
    private static final long C7 = C6 * 7L; // days per week
    private static final long C8 = (long) (C7 * 4.345); // weeks per month
    private static final long C9 = C8 * 3L; // months per quarter
    private static final long C10 = C9 * 4L; // quarters per year
    private static final long MAX = Long.MAX_VALUE;

    public abstract long convert(long sourceDuration, TimePlayerUnit sourceUnit);

    public abstract long toMillis(long duration);

    public abstract long toSeconds(long duration);

    public abstract long toMinutes(long duration);

    public abstract long toHours(long duration);

    public abstract long toDays(long duration);

    public abstract long toWeeks(long duration);

    public abstract long toMonths(long duration);

    public abstract long toQuarters(long duration);

    public abstract long toYears(long duration);

    public static List<TimePlayerUnit> timeUnitsSmallToLarge() {
        return timePlayerUnitsSmallToLarge;
    }
}
