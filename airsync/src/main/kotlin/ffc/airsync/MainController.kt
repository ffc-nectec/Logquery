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

import ffc.airsync.api.analyzer.analyzer
import ffc.airsync.api.analyzer.initSync2
import ffc.airsync.api.chronic.Chronics
import ffc.airsync.api.genogram.initRelation
import ffc.airsync.api.genogram.relation
import ffc.airsync.api.healthcare.healthCare
import ffc.airsync.api.healthcare.initSync
import ffc.airsync.api.house.houses
import ffc.airsync.api.house.initSync
import ffc.airsync.api.organization.LocalOrganization
import ffc.airsync.api.organization.orgApi
import ffc.airsync.api.person.gets
import ffc.airsync.api.person.initSync
import ffc.airsync.api.person.mapChronic
import ffc.airsync.api.person.persons
import ffc.airsync.api.template.TemplateInit
import ffc.airsync.api.user.initSync
import ffc.airsync.api.user.users
import ffc.airsync.api.village.initSync
import ffc.airsync.api.village.villages
import ffc.airsync.db.DatabaseDao
import ffc.airsync.provider.airSyncUiModule
import ffc.entity.Organization
import ffc.entity.Person
import ffc.entity.Token

class MainController(val dao: DatabaseDao) {

    private var property = LocalOrganization(dao, "ffcProperty.cnf")
    var everLogin: Boolean = false

    fun run() {
        val orgLocal = property.organization
        checkProperty(orgLocal)
        registerOrg(orgLocal)
        initSync()
        SetupAutoSync(dao)
        SetupNotification(dao)
        SetupDatabaseWatcher(dao)
        startLocalAirSyncServer()
    }

    private fun checkProperty(org: Organization) {
        val token = property.token
        if (token.isNotEmpty()) {
            everLogin = true
            val user = property.userOrg
            org.users.add(user)
            org.bundle["token"] = Token(user, property.token)
        }
    }

    private fun registerOrg(orgPropertyStore: Organization) {
        orgApi.registerOrganization(orgPropertyStore) { organization, token ->
            property.token = token.token
            property.orgId = organization.id
            property.userOrg = organization.users[0]
        }
    }

    private fun initSync() {
        val person = Person().gets()
        person.mapChronic(Chronics())

        printDebug("ใส่ข้อมูล ช่วยกรอกอัตโนมัติ....")
        TemplateInit()
        printDebug("ใส่ข้อมูลผู้ใช้ (1/7)")
        users.initSync()
        printDebug("เข้าถึงหมู่บ้าน (2/7)")
        villages.initSync()
        printDebug("ดูบ้าน (3/7)")
        houses.initSync(person)
        printDebug("ดูข้อมูลคน (4/7)")
        persons.initSync(houses, person)
        printDebug("วิเคราะห์ความสัมพันธ์ (5/7)")
        relation.initRelation()
        printDebug("รวบรวมข้อมูลการให้บริการ 1 ปี... (6/7)")
        healthCare.initSync {
            printDebug("รวบรวมข้อมูลการให้บริการ $it%")
        }
        printDebug("สำรวจความเจ็บป่วย (7/7)")
        analyzer.initSync2(healthCare)

        printDebug("Finished push")
    }

    private fun startLocalAirSyncServer() {
        airSyncUiModule().start()
    }
}
