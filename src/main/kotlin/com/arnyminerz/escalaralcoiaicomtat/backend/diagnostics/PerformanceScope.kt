package com.arnyminerz.escalaralcoiaicomtat.backend.diagnostics

import io.sentry.ITransaction

abstract class PerformanceScope(protected val transaction: ITransaction?) {
    abstract fun <ResultType> span(operation: String, block: PerformanceScope.() -> ResultType): ResultType
}
