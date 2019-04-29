package ffc.airsync.school

import ffc.airsync.MySqlJdbi
import ffc.airsync.extension
import ffc.entity.place.School

class SchoolJdbi(
    val jdbiDao: MySqlJdbi = MySqlJdbi(null)
) : QuerySchool {
    override fun get(): List<School> {
        return jdbiDao.extension<QuerySchool, List<School>> { get() }
    }
}
