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

package ffc.airsync.visit

import ffc.airsync.Dao
import ffc.airsync.MySqlJdbi
import ffc.airsync.db.DatabaseDao
import ffc.airsync.extension
import ffc.airsync.getLogger
import ffc.airsync.ncds.NCDscreenQuery
import ffc.airsync.specialpp.SpecialppQuery
import ffc.airsync.visit.NewVisitQuery.Lookup
import ffc.airsync.visit.fixBug.FixBugDuplicate
import ffc.entity.Link
import ffc.entity.Person
import ffc.entity.System
import ffc.entity.healthcare.Diagnosis
import ffc.entity.healthcare.HealthCareService
import ffc.entity.healthcare.HomeVisit
import ffc.entity.healthcare.Icd10
import ffc.entity.healthcare.NCDScreen
import ffc.entity.healthcare.SpecialPP
import ffc.entity.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jdbi.v3.core.statement.UnableToExecuteStatementException
import org.joda.time.LocalDate
import java.util.LinkedList
import java.util.Queue
import java.util.SortedMap
import kotlin.system.measureTimeMillis

class VisitJdbi(
    val jdbiDao: Dao = MySqlJdbi(null)
) : VisitDao {
    private val logger by lazy { getLogger(this) }
    override fun createHomeVisit(
        homeVisit: HomeVisit,
        healthCareService: HealthCareService,
        pcucode: String,
        pcucodePerson: String,
        patient: Person,
        username: String
    ): HealthCareService {

        val visitNum = getMaxVisit() + 1
        try {
            val rightcode = (patient.link?.keys?.get("rightcode")) as String?
            val rightno = (patient.link?.keys?.get("rightno")) as String?
            val hosmain = (patient.link?.keys?.get("hosmain")) as String?
            val hossub = (patient.link?.keys?.get("hossub")) as String?
            val visitData = healthCareService.buildInsertData(
                pcucode,
                visitNum,
                pcucodePerson,
                ((patient.link?.keys?.get("pid")) as String).toLong(),
                username,
                rightcode,
                rightno,
                hosmain,
                hossub
            )

            if (healthCareService.link == null) {
                getLogger(this).info("Create link because new HealthCareService ${healthCareService.id} from cloud.")
                healthCareService.link = Link(System.JHICS)
            }

            insertVisit(visitData)

            val insertDiagData = healthCareService.buildInsertDiag(pcucode, visitNum, username)
            jdbiDao.extension<VisitDiagQuery, Unit> {
                insertVisitDiag(insertDiagData)
            }

            val visitIndividualData =
                homeVisit.buildInsertIndividualData(healthCareService, pcucode, visitNum, username)
            jdbiDao.extension<HomeVisitIndividualQuery, Unit> { insertVitsitIndividual(visitIndividualData) }

            healthCareService.link!!.keys["pcucode"] = pcucode
            healthCareService.link!!.keys["visitno"] = visitNum.toString()

            ((patient.link?.keys?.get("pid")) as String?)?.let {
                healthCareService.link!!.keys["pid"] = it
            }
            rightcode?.let { healthCareService.link!!.keys["rightcode"] = it }
            rightno?.let { healthCareService.link!!.keys["rightno"] = it }
            hosmain?.let { healthCareService.link!!.keys["hosmain"] = it }
            hossub?.let { healthCareService.link!!.keys["hossub"] = it }
            healthCareService.link!!.isSynced = true

            return healthCareService
        } catch (ex: UnableToExecuteStatementException) {
            val message = ex.message ?: ""
            /*if (message.startsWith("com.mysql.jdbc.MysqlDataTruncation: Data truncation: Data too long")) {

            }*/
            logger.warn("กำลังแก้ไขปัญหาที่เกิดขึ้น")
            val fixBug = FixBugDuplicate(jdbiDao, visitNum)
            fixBug.fix()
            throw ex
        }
    }

    override fun updateHomeVisit(
        homeVisit: HomeVisit,
        healthCareService: HealthCareService,
        pcucode: String,
        pcucodePerson: String,
        patient: Person,
        username: String
    ): HealthCareService {

        val visitNum = healthCareService.link!!.keys["visitno"].toString().toLong()
        val rightcode = (patient.link?.keys?.get("rightcode")) as String?
        val rightno = (patient.link?.keys?.get("rightno")) as String?
        val hosmain = (patient.link?.keys?.get("hosmain")) as String?
        val hossub = (patient.link?.keys?.get("hossub")) as String?
        val visitData = healthCareService.buildInsertData(
            pcucode,
            visitNum,
            pcucodePerson,
            ((patient.link?.keys?.get("pid")) as String).toLong(),
            username,
            rightcode,
            rightno,
            hosmain,
            hossub
        )

        val updateResult = jdbiDao.extension<VisitQuery, Number> { updateVisit(visitData) }
        check(updateResult == 1) { "เกิดการ update มากกว่า 1 แถว visitno=${visitData.visitno}" }

        healthCareService.buildInsertDiag(pcucode, visitNum, username).forEach {
            jdbiDao.extension<VisitDiagQuery, Unit> {
                updateVisitDiag(it)
            }
        }

        val visitIndividualData = homeVisit.buildInsertIndividualData(healthCareService, pcucode, visitNum, username)
        jdbiDao.extension<HomeVisitIndividualQuery, Unit> { updateVitsitIndividual(visitIndividualData) }

        healthCareService.link!!.keys["pcucode"] = pcucode
        healthCareService.link!!.keys["visitno"] = visitNum.toString()

        ((patient.link?.keys?.get("pid")) as String?)?.let {
            healthCareService.link!!.keys["pid"] = it
        }
        rightcode?.let { healthCareService.link!!.keys["rightcode"] = it }
        rightno?.let { healthCareService.link!!.keys["rightno"] = it }
        hosmain?.let { healthCareService.link!!.keys["hosmain"] = it }
        hossub?.let { healthCareService.link!!.keys["hossub"] = it }
        healthCareService.link!!.isSynced = true

        return healthCareService
    }

    override fun getMaxVisit(): Long {
        val listMaxVisit = jdbiDao.extension<VisitQuery, List<Long>> { getMaxVisitNumber() }
        return listMaxVisit.last()
    }

    fun insertVisit(insertData: InsertData) {
        val listVisitData = arrayListOf<InsertData>().apply {
            add(insertData)
        }
        jdbiDao.extension<VisitQuery, Unit> { insertVisit(listVisitData) }
    }

    override fun getHealthCareService(
        whereString: String,
        progressCallback: (Int) -> Unit,
        lookup: () -> DatabaseDao.LookupHealthCareService
    ): List<HealthCareService> {
        var i = 0
        val result = NewVisitQuery(jdbiDao)
            .get(whereString) {
                object : Lookup {
                    override fun patientId(pcuCode: String, pid: String): String? {
                        return lookup().lookupPatientId(pcuCode, pid)
                    }

                    override fun providerId(username: String): String? {
                        return lookup().lookupProviderId(username)
                    }
                }
            }
        val size = result.size
        val avgTimeRun: Queue<Long> = LinkedList()
        var sumTime = 0L
        val sugarLabQuery = SugarLabQuery(jdbiDao)

        return result.mapNotNull { healthCare ->

            if (healthCare.patientId.isBlank()) {
                logger.warn { "ค้นหา Person ไม่พบ ${healthCare.patientId}" }
                return@mapNotNull null
            }
            if (healthCare.providerId.isBlank()) {
                logger.warn { "ค้นหา User ไม่พบ ${healthCare.providerId}" }
                return@mapNotNull null
            }

            var runtimeQueryDb: Long = -1L
            val allRunTime = measureTimeMillis {
                i++
                healthCare.link?.keys?.get("visitno")?.toString()?.toLong()?.let { visitNumber ->

                    runtimeQueryDb = measureTimeMillis {
                        runBlocking {
                            launch {
                                healthCare.diagnosises = getDiagnosisIcd10(
                                    getVisitDiag(visitNumber)
                                ) { icd10 -> lookup().lookupDisease(icd10) }
                            }
                            launch {
                                getSpecialPP(visitNumber).forEach {
                                    healthCare.addSpecialPP(
                                        lookup().lookupSpecialPP(it.trim()) ?: SpecialPP.PPType(it, it)
                                    )
                                }
                            }
                            launch {
                                healthCare.ncdScreen = getNcdScreen(visitNumber).firstOrNull()?.let {
                                    createNcdScreen(healthCare.providerId, healthCare.patientId, it)
                                }
                            }
                            launch {
                                getHomeVisit(visitNumber).firstOrNull()?.let { visit ->
                                    visit.bundle["dateappoint"]?.let { healthCare.nextAppoint = it as LocalDate }
                                    healthCare.communityServices.add(
                                        HomeVisit(
                                            serviceType = lookup().lookupServiceType(visit.serviceType.id.trim())
                                                ?: visit.serviceType,
                                            detail = visit.detail,
                                            plan = visit.plan,
                                            result = visit.result
                                        )
                                    )
                                }
                            }
                            launch {
                                sugarLabQuery.get(healthCare.pcuCode(), healthCare.visitNumber().toInt())?.let {
                                    healthCare.sugarLab = it
                                }
                            }
                        }
                    }
                }
            }

            if (avgTimeRun.size > 10000)
                sumTime -= avgTimeRun.poll()
            avgTimeRun.offer(allRunTime)
            sumTime += allRunTime

            val avgTime = sumTime / avgTimeRun.size

            if (i % 200 == 0 || i == size) {
                progressCallback(((i * 45) / size) + 5)
                var message = "Visit $i:$size"
                message += "\tRuntime:$runtimeQueryDb"
                message += "\tAllTime:$allRunTime"
                message += ((size - i) * avgTime).printTime()
                logger.debug(message)
            }

            healthCare
        }
    }

    private fun createNcdScreen(
        providerId: String,
        patientId: String,
        ncdScreen: NCDScreen
    ): NCDScreen {
        return NCDScreen(
            providerId = providerId,
            patientId = patientId,
            hasDmInFamily = ncdScreen.hasDmInFamily,
            hasHtInFamily = ncdScreen.hasHtInFamily,
            smoke = ncdScreen.smoke,
            alcohol = ncdScreen.alcohol,
            bloodSugar = ncdScreen.bloodSugar,
            weight = ncdScreen.weight,
            height = ncdScreen.height,
            waist = ncdScreen.waist,
            bloodPressure = ncdScreen.bloodPressure,
            bloodPressure2nd = ncdScreen.bloodPressure2nd
        ).update(ncdScreen.timestamp) {
            time = ncdScreen.time
            endTime = ncdScreen.endTime
            location = ncdScreen.location
            link = ncdScreen.link
        }
    }

    private fun getDiagnosisIcd10(
        diagnosisIcd10: List<Diagnosis>,
        lookupDisease: (icd10: String) -> Icd10?
    ): MutableList<Diagnosis> {
        return diagnosisIcd10.map {
            Diagnosis(
                disease = lookupDisease(it.disease.id.trim()) ?: it.disease,
                dxType = it.dxType,
                isContinued = it.isContinued
            )
        }.toMutableList()
    }

    private fun getVisitDiag(visitNumber: Long) =
        jdbiDao.extension<VisitDiagQuery, List<Diagnosis>> { getDiag(visitNumber) }

    private fun getVisitDiagCache(lookupDisease: (icd10: String) -> Icd10?): SortedMap<Long, List<Diagnosis>> {
        return NewVisitDiagQuery(jdbiDao) {
            object : NewVisitDiagQuery.Lookup {
                override fun lookupIcd10(icd10: String): Icd10 {
                    return lookupDisease(icd10.trim()) ?: Icd10(icd10, icd10)
                }
            }
        }.get()
    }

    private fun getHomeVisit(visitNumber: Long): List<HomeVisit> {
        return jdbiDao.extension<HomeVisitIndividualQuery, List<HomeVisit>> { getBy(visitNumber) }
    }

    private fun getNcdScreen(visitNumber: Long): List<NCDScreen> {
        return jdbiDao.extension<NCDscreenQuery, List<NCDScreen>> { getBy(visitNumber) }
    }

    private fun getSpecialPP(visitNumber: Long): List<String> {
        return jdbiDao.extension<SpecialppQuery, List<String>> { getBy(visitNumber) }
    }

    private fun getVisit(whereString: String): List<HealthCareService> {
        return if (whereString.isBlank())
            jdbiDao.extension<VisitQuery, List<HealthCareService>> { get() }
        else
            jdbiDao.extension<VisitQuery, List<HealthCareService>> { get(whereString) }
    }

    private fun Long.printTime(): String {
        if (this > 0) {
            val sec = (this / 1000) % 60
            val min = (this / 60000) % 60
            val hour = (this / 36e5).toInt()
            return ("\t$hour:$min:$sec")
        }
        return ""
    }

    private fun HealthCareService.pcuCode(): String = link!!.keys["pcucode"]?.toString()!!
    private fun HealthCareService.visitNumber(): String = link!!.keys["visitno"]?.toString()!!
}
