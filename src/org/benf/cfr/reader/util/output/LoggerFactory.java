/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.output;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.benf.cfr.reader.util.output.LogFormatter;

public class LoggerFactory {
    private static Handler handler = LoggerFactory.getHandler();
    private static Level level = Level.WARNING;

    public static void setGlobalLoggingLevel() {
        LoggerFactory.level = Level.FINEST;
    }

    private static Handler getHandler() {
        ConsoleHandler handler = new ConsoleHandler();
        LogFormatter formatter = new LogFormatter();
        handler.setFormatter(formatter);
        return handler;
    }

    public static <T> Logger create(Class<T> clazz) {
        Logger logger = Logger.getLogger(clazz.getName());
        logger.setUseParentHandlers(false);
        logger.addHandler(LoggerFactory.handler);
        logger.setLevel(LoggerFactory.level);
        return logger;
    }
}

