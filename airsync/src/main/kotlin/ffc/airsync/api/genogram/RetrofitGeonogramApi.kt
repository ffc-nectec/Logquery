package ffc.airsync.api.genogram

import ffc.airsync.retrofit.RetofitApi
import ffc.airsync.utils.printDebug
import ffc.entity.Person

class RetrofitGeonogramApi : RetofitApi(), GeonogramApi {
    override fun put(personId: String, relationship: List<Person.Relationship>): List<Person.Relationship> {
        val relationLastUpdate = arrayListOf<Person.Relationship>()
        var syncccc = false
        var loop = 1
        while (!syncccc) {
            print("Sync rela loop ${loop++} ")
            val response = restService.updateRelationship(
                orgId = organization.id,
                authkey = tokenBarer,
                personId = personId,
                relationship = relationship
            ).execute()
            printDebug(" response ${response.code()} err:${response.errorBody()?.source()}")
            if (response.code() != 201) throw IllegalAccessException("Cannot Login ${response.code()}")
            relationLastUpdate.addAll(response.body() ?: arrayListOf())
            syncccc = true
        }
        return relationLastUpdate
    }
}