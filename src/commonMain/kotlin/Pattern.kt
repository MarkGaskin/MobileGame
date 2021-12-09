import Number.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import io.github.aakira.napier.Napier

// http://pentolla.com/images/Pieces.png
enum class Pattern() {
    TRIPLE,
    O4,
    I4,
    L4,
    Z4,
    U5,
    L5,
    N5,
    P5,
    Z5,
    V5,
    W5,
    I5,
    I6;

    fun getSquareCount (): Int {
        return when (this) {
                    TRIPLE -> 3
                    O4,
                    I4,
                    L4,
                    Z4 -> 4
                    U5,
                    L5,
                    N5,
                    P5,
                    Z5,
                    V5,
                    W5,
                    I5 -> 5
                    I6 -> 6
                }
    }
}

fun determinePattern(positionList: MutableList<Position>): Pattern {
    if (positionList.size == 3) {
        return Pattern.TRIPLE
    }
    else if (positionList.size == 4) {
        if ((positionList.all { position -> positionList.filter { newPosition -> position.x == newPosition.x}.size == 2 }) &&
            (positionList.all { position -> positionList.filter { newPosition -> position.y == newPosition.y}.size == 2 })) {
            Napier.v("Pattern O4 found")
            return Pattern.O4
        }
        else if ((positionList.all { position -> positionList.filter { newPosition -> position.x == newPosition.x}.size == 4 }) ||
                 (positionList.all { position -> positionList.filter { newPosition -> position.y == newPosition.y}.size == 4 })) {
            Napier.v("Pattern I4 found")
            return Pattern.I4
        }
        else if ((positionList.all { position -> positionList.filter { newPosition -> position.x == newPosition.x}.size == 3 }) ||
                 (positionList.all { position -> positionList.filter { newPosition -> position.y == newPosition.y}.size == 3 })) {
            Napier.v("Pattern L4 found")
            return Pattern.L4
        }
        else
        {
            Napier.v("Pattern Z4 found")
            return Pattern.Z4
        }
    }
    else if (positionList.size == 5) {
        if ((positionList.all { position -> positionList.filter { newPosition -> position.x == newPosition.x }.size == 5 }) ||
            (positionList.all { position -> positionList.filter { newPosition -> position.y == newPosition.y }.size == 5 })
        ) {
            Napier.v("Pattern I5 found")
            return Pattern.I5
        }
        else
        {
            Napier.v("Pattern U5 found")
            return Pattern.U5
        }

    }
    else if (positionList.size == 6) {
        if ((positionList.all { position -> positionList.filter { newPosition -> position.x == newPosition.x }.size == 6 }) ||
            (positionList.all { position -> positionList.filter { newPosition -> position.y == newPosition.y }.size == 6 })
        ) {
            Napier.v("Pattern I6 found")
            return Pattern.I6
        }
        else
        {
            Napier.v("Pattern U5 found")
            return Pattern.U5
        }

    }
    else
    {
        Napier.v("Pattern U5 found")
        return Pattern.U5
    }

}