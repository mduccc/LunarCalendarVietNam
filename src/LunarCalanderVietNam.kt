import java.lang.Math.PI
import jdk.nashorn.internal.runtime.PropertyMap.diff



class LunarCalendar{
    fun jdFromDate(dd: Int, mm: Int, yy: Int): Long {
        val a = Math.floor(((14 - mm) / 12).toDouble())
        val y = yy + 4800 - a
        val m = mm + 12 * a - 3
        var jd = dd.toDouble() + Math.floor((153 * m + 2) / 5) + 365 * y + Math.floor(y / 4) - Math.floor(y / 100) + Math.floor(y / 400) - 32045
        if (jd < 2299161) {
            jd = dd.toDouble() + Math.floor((153 * m + 2) / 5) + 365 * y + Math.floor(y / 4) - 32083
        }
        return jd.toLong()
    }

    fun getNewMoonDay(k: Double, timeZone: Float): Long{
        val deltat: Double
        val T = k/1236.85 // Time in Julian centuries from 1900 January 0.5
        val T2 = T * T
        val T3 = T2 * T
        val dr = PI/180
        var Jd1 = 2415020.75933 + 29.53058868*k + 0.0001178*T2 - 0.000000155*T3
        Jd1 = Jd1 + 0.00033*Math.sin((166.56 + 132.87*T - 0.009173*T2)*dr) // Mean new moon
        val M = 359.2242 + 29.10535608*k - 0.0000333*T2 - 0.00000347*T3 // Sun's mean anomaly
        val Mpr = 306.0253 + 385.81691806*k + 0.0107306*T2 + 0.00001236*T3 // Moon's mean anomaly
        val F = 21.2964 + 390.67050646*k - 0.0016528*T2 - 0.00000239*T3 // Moon's argument of latitude
        var C1=(0.1734 - 0.000393*T)*Math.sin(M*dr) + 0.0021*Math.sin(2*dr*M)
        C1 = C1 - 0.4068*Math.sin(Mpr*dr) + 0.0161*Math.sin(dr*2*Mpr)
        C1 = C1 - 0.0004*Math.sin(dr*3*Mpr)
        C1 = C1 + 0.0104*Math.sin(dr*2*F) - 0.0051*Math.sin(dr*(M+Mpr))
        C1 = C1 - 0.0074*Math.sin(dr*(M-Mpr)) + 0.0004*Math.sin(dr*(2*F+M))
        C1 = C1 - 0.0004*Math.sin(dr*(2*F-M)) - 0.0006*Math.sin(dr*(2*F+Mpr))
        C1 = C1 + 0.0010*Math.sin(dr*(2*F-Mpr)) + 0.0005*Math.sin(dr*(2*Mpr+M))
        if (T < -11) {
            deltat= 0.001 + 0.000839*T + 0.0002261*T2 - 0.00000845*T3 - 0.000000081*T*T3
        } else {
            deltat= -0.000278 + 0.000265*T + 0.000262*T2
        }
        val JdNew = Jd1 + C1 - deltat
        return Math.floor(JdNew + 0.5 + timeZone/24).toLong()
    }

    fun getSunLongitude(jdn: Long, timeZone: Float): Double {
        val T = (jdn - 2451545.5 - timeZone/24) / 36525 // Time in Julian centuries from 2000-01-01 12:00:00 GMT
        val T2 = T*T
        val dr = PI/180 // degree to radian
        val M = 357.52910 + 35999.05030*T - 0.0001559*T2 - 0.00000048*T*T2 // mean anomaly, degree
        val L0 = 280.46645 + 36000.76983*T + 0.0003032*T2 // mean longitude, degree
        var DL = (1.914600 - 0.004817*T - 0.000014*T2)*Math.sin(dr*M)
        DL = DL + (0.019993 - 0.000101*T)*Math.sin(dr*2*M) + 0.000290*Math.sin(dr*3*M)
        var L = L0 + DL // true longitude, degree
        L = L*dr
        L = L - PI*2*(Math.floor(L/(PI*2))) // Normalize to (0, 2*PI)
        return Math.floor(L / PI * 6)
    }

    fun getLunarMonth11(yy: Int,timeZone: Float): Long{
        val off = jdFromDate(31, 12, yy) - 2415021
        val k = Math.floor(off / 29.530588853)
        var nm = getNewMoonDay(k, timeZone)
        val sunLong = getSunLongitude(nm, timeZone) // sun longitude at local midnight
        if (sunLong >= 9) { nm = getNewMoonDay(k-1, timeZone)
        }
        return nm
    }

    fun getLeapMonthOffset(a11: Long, timeZone: Float): Int{
        val k = Math.floor((a11 - 2415021.076998695) / 29.530588853 + 0.5)
        var last: Double
        var i = 1 // We start with the month following lunar month 11
        var arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone)
        do {
            last = arc
            i++
            arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone)
        } while (arc != last && i < 14)
        return i - 1
    }

    fun convertSolar2Lunar(dd: Int, mm: Int, yy: Int, timeZone: Float){
        var lunarYear: Int
        val dayNumber = jdFromDate(dd, mm, yy)
        val k = Math.floor((dayNumber - 2415021.076998695) / 29.530588853)
        var monthStart = getNewMoonDay(k + 1, timeZone)

        if (monthStart > dayNumber) {
            monthStart = getNewMoonDay(k, timeZone)
        }
        var a11 = getLunarMonth11(yy, timeZone)
        var b11 = a11
        if (a11 >= monthStart) {
            lunarYear = yy
            a11 = getLunarMonth11(yy - 1, timeZone)
        } else {
            lunarYear = yy + 1
            b11 = getLunarMonth11(yy + 1, timeZone)
        }
        val lunarDay = dayNumber - monthStart + 1
        val diff = Math.floor(((monthStart - a11) / 29).toDouble())
        val lunarLeap: Int
        var lunarMonth = diff + 11
        if (b11 - a11 > 365) {
            val leapMonthDiff = getLeapMonthOffset(a11, timeZone)
            if (diff >= leapMonthDiff) {
                lunarMonth = diff + 10
                if (leapMonthDiff.toDouble() == diff) {
                    lunarLeap = 1
                }
            }
        }
        if (lunarMonth > 12) {
            lunarMonth = lunarMonth - 12
        }
        if (lunarMonth >= 11 && diff < 4) {
            lunarYear -= 1
        }
        println("{'lunarDay': '$lunarDay', 'lunarMonth': '${lunarMonth.toInt()}, 'lunarYear': '$lunarYear'}")
    }
}



fun main(args: Array<String>){
    val lunarCal =  LunarCalendar()
    lunarCal.convertSolar2Lunar(5,7,2018,7f)
}