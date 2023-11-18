package com.arnyminerz.escalaralcoiaicomtat.backend.diagnostics

import io.sentry.Sentry
import io.sentry.SpanStatus

object Performance {
    /**
     * Uses the Sentry SDK to measure the performance on the operation inside [block].
     *
     * @param name The transaction name
     * @param operation The operation being performed
     *
     * @return The result of [block], or `null` if [Diagnostics] is not initialized.
     */
    inline fun <Result> measure(name: String, operation: String, block: PerformanceScope.() -> Result): Result? {
        if (!Diagnostics.isInitialized) return null

        val transaction = Sentry.startTransaction(name, operation)
        return try {
            block(
                object : PerformanceScope(transaction) {
                    override fun <ResultType> span(
                        operation: String,
                        block: PerformanceScope.() -> ResultType
                    ): ResultType {
                        val span = this.transaction.startChild(operation)
                        return try {
                            block()
                        } catch (exception: Exception) {
                            transaction.throwable = exception
                            transaction.status = SpanStatus.INTERNAL_ERROR
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
