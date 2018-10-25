/*
 * Copyright (c) 2018 NECTEC
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

package ffc.airsync.db

import ffc.entity.Link
import ffc.entity.Person
import ffc.entity.System
import ffc.entity.ThaiCitizenId
import ffc.entity.update
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import org.jdbi.v3.sqlobject.config.RegisterRowMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.joda.time.LocalDate
import java.sql.ResultSet

interface QueryPerson {
    @SqlQuery(
        """
SELECT
	person.idcard,
	person.fname,
	person.lname,
	person.hcode,
	person.pcucodeperson,
	person.birth,
	person.pid,
	person.dischargetype,

	person.marystatus,
	cstatus.statusname,

	person.familyno,
	person.familyposition,
	cfamilyposition.famposname,

	person.father,
	person.fatherid,
	person.mother,
	person.motherid,
	person.mate,
	person.mateid,

	ctitle.titlename,
	`person`.`rightcode`,
	`person`.`rightno`,
	`person`.`hosmain`,
	`person`.`hossub`
FROM person
	LEFT JOIN ctitle ON
		person.prename=ctitle.titlecode
	LEFT JOIN cfamilyposition ON
		person.familyposition=cfamilyposition.famposcode
	LEFT JOIN cstatus ON
		person.marystatus=cstatus.statuscode
    """
    )   // WHERE hcode = 3890
    @RegisterRowMapper(PersonMapper::class)
    fun get(): List<Person>

    @SqlQuery(
        """
SELECT
	person.idcard,
	person.fname,
	person.lname,
	person.hcode,
	person.pcucodeperson,
	person.birth,
	person.pid,
	person.dischargetype,

	person.marystatus,
	cstatus.statusname,

	person.familyno,
	person.familyposition,
	cfamilyposition.famposname,

	person.father,
	person.fatherid,
	person.mother,
	person.motherid,
	person.mate,
	person.mateid,

	ctitle.titlename,
	`person`.`rightcode`,
	`person`.`rightno`,
	`person`.`hosmain`,
	`person`.`hossub`
FROM person
	LEFT JOIN ctitle ON
		person.prename=ctitle.titlecode
	LEFT JOIN cfamilyposition ON
		person.familyposition=cfamilyposition.famposcode
	LEFT JOIN cstatus ON
		person.marystatus=cstatus.statuscode

	WHERE
		`person`.`pcucodeperson` = :pcucode AND `person`.`pid`= :pid
LIMIT 1
    """
    )
    @RegisterRowMapper(PersonMapper::class)
    fun findPerson(@Bind("pcucode") pcucode: String, @Bind("pid") pid: Long): List<Person>
}

class PersonMapper : RowMapper<Person> {

    override fun map(rs: ResultSet?, ctx: StatementContext?): Person {
        if (rs == null) throw ClassNotFoundException()
        val statusLive = rs.getString("dischargetype")
        return Person().update {
            identities.add(ThaiCitizenId(rs.getString("idcard")))
            firstname = rs.getString("fname")
            lastname = rs.getString("lname")
            prename = rs.getString("titlename")
            birthDate = LocalDate.fromDateFields(rs.getDate("birth"))
            link = Link(
                System.JHICS,
                "pcucodeperson" to rs.getString("pcucodeperson"),
                "pid" to rs.getString("pid"),
                "hcode" to rs.getString("hcode"),
                "marystatus" to rs.getString("statusname"),
                "familyposition" to rs.getString("famposname"),

                "rightcode" to rs.getString("rightcode"),
                "rightno" to rs.getString("rightno"),
                "hosmain" to rs.getString("hosmain"),
                "hossub" to rs.getString("hossub")

            )
            bundle["fatherid"] = rs.getString("fatherid") ?: ""
            bundle["motherid"] = rs.getString("motherid") ?: ""

            bundle.forEach { key: String, value: Any ->
                if ((value as String).isBlank()) {
                    bundle.remove(key)
                }
            }

            link?.keys?.forEach { key, value ->
                if ((value as String).isBlank()) {
                    link?.keys?.remove(key)
                }
            }
        }
    }
}
