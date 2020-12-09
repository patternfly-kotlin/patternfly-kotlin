package org.patternfly.dom

internal interface IdSamples {

    fun build() {
        Id.build("") // ""
        Id.build("-") // ""
        Id.build("!@#$%^") // ""
        Id.build("lorem", "", "", "ipsum", "") // "lorem-ipsum"
        Id.build("lorem-ipsum") // "lorem-ipsum"
        Id.build("Lorem Ipsum") // "lorem-ipsum"
        Id.build("Lorem", "Ipsum") // "lorem-ipsum"
        Id.build(" Lorem ", " Ipsum ") // "lorem-ipsum"
        Id.build("l0rem ip5um") // "l0rem-ip5um"
        Id.build("l0rem", "ip5um") // "l0rem-ip5um"
        Id.build(" l0rem ", " ip5um ") // "l0rem-ip5um"
        Id.build("""lorem §±!@#$%^&*()=_+[]{};'\:"|,./<>?`~ ipsum""") // "lorem-ipsum"
        Id.build("lorem", """§±!@#$%^&*()=_+[]{};'\:"|,./<>?`~""", "ipsum") // "lorem-ipsum"
    }
}