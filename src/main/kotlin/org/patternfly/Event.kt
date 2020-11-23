package org.patternfly

import dev.fritz2.dom.Listener
import dev.fritz2.dom.WithDomNode
import dev.fritz2.dom.html.EventType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import org.w3c.dom.Element
import org.w3c.dom.events.Event

@OptIn(ExperimentalCoroutinesApi::class)
internal fun <T : Element, E : Event> subscribe(domNode: WithDomNode<T>?, type: EventType<E>): Listener<E, T> =
    if (domNode != null) {
        Listener(callbackFlow {
            val listener: (Event) -> Unit = {
                offer(it.unsafeCast<E>())
            }
            domNode.domNode.addEventListener(type.name, listener)

            awaitClose { domNode.domNode.removeEventListener(type.name, listener) }
        })
    } else {
        Listener(emptyFlow())
    }
