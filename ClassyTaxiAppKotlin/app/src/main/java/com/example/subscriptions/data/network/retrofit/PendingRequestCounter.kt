/*
 * Copyright 2021 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.subscriptions.data.network.retrofit

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicInteger

/**
 * Keep track of all pending network requests and set [SteFlow] "loading"
 * to true when there remaining pending requests and false when all requests have been responded to.
 *
 * SteFlow Object "loading" is used to show a progress bar in the UI.
 *
 * TODO(cassigbe@): Improve Pending requests count according to http/b/199924571.
 *
 */
class PendingRequestCounter {

    /**
     * Track the number of pending server requests.
     */
    private val pendingRequestCount = AtomicInteger()

    /**
     * True when there are pending network requests.
     */
    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    /**
     * Increment request count and update loading value.
     * Must plan on calling [.decrementRequestCount] when the request completes.
     *
     */
    suspend fun incrementRequestCount() {
        val newPendingRequestCount = pendingRequestCount.incrementAndGet()
        Log.i(TAG, "Pending Server Requests: $newPendingRequestCount")
        if (newPendingRequestCount <= 0) {
            Log.w(
                TAG, "Unexpectedly low request count after new request: "
                        + newPendingRequestCount
            )
            _loading.emit(false)
        } else {
            _loading.emit(true)
        }
    }

    /**
     * Decrement request count and update loading value.
     * Must call [.incrementRequestCount] each time a network call is made.
     * and call [.decrementRequestCount] when the server responds to the request.
     *
     */
    suspend fun decrementRequestCount() {
        val newPendingRequestCount = pendingRequestCount.decrementAndGet()
        Log.i(TAG, "Pending Server Requests: $newPendingRequestCount")
        if (newPendingRequestCount < 0) {
            Log.w(
                TAG, "Unexpectedly negative request count: "
                        + newPendingRequestCount
            )
            _loading.emit(false)
        } else if (newPendingRequestCount == 0) {
            _loading.emit(false)
        }
    }

    companion object {
        private const val TAG = "RequestCounter"
    }

}