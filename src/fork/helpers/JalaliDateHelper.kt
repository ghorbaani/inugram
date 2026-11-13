package desu.inugram.helpers

import desu.inugram.InuConfig
import java.util.Calendar

/**
 * Jalali (Persian / Shamsi) date formatting.
 *
 * LocaleController asks for the date part of every timestamp (chat date headers, chat list,
 * "last seen", message details, ...) through these functions first. They return null while the
 * setting is off, so the stock formatter is used instead. The time part is never touched.
 */
object JalaliDateHelper {
    private val MONTHS = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
    )
    private val GREGORIAN_DAYS_BEFORE_MONTH = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)

    private fun enabled(): Boolean = InuConfig.JALALI_DATES.value

    /** returns [year, month (1-12), day] in the Jalali calendar */
    @JvmStatic
    fun toJalali(gy: Int, gm: Int, gd: Int): IntArray {
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 +
            gd + GREGORIAN_DAYS_BEFORE_MONTH[gm - 1]
        var jy = -1595 + 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + days / 31
            jd = 1 + days % 31
        } else {
            jm = 7 + (days - 186) / 30
            jd = 1 + (days - 186) % 30
        }
        return intArrayOf(jy, jm, jd)
    }

    private fun parts(ms: Long): IntArray {
        val c = Calendar.getInstance()
        c.timeInMillis = ms
        return toJalali(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
    }

    private fun digits(s: String): String {
        if (!InuConfig.JALALI_PERSIAN_DIGITS.value) return s
        val sb = StringBuilder(s.length)
        for (ch in s) sb.append(if (ch in '0'..'9') ('\u06F0' + (ch - '0')) else ch)
        return sb.toString()
    }

    private fun two(n: Int): String = if (n < 10) "0$n" else n.toString()

    /** "21 آبان" */
    @JvmStatic
    fun dayMonth(ms: Long): String? {
        if (!enabled()) return null
        val p = parts(ms)
        return digits("${p[2]} ${MONTHS[p[1] - 1]}")
    }

    /** "21 آبان 1405" */
    @JvmStatic
    fun fullDate(ms: Long): String? {
        if (!enabled()) return null
        val p = parts(ms)
        return digits("${p[2]} ${MONTHS[p[1] - 1]} ${p[0]}")
    }

    /** "1405/08/21" */
    @JvmStatic
    fun numericDate(ms: Long): String? {
        if (!enabled()) return null
        val p = parts(ms)
        return digits("${p[0]}/${two(p[1])}/${two(p[2])}")
    }

    /** Jalali year as text, or the given Gregorian year while the setting is off */
    @JvmStatic
    fun yearOr(ms: Long, gregorianYear: Int): String {
        if (!enabled()) return gregorianYear.toString()
        return digits(parts(ms)[0].toString())
    }
}
