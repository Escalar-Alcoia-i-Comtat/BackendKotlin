package diagnostics

import io.sentry.Sentry
import io.sentry.SpanStatus

object Performance {
    /**
     * Uses the Sentry SDK to measure the performance on the operation inside [block].
     * If [Diagnostics] is not initialized, [block] is still run, but no measurements will be made.
     *
     * @param name The transaction name
     * @param operation The operation being performed
     * @param block The block of code to be executed.
     *
     * @return The result of [block].
     */
    inline fun <Result> measure(name: String, operation: String, block: PerformanceScope.() -> Result): Result {
        if (!Diagnostics.isInitialized) {
            return block(
                object : PerformanceScope(null) {
                    override fun <ResultType> span(
                        operation: String,
                        block: PerformanceScope.() -> ResultType
                    ): ResultType = block()
                }
            )
        }

        val transaction = Sentry.startTransaction(name, operation)
        return try {
            block(
                object : PerformanceScope(transaction) {
                    override fun <ResultType> span(
                        operation: String,
                        block: PerformanceScope.() -> ResultType
                    ): ResultType {
                        val span = this.transaction!!.startChild(operation)
                        return try {
                            block()
                        } catch (@Suppress("TooGenericExceptionCaught") exception: Exception) {
                            span.throwable = exception
                            span.status = SpanStatus.INTERNAL_ERROR
                            throw exception
                        } finally {
                            span.finish()
                        }
                    }
                }
            )
        } catch (@Suppress("TooGenericExceptionCaught") exception: Exception) {
            transaction.throwable = exception
            transaction.status = SpanStatus.INTERNAL_ERROR
            throw exception
        } finally {
            transaction.finish()
        }
    }
}
