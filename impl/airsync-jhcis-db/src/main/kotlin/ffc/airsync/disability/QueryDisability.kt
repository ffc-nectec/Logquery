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

package ffc.airsync.disability

import ffc.entity.healthcare.Disability
import ffc.entity.healthcare.Disability.Cause
import ffc.entity.healthcare.Disability.Cause.DISEASED
import ffc.entity.healthcare.Disability.Cause.INBORN
import ffc.entity.healthcare.Disability.Cause.INJURED
import ffc.entity.healthcare.Disability.Cause.UNKNOWN
import ffc.entity.healthcare.Disability.Group
import ffc.entity.healthcare.Disability.Group.AUTISM
import ffc.entity.healthcare.Disability.Group.BLINDNESS
import ffc.entity.healthcare.Disability.Group.DEAFNESS
import ffc.entity.healthcare.Disability.Group.INTELLECTUAL
import ffc.entity.healthcare.Disability.Group.LEARNING
import ffc.entity.healthcare.Disability.Group.MENTAL
import ffc.entity.healthcare.Disability.Group.MOBILITY
import ffc.entity.healthcare.Icd10
import ffc.entity.healthcare.Severity
import ffc.entity.healthcare.Severity.MID
import ffc.entity.healthcare.Severity.OK
import ffc.entity.healthcare.Severity.UNDEFINED
import ffc.entity.healthcare.Severity.VERY_HI
import ffc.entity.util.generateTempId
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import org.jdbi.v3.sqlobject.config.RegisterRowMapper
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.sql.ResultSet

interface QueryDisability {
    @SqlQuery(
        """
SELECT
	personunable.pcucodeperson,
	personunable.pid,
	cpersonincomplete.incompletename,
	cpersonincomplete.incompletetype,
	personunable1type.unablelevel,
	personunable1type.datefound,
	personunable1type.disabcause,
	personunable1type.diagcode,
	personunable1type.datestartunable,
	personunable1type.dateupdate
FROM personunable
INNER JOIN personunable1type ON
	personunable.pcucodeperson=personunable1type.pcucodeperson
	AND
	personunable.pid=personunable1type.pid
INNER JOIN cpersonincomplete ON
	cpersonincomplete.incompletecode=personunable1type.typecode
    """
    )
    @RegisterRowMapper(DisabilityMapper::class)
    fun get(): List<DisabilityData>
}

data class DisabilityData(val pcuCode: String, val pid: String, val dis: Disability?)

internal class DisabilityMapper : RowMapper<DisabilityData> {
    /**
     * @return pcucode, pid to Disability
     */
    override fun map(rs: ResultSet, ctx: StatementContext): DisabilityData {
        val pcuCode = rs.getString("pcucodeperson")!!
        val pid = rs.getString("pid")!!
        val typeName = rs.getString("incompletename")!!

        /**
         * 1 การมองเห็น
         * 2 การได้ยิน
         * 3 ร่างกาย
         * 4 ทางจิต พฤติกรรม
         * 5 สติปัญญา
         * 6 ทางการเรียนรู้
         * 7 ออทิสติก
         */
        val group = rs.getString("incompletetype")?.preGroup()

        // 1-9
        val severity = rs.getString("unablelevel").preSeverity()
        // วันที่พบ
        val detectDate = DateTime(rs.getDate("datefound")).toLocalDate()
        // สาเหตุความพิการ
        val cause = rs.getString("disabcause").preCause()

        val disease = rs.getString("diagcode")?.preDisease()

        val start = DateTime(rs.getDate("datestartunable")).toLocalDate()

        val disabilityRun = kotlin.runCatching {
            Disability(
                group!!,
                detectDate ?: LocalDate.now(),
                start ?: detectDate ?: LocalDate.now(),
                cause,
                disease,
                severity
            )
        }

        return if (disabilityRun.isSuccess) {
            DisabilityData(pcuCode, pid, disabilityRun.getOrNull())
        } else {
            DisabilityData("", "", null)
        }
    }

    private fun String?.preCause(): Cause {
        return when (this?.trim()) {
            "1" -> INBORN
            "2" -> INJURED
            "3" -> DISEASED
            else -> UNKNOWN
        }
    }

    private fun String.preDisease(): Icd10 {
        return Icd10(generateTempId(), this.trim())
    }

    private fun String?.preSeverity(): Severity {
        return when (this?.trim()) {
            "1" -> OK
            "2" -> MID
            "3" -> VERY_HI
            else -> UNDEFINED
        }
    }

    private fun String.preGroup(): Group? {
        return when (trim()) {
            "1" -> BLINDNESS
            "2" -> DEAFNESS
            "3" -> MOBILITY
            "4" -> MENTAL
            "5" -> INTELLECTUAL
            "6" -> LEARNING
            "7" -> AUTISM
            else -> null
        }
    }
}
