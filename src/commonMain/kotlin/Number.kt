import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import kotlin.math.abs
import kotlin.math.max

val finalColor = Colors["#ffa96a"]

enum class Number(val value: Int, val color: RGBA, val display: String) {
    ZERO(1, Colors["#fae3d9"], "1"),
    ONE(3, Colors["#bbded6"], "3"),
    TWO(9, Colors["#ffb6b9"], "9"),
    THREE(27, Colors["#61c0bf"], "27"),
    FOUR(81, Colors["#ffd581"], "81"),
    FIVE(243, Colors["#bf81ff"], "243"),
    SIX(729, Colors["#837dff"], "729"),
    SEVEN(2187, Colors["#6927ff"], "2187"),
    EIGHT(6561, Colors["#2aa9d2"], "6561"),
    NINE(19683, Colors["#1874c3"], "19683"),
    TEN(59049, Colors["#2931b3"], "59049"),
    ELEVEN(177147, finalColor, "3^11"),
    TWELVE(531441, finalColor, "3^12"),
    THIRTEEN(1594323, finalColor, "3^13"),
    FOURTEEN(4782969, finalColor, "3^14"),
    FIFTEEN(14348907, finalColor, "3^15"),
    SIXTEEN(43046721, finalColor, "3^16"),
    SEVENTEEN(129140163, finalColor, "3^17"),
    EIGHTEEN(387420489, finalColor, "3^18"),
    NINETEEN(1162261467, finalColor, "3^19");

    fun next() = values()[(ordinal + 1) % values().size]

    fun previous() = values()[max((ordinal - 1),0) % values().size]

}


fun findClosest (comparisonValue: Int) = Number.values().minByOrNull { number -> abs(number.value - comparisonValue) }!!

fun findClosestRoundedUp (comparisonValue: Int) = Number.values().find { number -> number.value > comparisonValue }!!