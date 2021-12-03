import com.soywiz.korim.color.RGBA

enum class Number(val value: Int, val color: RGBA, val display: String) {
    ZERO(1, RGBA(240, 228, 218), "1"),
    ONE(3, RGBA(236, 224, 201), "3"),
    TWO(9, RGBA(255, 178, 120), "9"),
    THREE(27, RGBA(254, 150, 92), "27"),
    FOUR(81, RGBA(247, 123, 97), "81"),
    FIVE(243, RGBA(235, 88, 55), "243"),
    SIX(729, RGBA(236, 220, 146), "729"),
    SEVEN(2187, RGBA(240, 212, 121), "2187"),
    EIGHT(6561, RGBA(244, 206, 96), "6561"),
    NINE(19683, RGBA(248, 200, 71), "19683"),
    TEN(59049, RGBA(256, 194, 46), "59049"),
    ELEVEN(177147, RGBA(104, 130, 249), "3^11"),
    TWELVE(531441, RGBA(51, 85, 247), "3^12"),
    THIRTEEN(1594323, RGBA(10, 47, 222), "3^13"),
    FOURTEEN(4782969, RGBA(9, 43, 202), "3^14"),
    FIFTEEN(14348907, RGBA(181, 37, 188), "3^15"),
    SIXTEEN(43046721, RGBA(166, 34, 172), "3^16"),
    SEVENTEEN(129140163, RGBA(166, 34, 172), "3^17"),
    EIGHTEEN(387420489, RGBA(166, 34, 172), "3^18"),
    NINETEEN(1162261467, RGBA(166, 34, 172), "3^19");

    fun next() = values()[(ordinal + 1) % values().size]
}