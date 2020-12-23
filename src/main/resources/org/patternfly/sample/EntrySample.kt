package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.DropdownStore
import org.patternfly.Notification
import org.patternfly.addTo
import org.patternfly.dropdown
import org.patternfly.fas
import org.patternfly.icon
import org.patternfly.item
import org.patternfly.items
import org.patternfly.textToggle
import org.patternfly.unwrap

internal interface EntrySample {

    fun addTo() {
        data class User(val id: String, val name: String, val age: Int)

        fun loadUser(): List<User> {
            // fetching users from backend
            return emptyList()
        }

        val store = DropdownStore<User>()
        render {
            dropdown(store) {
                textToggle { +"Please choose" }
            }
        }

        loadUser().addTo(store) { user ->
            icon = { icon("user".fas()) }
            description = "Description for ${user.name}"
            disabled = user.age < 18
        }
    }

    fun unwrap() {
        render {
            dropdown<String> {
                store.selects.unwrap() handledBy Notification.add {
                    info("You selected on $it")
                }
                textToggle { +"Text" }
                items {
                    item("Foo")
                    item("Bar")
                }
            }
        }
    }
}
