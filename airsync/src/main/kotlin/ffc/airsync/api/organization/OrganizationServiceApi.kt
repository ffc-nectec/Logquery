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

package ffc.airsync.api.organization

import ffc.airsync.api.cloudweakup.RetofitWeakUp
import ffc.airsync.retrofit.RetofitApi
import ffc.airsync.utils.callApi
import ffc.airsync.utils.getLogger
import ffc.entity.Organization
import ffc.entity.Token
import ffc.entity.gson.toJson

class OrganizationServiceApi : RetofitApi<OrganizationService>(OrganizationService::class.java), OrganizationApi {
    private val logger by lazy { getLogger(this) }
    override fun registerOrganization(
        localOrganization: Organization,
        onSuccessRegister: (organization: Organization, token: Token) -> Unit
    ) {
        RetofitWeakUp().weakUp()
        if (isEverRegister(localOrganization)) {
            token = localOrganization.bundle["token"] as Token
            organization = localOrganization
        } else {

            organization = regisOrgToCloud(localOrganization)

            val user = localOrganization.users[0]
            val bodyLogin = hashMapOf<String, String>()
            bodyLogin["username"] = user.name
            bodyLogin["password"] = user.password
            logger.info("Organization login is ${bodyLogin["username"]}")
            val response = restService.loginOrg(organization.id, bodyLogin).execute()
            val tokenFromServer = response.body()
                ?: throw Exception(
                    "ไม่สามารถ Login org ได้ code:${response.code()} " +
                            "${response.errorBody()?.byteStream()?.reader()?.readLines()?.toJson()}"
                )
            logger.debug("\tToken = ${tokenFromServer.toJson()}")
            token = tokenFromServer
            organization.bundle["token"] = tokenFromServer

            logger.debug("Client update registerOrg from cloud ${organization.toJson()}")
        }
        onSuccessRegister(organization, token)
    }

    private fun isEverRegister(organization: Organization) = organization.bundle["token"] != null

    private fun regisOrgToCloud(organization: Organization): Organization {
        val response = restService.regisOrg(organization).execute()
        if (response.code() != 201) {
            throw Exception(
                "Code ${response.code()} " +
                        "Message ${response.errorBody()?.byteStream()?.reader()?.readLines()?.toJson()}"
            )
        }
        val restOrg: Organization? = response.body()

        return restOrg!!
    }

    override fun deleteOrganization(): Boolean {
        logger.info { "Call delete organization api." }
        return callApi {
            val response = restService.removeOrganization(
                organization.id, tokenBarer
            ).execute()
            val statusCode = response.code()
            restService.removeOrganizationStampName(organization.id, organization.name).execute()
            logger.info {
                "Delete organization response $statusCode"
            }
            statusCode == 200
        }
    }
}
