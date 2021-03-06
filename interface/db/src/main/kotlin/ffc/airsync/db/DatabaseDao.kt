/*
 *
 *  * Copyright 2020 NECTEC
 *  *   National Electronics and Computer Technology Center, Thailand
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package ffc.airsync.db

import ffc.entity.Person
import ffc.entity.Template
import ffc.entity.User
import ffc.entity.Village
import ffc.entity.healthcare.CommunityService
import ffc.entity.healthcare.HealthCareService
import ffc.entity.healthcare.HomeVisit
import ffc.entity.healthcare.Icd10
import ffc.entity.healthcare.SpecialPP
import ffc.entity.place.Business
import ffc.entity.place.House
import ffc.entity.place.ReligiousPlace
import ffc.entity.place.School
import java.io.File

interface DatabaseDao {
    fun init()

    fun getDatabaseLocaion(): File

    fun getDetail(): HashMap<String, String>

    fun getUsers(): List<User>

    fun getPerson(lookupDisease: (icd10: String) -> Icd10): List<Person>

    fun findPerson(pcucode: String, pid: Long, lookupDisease: (icd10: String) -> Icd10): Person

    fun getHouse(lookupVillage: (jVillageId: String) -> Village?, whereString: String = ""): List<House>

    fun upateHouse(house: House)

    fun queryMaxVisit(): Long

    interface LookupHealthCareService {
        fun lookupPatientId(pcuCode: String, pid: String): String?
        fun lookupProviderId(name: String): String?
        fun lookupDisease(icd10: String): Icd10?
        fun lookupSpecialPP(ppCode: String): SpecialPP.PPType?
        fun lookupServiceType(serviceId: String): CommunityService.ServiceType?
    }

    /**
     * 3
     */
    fun getHealthCareService(
        whereString: String = "",
        progressCallback: (Int) -> Unit = {},
        lookup: () -> LookupHealthCareService
    ): List<HealthCareService>

    fun createHomeVisit(
        homeVisit: HomeVisit,
        healthCareService: HealthCareService,
        pcucode: String,
        pcucodePerson: String,
        patient: Person,
        username: String
    ): HealthCareService

    fun updateHomeVisit(
        homeVisit: HomeVisit,
        healthCareService: HealthCareService,
        pcucode: String,
        pcucodePerson: String,
        patient: Person,
        username: String
    ): HealthCareService

    fun getVillage(): List<Village>

    fun getBusiness(): List<Business>

    fun getSchool(): List<School>

    fun getTemple(): List<ReligiousPlace>

    fun getTemplate(): List<Template>
}
