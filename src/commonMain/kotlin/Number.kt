import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import kotlin.math.abs
import kotlin.math.max

val finalColor = Colors["#ffa96a"]

// enum class Number(name: String, val value: Int, val color: RGBA, val display: String) {
//     ZERO("Blue", 1, Colors["#fae3d9"], "1"),
//     ONE("Orange", 3, Colors["#bbded6"], "3"),
//     TWO("Brown", 9, Colors["#ffb6b9"], "9"),
//     THREE("Purple", 27, Colors["#61c0bf"], "27"),
//     FOUR("Yellow", 81, Colors["#ffd581"], "81"),
//     FIVE("Red", 243, Colors["#bf81ff"], "243"),
//     SIX("Green", 729, Colors["#837dff"], "729"),
//     SEVEN("White", 2187, Colors["#6927ff"], "2187"),
//     EIGHT("Black", 6561, Colors["#2aa9d2"], "6561"),
//     NINE("Purple Heart", 19683, Colors["#1874c3"], "19683"),
//     TEN("Brown Heart", 59049, Colors["#2931b3"], "59049"),
//     ELEVEN("White Heart", 177147, finalColor, "3^11"),
//     TWELVE("Yellow Heart", 531441, finalColor, "3^12"),
//     THIRTEEN("Black Heart", 1594323, finalColor, "3^13"),
//     FOURTEEN("Green Heart", 4782969, finalColor, "3^14"),
//     FIFTEEN("Blue Heart", 14348907, finalColor, "3^15"),
//     SIXTEEN("Red Heart", 43046721, finalColor, "3^16"),
//     SEVENTEEN("Panda", 129140163, finalColor, "3^17"),
//     EIGHTEEN("Melting Face", 387420489, finalColor, "3^18"),
//     NINETEEN("100 Emoji", 1162261467, finalColor, "3^19"),
//     ;

//     fun next() = values()[(ordinal + 1) % values().size]

//     fun previous() = values()[max((ordinal - 1), 0) % values().size]
// }
enum class Number(
    val blockName: String,
    val value: Int,
    val color: RGBA,
    val TextColor: RGBA,
    val display: String,
    val emoji: String
    ) {
    ZERO("Black", 1, Colors["#544148"], Colors["#FFFFFF"], "1", "\u2B1B"),
    ONE("Yellow", 3, Colors["#F4F1BB"], Colors["#150A19"], "3", "\uD83D\uDFE8"),
    TWO("Blue", 9, Colors["#9ECDD3"], Colors["#220D00"], "9", "\uD83D\uDFE6"),
    THREE("Green", 27, Colors["#0C6408"],  Colors["#FFFFFF"], "27", "\uD83D\uDFE9"),
    FOUR("Purple", 81, Colors["#8150C3"], Colors["#CFEBF1"], "81", "\uD83D\uDFEA"),
    FIVE("Red", 243, Colors["#A71D31"], Colors["#FFECCE"], "243", "\uD83D\uDFE5"),
    SIX("Orange", 729, Colors["#ED6A5A"], Colors["#011817"], "729", "\uD83D\uDFE7"),
    SEVEN("White", 2187, Colors["#FFFFFF"], Colors["#0A0810"], "2187", "\u2B1C"),
    EIGHT("Brown", 6561, Colors["#7B3F05"], Colors["#FFFFFF"], "6561", "\uD83D\uDFEB"),
    NINE("Purple Heart", 19683, Colors["#9B30FF"], Colors["#FFFFFF"], "19683", "\uD83D\uDC9C"),
    TEN("Red Heart", 59049, Colors["#CD5C5C"], Colors["#FFFFFF"], "59049", "\uD83E\uDD8B"),
    ELEVEN("White Heart", 177147, Colors["#F0F0F0"], Colors["#010101"], "3^11", "\uD83E\uDD0D"),
    TWELVE("Yellow Heart", 531441, Colors["#FFF9BA"], Colors["#000000"], "3^12", "\uD83D\uDC9B"),
    THIRTEEN("Black Heart", 1594323, Colors["#4A4A4A"], Colors["#FFFFFF"], "3^13", "\uD83D\uDC9A"),
    FOURTEEN("Green Heart", 4782969, Colors["#08A255"], Colors["#FFFFFF"], "3^14", "\uD83D\uDC9A"),
    FIFTEEN("Blue Heart", 14348907, Colors["#6495ED"], Colors["#FFFFFF"], "3^15", "\uD83D\uDC99"),
    SIXTEEN("Brown Heart", 43046721, Colors["#8B4513"], Colors["#FFFFFF"], "3^16", "\uD83D\uDC94"),
    SEVENTEEN("Panda", 129140163, Colors["#BEBEBE"], Colors["#FFFFFF"], "3^17", "\uD83D\uDC3C"),
    EIGHTEEN("Melting Face", 387420489, Colors["#FFDAB9"], Colors["#040811"], "3^18", "\uD83E\uDD75"),
    NINETEEN("100 Emoji", 1162261467, Colors["#FF4500"], Colors["#FFFFFF"], "3^19", "\uD83D\uDD1F"),
    ;

    fun next() = values()[(ordinal + 1) % values().size]
    fun previous() = values()[max((ordinal - 1), 0) % values().size]
}
fun findClosest(comparisonValue: Int) = Number.values().minByOrNull { number: Number -> abs(number.value - comparisonValue) }!!
fun findClosestRoundedUp(comparisonValue: Int) = Number.values().find { number: Number -> number.value > comparisonValue }!!



