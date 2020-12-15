package org.patternfly

import dev.fritz2.dom.Listener
import dev.fritz2.dom.WithDomNode
import dev.fritz2.dom.html.EventType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import org.w3c.dom.Element
import org.w3c.dom.events.Event

// Taken from dev.fritz2.dom.WithEvents.subscribe()
internal fun <T : Element, E : Event> subscribe(wdn: WithDomNode<T>?, type: EventType<E>): Listener<E, T> =
    if (wdn != null) {
        Listener(
            callbackFlow {
                val listener: (Event) -> Unit = {
                    offer(it.unsafeCast<E>())
                }
                wdn.domNode.addEventListener(type.name, listener)

                awaitClose { wdn.domNode.removeEventListener(type.name, listener) }
            }
        )
    } else {
        Listener(emptyFlow())
    }
