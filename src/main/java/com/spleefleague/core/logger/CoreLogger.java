package com.spleefleague.core.logger;

import com.spleefleague.core.Core;

import javax.annotation.Nullable;
import java.util.logging.Level;

/**
 * @author NickM13
 * @since 4/24/2020
 */
public class CoreLogger {

    public static void logError(String msg) {
        Core.getInstance().getLogger().log(Level.SEVERE, msg);
        for (StackTraceElement element : new Throwable().getStackTrace()) {
            Core.getInstance().getLogger().log(Level.SEVERE, element.toString());
        }
    }

    public static void logError(Throwable throwable) {
        Core.getInstance().getLogger().log(Level.SEVERE, null, throwable);
        for (StackTraceElement element : throwable.getStackTrace()) {
            Core.getInstance().getLogger().log(Level.SEVERE, element.toString());
        }
    }

    public static void logError(String msg, Throwable throwable) {
        Core.getInstance().getLogger().log(Level.SEVERE, msg, throwable);
        for (StackTraceElement element : throwable.getStackTrace()) {
            Core.getInstance().getLogger().log(Level.SEVERE, element.toString());
        }
    }

    public static void logWarning(@Nullable String msg) {
        Core.getInstance().getLogger().log(Level.WARNING, msg);
    }

    public static void logWarning(@Nullable String msg, @Nullable Throwable throwable) {
        if (throwable != null)
            Core.getInstance().getLogger().log(Level.WARNING, msg, throwable);
        else
            Core.getInstance().getLogger().log(Level.WARNING, msg);
    }

    public static void logInfo(@Nullable String msg) {
        Core.getInstance().getLogger().log(Level.INFO, msg);
    }

    public static void logInfo(@Nullable String msg, @Nullable Throwable throwable) {
        if (throwable != null)
            Core.getInstance().getLogger().log(Level.INFO, msg, throwable);
        else
            Core.getInstance().getLogger().log(Level.INFO, msg);
    }

}
