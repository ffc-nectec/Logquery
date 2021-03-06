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

package ffc.airsync.hosdetail

import ffc.airsync.Dao
import ffc.airsync.MySqlJdbi
import ffc.airsync.extension
import ffc.airsync.getLogger

class HosDetailJdbi(
    val jdbiDao: Dao = MySqlJdbi(null)
) : HosDao {
    override fun get(): HashMap<String, String> {
        val currentOrganization = MySqlJdbi.dbConfig.currentOrganization
        val hosList = jdbiDao.extension<QueryHosDetail, List<HashMap<String, String>>> { get() }
        val configFile = MySqlJdbi.dbConfig.jhcisConfigFile
        return hosList.find {
            it["pcucode"] == currentOrganization
        } ?: hosList.find {
            val code = it["pcucode"]?.trim() ?: ""
            val condition = code.isNotEmpty() && (code != "0000x")
            if (condition)
                getLogger(this).warn("ใช้ Default $code เพราะค้นหารหัส pcucode ในไฟล์ $configFile ไม่พบ")
            condition
        } ?: throw Exception(
            "ไม่พบรหัส pcucode Debug: Config in file " +
                    "$configFile is $currentOrganization"
        )
    }
}
