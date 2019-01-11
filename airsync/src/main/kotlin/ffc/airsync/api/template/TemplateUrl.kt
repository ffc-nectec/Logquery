package ffc.airsync.api.template

import ffc.entity.Template
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface TemplateUrl {
    @POST("/v0/org/{orgId}/template")
    fun clearnAndCreate(
        @Path("orgId") orgId: String,
        @Header("Authorization") authkey: String,
        @Body template: List<Template>
    ): Call<Void>
}