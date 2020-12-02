package org.patternfly

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public interface HasItem<T> {
    public val item: T
}

public fun <T> Flow<List<HasItem<T>>>.unwrap(): Flow<List<T>> = this.map { items -> items.map { it.item } }

public fun <T> Flow<HasItem<T>>.unwrap(): Flow<T> = this.map { it.item }

public fun <T> Flow<HasItem<T>?>.unwrapOrNull(): Flow<T?> = this.map { it?.item }

public fun <T> HasItem<T>.unwrap(): T = this.item