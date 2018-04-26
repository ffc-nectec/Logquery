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

package ffc.airsync.api


import com.fatboyindustrial.gsonjodatime.Converters
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import ffc.model.Identity
import ffc.model.IdentityDeserializer
import me.piruin.geok.LatLng
import me.piruin.geok.gson.LatLngSerializer
import me.piruin.geok.gson.adapterFor

import javax.ws.rs.Consumes
import javax.ws.rs.Produces
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.ext.MessageBodyReader
import javax.ws.rs.ext.MessageBodyWriter
import javax.ws.rs.ext.Provider
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.lang.reflect.Type

@Provider
@Produces(MediaType.APPLICATION_JSON, "application/vnd.geo+json")
@Consumes(MediaType.APPLICATION_JSON, "application/vnd.geo+json")
class GsonJerseyProvider : MessageBodyWriter<Any>, MessageBodyReader<Any> {


    private val gson: Gson
        get() = Converters.registerAll(GsonBuilder())
          .adapterFor<LatLng>(LatLngSerializer())
          .adapterFor<Identity>(IdentityDeserializer())
          .create()

    override fun isReadable(type: Class<*>?, genericType: Type?, annotations: Array<out Annotation>?, mediaType: MediaType?): Boolean {
        return true
    }

    @Throws(IOException::class)
    override fun readFrom(type: Class<Any>, genericType: Type,
                          annotations: Array<Annotation>, mediaType: MediaType,
                          httpHeaders: MultivaluedMap<String, String>, entityStream: InputStream): Any? {
        try {
            InputStreamReader(entityStream, UTF_8).use { streamReader -> return gson.fromJson<Any>(streamReader, genericType) }
        } catch (e: com.google.gson.JsonSyntaxException) {
            // Log exception
        }

        return null
    }

    override fun isWriteable(type: Class<*>, genericType: Type,
                             annotations: Array<Annotation>, mediaType: MediaType): Boolean {
        return true
    }

    override fun getSize(`object`: Any, type: Class<*>, genericType: Type,
                         annotations: Array<Annotation>, mediaType: MediaType): Long {
        return -1
    }

    @Throws(IOException::class, WebApplicationException::class)
    override fun writeTo(`object`: Any, type: Class<*>, genericType: Type,
                         annotations: Array<Annotation>, mediaType: MediaType,
                         httpHeaders: MultivaluedMap<String, Any>,
                         entityStream: OutputStream) {
        OutputStreamWriter(entityStream, UTF_8).use { writer -> gson.toJson(`object`, genericType, writer) }
    }

    companion object {

        private val UTF_8 = "UTF-8"
    }
}
