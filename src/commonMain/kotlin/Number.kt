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
    ZERO("Blue", 1, Colors["#7393B3"], Colors.WHITE, "1", "\uD83D\uDFE6"),
    ONE("Orange", 3, Colors["#FFA500"], Colors.WHITE, "3", "\uD83D\uDFE7"),
    TWO("Brown", 9, Colors["#A52A2A"], Colors.WHITE, "9", "\uD83D\uDFEB"),
    THREE("Purple", 27, Colors["#800080"], Colors.WHITE, "27", "\uD83D\uDFEA"),
    FOUR("Yellow", 81, Colors["#FFD700"], Colors.WHITE, "81", "\uD83D\uDFE8"),
    FIVE("Red", 243, Colors["#FF0000"], Colors.WHITE, "243", "\uD83D\uDFE5"),
    SIX("Green", 729, Colors["#008000"], Colors.WHITE, "729", "\uD83D\uDFE9"),
    SEVEN("White", 2187, Colors["#FFFFFF"], Colors.WHITE, "2187", "\u2B1C"),
    EIGHT("Black", 6561, Colors["#000000"], Colors.WHITE, "6561", "\u2B1B"),
    NINE("Purple Heart", 19683, Colors["#9B30FF"], Colors.WHITE, "19683", "\uD83D\uDC9C"),
    TEN("Brown Heart", 59049, Colors["#8B4513"], Colors.WHITE, "59049", "\uD83E\uDD8B"),
    ELEVEN("White Heart", 177147, Colors["#F0F0F0"], Colors.WHITE, "3^11", "\uD83E\uDD0D"),
    TWELVE("Yellow Heart", 531441, Colors["#FFF9BA"], Colors.WHITE, "3^12", "\uD83D\uDC9B"),
    THIRTEEN("Black Heart", 1594323, Colors["#4A4A4A"], Colors.WHITE, "3^13", "\uD83D\uDC9A"),
    FOURTEEN("Green Heart", 4782969, Colors["#00FF7F"], Colors.WHITE, "3^14", "\uD83D\uDC9A"),
    FIFTEEN("Blue Heart", 14348907, Colors["#6495ED"], Colors.WHITE, "3^15", "\uD83D\uDC99"),
    SIXTEEN("Red Heart", 43046721, Colors["#CD5C5C"], Colors.WHITE, "3^16", "\uD83D\uDC94"),
    SEVENTEEN("Panda", 129140163, Colors["#BEBEBE"], Colors.WHITE, "3^17", "\uD83D\uDC3C"),
    EIGHTEEN("Melting Face", 387420489, Colors["#FFDAB9"], Colors.WHITE, "3^18", "\uD83E\uDD75"),
    NINETEEN("100 Emoji", 1162261467, Colors["#FF4500"], Colors.WHITE, "3^19", "\uD83D\uDD1F"),
    ;

    fun next() = values()[(ordinal + 1) % values().size]
    fun previous() = values()[max((ordinal - 1), 0) % values().size]
}
fun findClosest(comparisonValue: Int) = Number.values().minByOrNull { number: Number -> abs(number.value - comparisonValue) }!!
fun findClosestRoundedUp(comparisonValue: Int) = Number.values().find { number: Number -> number.value > comparisonValue }!!



