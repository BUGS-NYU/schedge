class Logging(val level: Int = 3) {
    companion object {
        const val DEBUG = 1
        const val INFO = 2
        const val WARN = 3
        const val ERROR = 4

        var loggingLevel = INFO

        fun debug(value: Any?) {
            if (loggingLevel <= DEBUG) {
                System.err.println("DEBUG: ${(value ?: "").toString()}")
            }
        }

        fun info(value: Any?) {
            if (loggingLevel <= INFO) {
                System.err.println("INFO: ${(value ?: "").toString()}")
            }
        }

        fun warn(value: Any?) {
            if (loggingLevel <= WARN) {
                System.err.println("WARN: ${(value ?: "").toString()}")
            }
        }

        fun error(value: Any?) {
            if (loggingLevel <= ERROR) {
                System.err.println("ERROR: ${(value ?: "").toString()}")
            }
        }
    }

}

