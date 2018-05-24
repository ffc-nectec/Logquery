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

import ffc.model.Organization
import ffc.model.User
import ffc.model.UserStor
import ffc.model.printDebug
import java.util.*
import javax.ws.rs.ForbiddenException


interface UserDao {

    companion object {
        val userBlock = arrayListOf<String>(
          "ADM",
          "adm",
          "newuser",
          "usr_db",
          "Drug_Store_Admin")


        fun checkBlockUser(user: User) {
            printDebug("Check block user.")
            val user = userBlock.find {
                it == user.username.trim()
            }
            printDebug("\tResult block check $user")
            if (user != null) throw ForbiddenException("User ไม่มีสิทธ์")
        }
    }

    fun insert(user: User, org: Organization)
    fun find(orgUuid: UUID): List<UserStor>
    fun findById(id: String): List<UserStor>
    fun removeAll(orgUuid: UUID)
    fun isAllow(user: User, orgUuid: UUID): Boolean
    fun isAllowById(user: User, id: String): Boolean
    fun removeByOrgUuid(orgUUID: UUID)

}