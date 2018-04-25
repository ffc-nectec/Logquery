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

import ffc.model.Address
import ffc.model.StorageOrg
import org.elasticsearch.cluster.metadata.AliasAction
import java.util.*

interface HouseDao {
    fun insert(orgUuid: UUID, house: Address)
    fun insert(orgUuid: UUID, houseList: List<Address>)

    fun update(orgUuid: UUID, house: Address)
    fun update(orgUuid: UUID, houseList: List<Address>)

    fun find(latlng: Boolean = true): List<StorageOrg<Address>>

    fun find(orgUuid: UUID, latlng: Boolean = true): List<StorageOrg<Address>>

    fun findByHouseId(orgUuid: UUID, hid: Int): StorageOrg<Address>?

    fun removeByOrgUuid(orgUuid: UUID)
}