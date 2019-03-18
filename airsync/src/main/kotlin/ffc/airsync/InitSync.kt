package ffc.airsync

import ffc.airsync.api.analyzer.analyzer
import ffc.airsync.api.analyzer.initSync
import ffc.airsync.api.chronic.Chronics
import ffc.airsync.api.genogram.initRelation
import ffc.airsync.api.genogram.relation
import ffc.airsync.api.healthcare.healthCare
import ffc.airsync.api.healthcare.initSync
import ffc.airsync.api.house.houses
import ffc.airsync.api.house.initSync
import ffc.airsync.api.person.gets
import ffc.airsync.api.person.initSync
import ffc.airsync.api.person.mapChronic
import ffc.airsync.api.person.persons
import ffc.airsync.api.template.TemplateInit
import ffc.airsync.api.user.initSync
import ffc.airsync.api.user.users
import ffc.airsync.api.village.initSync
import ffc.airsync.api.village.villages
import ffc.airsync.gui.ProgressList
import ffc.entity.Person

class InitSync : ProgressList {

    var progressTemplate = 0
    var progressUser = 0
    var progressVillage = 0
    var progressHouse = 0
    var progressPerson = 0
    var progressRelation = 0
    var progressHealthCare = 0
    var progressAnalyzer = 0

    val progressOrg: Int
        get() {
            val sum = progressTemplate +
                    progressUser +
                    progressVillage +
                    progressHouse +
                    progressPerson +
                    progressRelation +
                    progressHealthCare +
                    progressAnalyzer
            return sum / 8
        }

    fun init() {
        val person = Person().gets()
        progressTemplate = 5
        person.mapChronic(Chronics())
        progressTemplate = 10
        printDebug("ใส่ข้อมูล ช่วยกรอกอัตโนมัติ....")
        TemplateInit()
        progressTemplate = 100

        printDebug("ใส่ข้อมูลผู้ใช้ (1/7)")
        users.initSync()
        progressUser = 100

        printDebug("เข้าถึงหมู่บ้าน (2/7)")
        villages.initSync()
        progressVillage = 100

        printDebug("ดูบ้าน (3/7)")
        houses.initSync(person) { progressHouse = it }
        printDebug("ดูข้อมูลคน (4/7)")
        persons.initSync(houses, person) { progressPerson = it }
        printDebug("วิเคราะห์ความสัมพันธ์ (5/7)")
        relation.initRelation { progressRelation = it }
        printDebug("รวบรวมข้อมูลการให้บริการ 1 ปี... (6/7)")
        healthCare.initSync { progressHealthCare = it }
        printDebug("สำรวจความเจ็บป่วย (7/7)")
        analyzer.initSync(healthCare) { progressAnalyzer = it }
        printDebug("Finished push")
    }

    override fun get(): Map<String, Int> {
        val map = hashMapOf<String, Int>()
        map["ระบบช่วยกรอก"] = progressTemplate
        map["ข้อมูลผู้ใช้"] = progressUser
        map["หมู่บ้าน"] = progressVillage
        map["เพิ่มบ้านในระบบ"] = progressHouse
        map["เพิ่มคนในระบบ"] = progressPerson
        map["วิเคราะห์ความสัมพันธ์"] = progressRelation
        map["ข้อมูลการให้บริการ"] = progressHealthCare
        map["สำรวจความเจ็บป่วย"] = progressAnalyzer

        return map.toMap()
    }
}