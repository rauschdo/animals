package de.rauschdo.animals.utility

import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

fun LocalDateTime.toMillis() =
    toInstant(ZoneId.systemDefault().rules.getOffset(this))?.toEpochMilli() ?: 0L

object DateUtil {

    val defaultLocale = Locale.GERMAN

    val FORMAT_TIME = DateTimeFormatter.ofPattern("HH:mm", defaultLocale)

    private fun <R> tryOrNull(block: () -> R) = try {
        block()
    } catch (e: Exception) {
        null
    }

    /////////////
    // PARSERS //
    /////////////

    fun convertMillisecondsToLocalDateTime(ms: Long?): LocalDateTime =
        ms?.let {
            tryOrNull { Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDateTime() }
        } ?: LocalDateTime.now()

    fun convertToDateTime(dateString: String?, formatter: DateTimeFormatter) =
        tryOrNull { LocalDateTime.parse(dateString, formatter) }

    fun convertToDateTime(dateString: String?) =
        tryOrNull { LocalDateTime.parse(dateString) }

    ////////////////
    // FORMATTERS //
    ////////////////

    fun convertMillisecondsToString(ms: Long?, format: DateTimeFormatter) =
        ms?.let {
            convertDateTimeToString(convertMillisecondsToLocalDateTime(ms), format)
        }

    fun convertDateTimeToString(x: LocalDateTime?, format: DateTimeFormatter) =
        tryOrNull { x?.format(format) }
}
