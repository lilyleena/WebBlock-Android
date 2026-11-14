package com.kshrd.core.api

/**
 * Where failed v2 calls are reported: method, path, status, problem code and the answer's `X-Request-Id`, so QA can
 * find the server's log line (docs/API-MAPPING.md section 1). The app points it at logcat in debug builds; it is
 * silent otherwise (and in unit tests). Never given a body, a header value other than the request id, or a token.
 */
object ApiLog {
    @Volatile
    var sink: (String) -> Unit = {}
}
