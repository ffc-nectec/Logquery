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

package ffc.airsync.client.client

import ffc.airsync.client.client.module.ApiFactory
import ffc.airsync.client.client.module.PcuSocket
import ffc.airsync.client.client.module.PcuSocketAuthByToken
import ffc.model.Organization
import java.net.URI
import java.util.*

class MainContraller {


    var org = Organization(UUID.fromString(Config.pcuUuid),"-1", "520", "Nectec")


    fun main(args: Array<String>) {
        //get config
        //check my.ini
        //check log resume
        //check database connection


        //register central



        val messageCentral : CentralMessageManage = CentralMessageMaorgUpdatenageV1()

        org = messageCentral.registerOrganization(org, Config.baseUrlRest)

        val userList = ApiFactory().buildUserDao().findAll()

        println("Add put username org = " + org.token)
        messageCentral.putUser(userList,org)




        val socket = PcuSocketAuthByToken(object : PcuSocket.OnEventCallbackMessageListener {
            override fun EventCallBackMessage(message: String) {
                if (message == "X") {

                   // messageCentral.getData()



                } else {// Cannot X

                }
            }

        },org)




        socket.connect(URI.create(org.socketUrl))
        socket.join()


    }
}