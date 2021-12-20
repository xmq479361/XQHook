package xmq.hook.gradle.util

import org.gradle.api.Project

class Logger {
    static ILogger logger
    static ILogger loggerDef = new LoggerImplDef()

    private static ILogger get() {
        if (logger == null)
            return loggerDef
        return logger
    }

    static void init(Project project) {
        logger = new Log4jImpl(project.logger)
    }

    static void d(String message) {
        get().debug(message.toString())
    }

    static void i(String message) {
        get().info(message.toString())
    }

    static void w(String message) {
        get().warn(message.toString())
    }

    static void e(String message) {
        get().error(message.toString())
    }

    interface ILogger {
        void debug(String message)

        void info(String message)

        void warn(String message)

        void error(String message)
    }

    static class Log4jImpl implements ILogger {
        Log4jImpl(org.slf4j.Logger logger) {
            this.logger = logger
        }
        org.slf4j.Logger logger

        @Override
        void debug(String message) {
            logger.debug(message)
        }

        @Override
        void info(String message) {
            logger.info(message)
        }

        @Override
        void warn(String message) {
            logger.warn(message)
        }

        @Override
        void error(String message) {
            logger.error(message)
        }
    }

    static class LoggerImplDef implements ILogger {
        LoggerImplDef(){}
        @Override
        void debug(String message) {
            System.out.println(message)
        }

        @Override
        void info(String message) {
            System.out.println(message)
        }

        @Override
        void warn(String message) {
            System.out.println(message)
        }

        @Override
        void error(String message) {
            System.err.println(message)

        }
    }
}