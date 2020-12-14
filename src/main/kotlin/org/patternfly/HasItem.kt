package org.patternfly

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Common interface for things with an item / payload.
 */
public interface HasItem<T> {
    public val item: T
}

/**
 * Unwraps the payload from the specified [Flow].
 *
 * @receiver a flow with a list of [HasItem] instances
 */
public fun <T> Flow<List<HasItem<T>>>.unwrap(): Flow<List<T>> = this.map { items -> items.map { it.item } }

/**
 * Unwraps the payload from the specified [Flow].
 *
 * @receiver a flow with a [HasItem] instance
 */
public fun <T> Flow<HasItem<T>>.unwrap(): Flow<T> = this.map { it.item }

/**
 * Unwraps the payload from the specified [Flow].
 *
 * @receiver a flow with a [HasItem] instance
 */
public fun <T> Flow<HasItem<T>?>.unwrapOrNull(): Flow<T?> = this.map { it?.item }

/**
 * Unwraps the payload from the specified [HasItem] instance.
 *
 * @receiver a [HasItem] instance
 */
public fun <T> HasItem<T>.unwrap(): T = this.item
