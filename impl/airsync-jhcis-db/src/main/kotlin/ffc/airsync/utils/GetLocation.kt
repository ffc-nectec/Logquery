package ffc.airsync.utils

import ffc.airsync.getLogger
import me.piruin.geok.geometry.Point
import org.jdbi.v3.core.result.ResultSetException
import java.sql.ResultSet

private interface GetLocation

private val logger = getLogger(GetLocation::class)
fun getLocation(rs: ResultSet): Point? {
    val point = try {

        val xgis = stringToDouble(rs.getString("xgis")) ?: 0.0
        val ygis = stringToDouble(rs.getString("ygis")) ?: 0.0
        if ((xgis != 0.0) && (ygis != 0.0))
            if (xgis < ygis)
                Point(xgis, ygis)
            else
                Point(ygis, xgis)
        else
            null
    } catch (ex: ResultSetException) {
        logger.warn("xgix, ygis error", ex)
        null
    } catch (ex: AssertionError) {
        null
    }
    if (point != null) {
        val latitude = point.coordinates.latitude
        val longitude = point.coordinates.longitude
        if (latitude >= -90 && latitude <= 90) {
            if (longitude >= -180 && longitude <= 180)
                return point
        } else
            return null
    }
    return point
}

fun stringToDouble(str: String): Double? {
    val rex = Regex("""^(\d+\.\d+).*""")
    val filter = rex.matchEntire(str)?.groupValues
    return filter?.lastOrNull()?.toDoubleOrNull()
}
