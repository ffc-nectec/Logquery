/*
 * Copyright (c) 2561 NECTEC
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

package ffc.airsync.api.dao

import ffc.model.People
import ffc.model.Person
import ffc.model.StorageOrg
import java.util.*

interface PersonDao {
    fun insert(orgUUID: UUID, person: Person)
    fun insert(orgUUID: UUID, personList: List<Person>)

    fun find(orgUuid: UUID): List<StorageOrg<Person>>


    fun remove(orgUuid: UUID)


    fun getPeopleInHouse(orgUUID: UUID, houseId: Int): ArrayList<People>?


    fun removeByOrgUuid(orgUUID: UUID)
}