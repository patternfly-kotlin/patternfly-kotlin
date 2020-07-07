package org.patternfly.showcase

import dev.fritz2.binding.SingleMountPoint
import dev.fritz2.dom.Tag
import dev.fritz2.routing.Route
import dev.fritz2.routing.Router
import dev.fritz2.routing.decodeURIComponent
import dev.fritz2.routing.encodeURIComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import kotlin.dom.clear

data class PlaceRequest(val token: String, val params: Map<String, String> = mapOf())

class PlaceRequestRoute(override val default: PlaceRequest) : Route<PlaceRequest> {

    override fun marshal(route: PlaceRequest): String = buildString {
        append(route.token)
        if (route.params.isNotEmpty()) {
            route.params
                .map { (key, value) -> "$key=${encodeURIComponent(value)}" }
                .joinTo(this, ";", ";")
        }
    }

    override fun unmarshal(hash: String): PlaceRequest {
        val token = hash.substringBefore(';')
        val params = hash.substringAfter(';', "")
            .split(",")
            .filter { it.isNotEmpty() }
            .associate {
                val (left, right) = it.split("=")
                left to decodeURIComponent(right)
            }
        return PlaceRequest(token, params)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PlaceManager(private val default: PlaceRequest, private val notFound: () -> Tag<HTMLElement>) :
    Router<PlaceRequest>(PlaceRequestRoute(default)) {

    internal var target: Element? = null
    internal var currentPresenter: Presenter<*>? = null

    fun <E : Element> manage(tag: Tag<E>) {
        target = tag.domNode
        routes
            .map { place ->
                val nonEmptyPlace = if (place.token.isEmpty()) default else place
                val safePlace = if (nonEmptyPlace.token in Presenter) nonEmptyPlace else default
                val presenter = Presenter.lookup<Presenter<View>>(safePlace.token)
                if (presenter != null) {
                    if (presenter !== currentPresenter) {
                        currentPresenter?.hide()
                    }
                    currentPresenter = presenter
                    presenter.prepareFromRequest(place)
                    presenter.view.elements
                } else {
                    console.error("No presenter found for $safePlace!")
                    listOf(notFound())
                }
            }
            .bind(this)
    }
}

internal fun Flow<List<Tag<HTMLElement>>>.bind(placeManager: PlaceManager) =
    PlaceManagerMountPoint(this, placeManager)

internal class PlaceManagerMountPoint(upstream: Flow<List<Tag<HTMLElement>>>, private val placeManager: PlaceManager) :
    SingleMountPoint<List<Tag<HTMLElement>>>(upstream) {

    override fun set(value: List<Tag<HTMLElement>>, last: List<Tag<HTMLElement>>?) {
        placeManager.target?.let {
            it.clear()
            value.forEach { tag -> it.appendChild(tag.domNode) }
        }
        placeManager.currentPresenter?.show()
    }
}
