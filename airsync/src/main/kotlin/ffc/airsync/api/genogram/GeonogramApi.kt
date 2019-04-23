package ffc.airsync.api.genogram

import ffc.entity.Person
import ffc.entity.Person.Relationship

interface GeonogramApi {
    fun put(personId: String, relationship: List<Relationship>): List<Person.Relationship>

    fun putBlock(
        relationship: Map<String, List<Person.Relationship>>,
        progressCallback: (Int) -> Unit
    ): Map<String, List<Person.Relationship>>
}
