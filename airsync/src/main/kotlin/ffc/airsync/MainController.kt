/*
 * Copyright (c) 2018 NECTEC
 *   National Electronics and Computer Technology Center, Thailand
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ffc.airsync

import ffc.airsync.api.Api
import ffc.airsync.api.ApiV1
import ffc.airsync.db.DatabaseDao
import ffc.airsync.provider.airSyncUiModule
import ffc.airsync.provider.notificationModule
import ffc.airsync.utils.printDebug
import ffc.entity.Chronic
import ffc.entity.Organization
import ffc.entity.Person
import ffc.entity.User
import ffc.entity.firebase.FirebaseToken
import ffc.entity.update
import java.util.UUID

class MainController(val org: Organization, val dao: DatabaseDao) {

    val api: Api by lazy { ApiV1() }

    fun run() {
        org.bundle.putAll(dao.getDetail())
        org.users.add(createOrgUser(org))

        val org = api.registerOrganization(org, Config.baseUrlRest)

        pushData(org)
        setupNotificationHandlerFor(org)
        startLocalAirSyncServer()
    }

    private fun createOrgUser(org: Organization): User {
        return User().apply {
            name = "airsync${org.bundle["hosId"]}"
            password = UUID.randomUUID().toString()
            role = User.Role.ORG
        }
    }

    private fun pushData(org: Organization) {
        val userList = dao.getUsers().toMutableList()
        userList.add(createAirSyncUser())

        val personOrgList = dao.getPerson()
        val chronicList = dao.getChronic()

        val personHaveChronic = personOrgList.mapChronics(chronicList)
        api.putUser(userList, org)
        api.putHouse(dao.getHouse(), org)
        api.putPerson(personHaveChronic, org)

        printDebug("Finish push")
    }

    private fun setupNotificationHandlerFor(org: Organization) {
        notificationModule().apply {
            onTokenChange { id ->
                api.putFirebaseToken(FirebaseToken(id), org)
            }
            onReceiveDataUpdate { type, id ->
                when (type) {
                    "House" -> api.getHouseAndUpdate(org, id, dao)
                }
            }
        }
    }

    private fun startLocalAirSyncServer() {
        airSyncUiModule().start()
    }

    fun List<Person>.mapChronics(chronics: List<Chronic>): List<Person> {
        forEach { person ->
            person.chronics.addAll(chronics.filter {
                it.link!!.keys["pid"] == person.link!!.keys["pid"]
            })
        }
        return this
    }

    fun createAirSyncUser(): User = User().update {
        name = "airsync.jhcis.pcu${org.id}"
    }
}
