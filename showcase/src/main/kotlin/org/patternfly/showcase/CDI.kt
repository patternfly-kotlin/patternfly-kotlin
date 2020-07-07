package org.patternfly.showcase

import dev.fritz2.dom.html.render
import org.patternfly.pfContent
import org.patternfly.pfSection

fun cdi(): CDI = CDIInstance

interface CDI {
    val placeManager: PlaceManager
}

internal object CDIInstance : CDI {
    init {
//        Presenter.register(Places.SERVER, ::ServerPresenter)
//        Presenter.register(Places.DEPLOYMENT, ::DeploymentPresenter)
//        Presenter.register(Places.MANAGEMENT, ::ManagementModelPresenter)
    }

    override val placeManager = PlaceManager(PlaceRequest(Places.SERVER)) {
        render {
            pfSection {
                pfContent {
                    h1 { +"Not Found" }
                    p { +"The requested page cannot be found!" }
                }
            }
        }
    }
}
