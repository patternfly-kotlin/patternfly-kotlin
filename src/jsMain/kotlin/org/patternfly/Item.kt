package org.patternfly

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface HasItem<T> {
    val item: T
}

fun <T> Flow<List<HasItem<T>>>.unwrap(): Flow<List<T>> = this.map { items -> items.map { it.item } }

fun <T> Flow<HasItem<T>>.unwrap(): Flow<T> = this.map { it.item }

fun <T> Flow<HasItem<T>?>.unwrapOrNull(): Flow<T?> = this.map { it?.item }

