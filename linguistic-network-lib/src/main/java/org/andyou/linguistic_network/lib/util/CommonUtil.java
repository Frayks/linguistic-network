package org.andyou.linguistic_network.lib.util;

import org.apache.commons.lang3.time.DurationFormatUtils;

public class CommonUtil {

    public static String formatDuration(long duration) {
        return DurationFormatUtils.formatDuration(duration, "HH:mm:ss.SSS", true);
    }

}
