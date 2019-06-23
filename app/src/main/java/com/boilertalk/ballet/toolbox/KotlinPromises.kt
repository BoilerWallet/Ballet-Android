package com.boilertalk.ballet.toolbox

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.android.UI
import kotlinx.coroutines.async
import java.lang.Exception

// Async awaits
suspend fun <T> CoroutineScope.await(block: () -> Deferred<T>): T = block().await()
suspend fun <T> CoroutineScope.awaitAsync(block: () -> T): T = async { block() }.await()

// Promise naming convention
typealias Promise<T> = Deferred<T>

// Promise extensions
fun <T, Y> Promise<T>.then(handler: (T) -> Y): Promise<Y> = async(UI) {
    val res = this@then.await()
    handler.invoke(res)
}

fun <T, Y> Promise<T>.thenAsync(handler: (T) -> Promise<Y>): Promise<Y> = async(UI) {
    val res = this@thenAsync.await()
    handler.invoke(res).await()
}

// Promise pseudo-constructor
object PromiseLike {

    fun <T> of(block: suspend CoroutineScope.() -> T): Promise<T> = async {
        block.invoke(this)
    }

    fun <T> of(deferred: Promise<T>): Promise<T> = deferred
}

fun <T> firstly(block: suspend CoroutineScope.() -> T): Promise<T> = async {
    block.invoke(this)
}

fun <T> firstly(block: () -> Promise<T>): Promise<T> = block.invoke()

// Catch, currently only possible as a final Promise chain element

fun <T> Promise<T>.catch(handler: (Exception) -> Unit) = async(UI) {
    try {
        this@catch.await()
    } catch (e: Exception) {
        handler.invoke(e)
    }
}
