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

import ffc.model.StorageOrg
import ffc.model.printDebug
import java.util.*
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.NotFoundException

class InMemoryMobileTokenDao : MobileTokenDao {

    private constructor()

    companion object {
        val instant = InMemoryMobileTokenDao()
    }

    val tokenList = arrayListOf<StorageOrg<UUID>>()

    override fun removeByOrgUuid(orgUUID: UUID) {
        tokenList.removeIf { it.uuid == orgUUID }

    }

    override fun insert(token: UUID, uuid: UUID, user: String, id: Int) {
        //1 User per 1 Token
        tokenList.removeIf { it.uuid == uuid && it.user == user }
        tokenList.add(StorageOrg(
          uuid = uuid,
          data = token,
          user = user,
          id = id))

        printDebug("Token insert. Before add token")
        tokenList.forEach {
            printDebug(it)
        }

    }

    override fun find(token: UUID): StorageOrg<UUID> {
        val tokenObj = tokenList.find { it.data == token }
        if (tokenObj == null) throw NotAuthorizedException("Not Auth")
        return tokenObj
    }

    override fun remove(token: UUID) {
        tokenList.removeIf { it.data == token }
    }
}