class Logging(val level: LoggingLevel = LoggingLevel.Warn) {
    enum class LoggingLevel {
        Debug,
        Info,
        Warn,
        Error;

        companion object {
            fun max(level1: LoggingLevel, level2: LoggingLevel): LoggingLevel {
                if (level1 == Error || level2 == Error)
                    return Error
                else if (level1 == Warn || level2 == Warn)
                    return Warn
                else if (level1 == Info || level2 == Info)
                    return Info
                else return Debug
            }
        }
    }

    companion object {
        val DEBUG = LoggingLevel.Debug
        val INFO = LoggingLevel.Info
        val WARN = LoggingLevel.Warn
        val ERROR = LoggingLevel.Error

        fun getLogger(level: LoggingLevel = WARN): Logging {
            val logger = Logging(level)
            logger.info("Logger created with level '${levelToString(level)}' (${level}).")
            return logger
        }

        private fun levelToString(level: LoggingLevel): String {
            return level.toString().toLowerCase()
        }

        fun log(level: LoggingLevel, message: String) {
            val levelString = levelToString(level).toUpperCase()
            val formattedMessage = message.trim().replace("\n", "\n${levelString}: ")
            System.err.println("${levelString}: $formattedMessage")
        }
    }

    fun log(level: LoggingLevel, value: Any?) {
        if (level >= this.level)
            Logging.log(level, value.toString())
    }

    fun log(level: LoggingLevel, value: () -> Any?) {
        if (level >= this.level)
            Logging.log(level, value().toString())
    }

    fun debug(value: Any?) = log(DEBUG, value)
    fun info(value: Any?) = log(INFO, value)
    fun warn(value: Any?) = log(WARN, value)

    fun debug(value: () -> Any?) = log(DEBUG, value)
    fun info(value: () -> Any?) = log(INFO, value)
    fun warn(value: () -> Any?) = log(WARN, value)

    fun error(value: Any?): Nothing {
        val message = value.toString()
        Logging.log(ERROR, message)
        throw Exception(message).also {
            it.stackTrace = it.stackTrace.copyOfRange(1, it.stackTrace.size)
        }
    }

    fun error(value: Any?, newException: (String) -> Exception): Nothing {
        val message = value.toString()
        Logging.log(ERROR, message)
        throw newException(message).also {
            it.stackTrace = it.stackTrace.copyOfRange(1, it.stackTrace.size)
        }
    }

    fun getLogger(level: LoggingLevel) {
      Logging.getLogger(LoggingLevel.max(level, this.level))
    }
}

