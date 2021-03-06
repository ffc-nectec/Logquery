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

package ffc.airsync

import ffc.airsync.api.analyzer.AnalyzerSyncApi
import ffc.airsync.api.analyzer.AnalyzerSyncServiceApi
import ffc.airsync.api.genogram.GeonogramApi
import ffc.airsync.api.genogram.GeonogramServiceApi
import ffc.airsync.api.healthcare.HealthCareApi
import ffc.airsync.api.healthcare.HealthCareServiceApi
import ffc.airsync.api.homehealthtype.HomeHealthTypeApi
import ffc.airsync.api.homehealthtype.HomeHealthTypeServiceApi
import ffc.airsync.api.house.HouseInterface
import ffc.airsync.api.house.HouseManage
import ffc.airsync.api.icd10.Icd10Api
import ffc.airsync.api.icd10.Icd10ServiceApi
import ffc.airsync.api.icd10.SpecialPpApi
import ffc.airsync.api.notification.NotificationApi
import ffc.airsync.api.notification.NotificationServiceApi
import ffc.airsync.api.organization.OrganizationApi
import ffc.airsync.api.organization.OrganizationServiceApi
import ffc.airsync.api.otp.OtpApi
import ffc.airsync.api.otp.OtpServiceApi
import ffc.airsync.api.person.PersonInterface
import ffc.airsync.api.person.PersonManage
import ffc.airsync.api.specialPP.SpecialPpServiceApi
import ffc.airsync.api.template.TemplateApi
import ffc.airsync.api.template.TemplateServiceApi
import ffc.airsync.api.user.UserInterface
import ffc.airsync.api.user.UserManage
import ffc.airsync.api.village.VILLAGELOOKUP
import ffc.airsync.api.village.VillageApi
import ffc.airsync.api.village.VillageServiceApi
import ffc.entity.Village
import ffc.entity.healthcare.HealthCareService
import ffc.entity.healthcare.Icd10

val userManage: UserInterface by lazy { UserManage() }
val houseManage: HouseInterface by lazy {
    HouseManage {
        object : HouseManage.Func {
            override fun villageLookup(villageCode: String): Village? {
                return VILLAGELOOKUP(villageCode)
            }
        }
    }
}
val personManage: PersonInterface by lazy {
    PersonManage {
        object : PersonManage.Func {
            override fun lookupDisease(icd10: String): Icd10 {
                return icd10Api.lookup(icd10)
            }
        }
    }
}
val otpApi: OtpApi by lazy { OtpServiceApi() }
val analyzerSyncApi: AnalyzerSyncApi by lazy { AnalyzerSyncServiceApi() }
val geonogramApi: GeonogramApi by lazy { GeonogramServiceApi() }
val healthCareApi: HealthCareApi by lazy { HealthCareServiceApi() }
val homeHealthTypeApi: HomeHealthTypeApi by lazy { HomeHealthTypeServiceApi() }
val icd10Api: Icd10Api by lazy { Icd10ServiceApi() }
val notificationApi: NotificationApi by lazy { NotificationServiceApi() }
val orgApi: OrganizationApi by lazy { OrganizationServiceApi() }
val specialPpApi: SpecialPpApi by lazy { SpecialPpServiceApi() }
val templateApi: TemplateApi by lazy { TemplateServiceApi() }
val villageApi: VillageApi by lazy { VillageServiceApi() }

val healthCare = arrayListOf<HealthCareService>()
val villages = arrayListOf<Village>()
