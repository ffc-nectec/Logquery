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

package ffc.airsync.client.client.module.retrofit

import ffc.model.Organization
import ffc.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthAirSync {
    @POST("/v0/org")
    fun regisOrg(@Body body: Organization): Call<Organization>




    @POST("/v0/org/{orgId}/user")
    fun regisUser(@Path("orgId") orgId: String, @Header("Authorization") authkey :String, @Body user : ArrayList<User>): Call<Any>

}
