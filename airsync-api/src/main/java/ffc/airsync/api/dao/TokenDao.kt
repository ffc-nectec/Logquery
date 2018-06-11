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

import ffc.model.TokenMessage
import ffc.model.StorageOrg
import java.util.*

interface TokenDao {
    fun insert(token: UUID, uuid: UUID, user: String, orgId: String, type: TokenMessage.TYPEROLE = TokenMessage.TYPEROLE.NOAUTH): TokenMessage
    fun find(token: UUID): StorageOrg<TokenMessage>
    fun findByOrgUuid(orgUUID: UUID): List<StorageOrg<TokenMessage>>
    fun remove(token: UUID)
    fun updateFirebaseToken(token: UUID, firebaseToken: String)
    fun removeByOrgUuid(orgUUID: UUID)
}