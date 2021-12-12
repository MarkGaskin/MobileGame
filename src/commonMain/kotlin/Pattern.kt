import Number.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import io.github.aakira.napier.Napier

// http://pentolla.com/images/Pieces.png
enum class Pattern() {
    UNDETERMINED,
    TRIPLE,
    O4,
    I4,
    I5,
    D6,
    I6,
    O9;

    fun getSquareCount (): Int {
        return when (this) {
                    UNDETERMINED -> 0
                    TRIPLE -> 3
                    O4,
                    I4,-> 4
                    I5 -> 5
                    D6,
                    I6 -> 6
                    O9 -> 9
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
        else
        {
            Napier.v("Non special 4 square pattern found")
            return Pattern.UNDETERMINED
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
            Napier.v("Non special 5 square pattern found")
            return Pattern.UNDETERMINED
        }
    }
    else if (positionList.size == 6) {
        if ((positionList.all { position -> positionList.filter { newPosition -> position.x == newPosition.x }.size == 6 }) ||
            (positionList.all { position -> positionList.filter { newPosition -> position.y == newPosition.y }.size == 6 })
        ) {
            Napier.v("Pattern I6 found")
            return Pattern.I6
        }
        else if ((positionList.all { position -> positionList.filter { newPosition -> position.x == newPosition.x }.size == 3
                                                 && positionList.filter { newPosition -> position.y == newPosition.y }.size == 2 } ) ||
                (positionList.all { position -> positionList.filter { newPosition -> position.x == newPosition.x }.size == 2
                                                && positionList.filter { newPosition -> position.y == newPosition.y }.size == 3 } )
        ) {
            Napier.v("Pattern D6 found")
            return Pattern.D6
        }
        else
        {
            Napier.v("Non special 6 square pattern found")
            return Pattern.UNDETERMINED
        }
    }
    else if (positionList.size == 8 || positionList.size == 9) {
        val xList = positionList.map { position -> position.x }
        val xMax = xList.maxOrNull() ?: 20
        val xMin = xList.minOrNull() ?: 0
        val xAvg = xList.average()
        val yList = positionList.map { position -> position.y }
        val yMax = yList.maxOrNull() ?: 20
        val yMin = yList.minOrNull() ?: 0
        val yAvg = yList.average()
        if ( yMax - yMin == 3 && xMax - xMin == 3 && xAvg == (xMax + xMin) / 2.0 && yAvg == (yMax + yMin) / 2.0){
            Napier.v("Pattern O9 found")
            return Pattern.O9
        }
        else
        {
            Napier.v("Non special 8 square pattern found")
            return Pattern.UNDETERMINED
        }
    }
    else
    {
        Napier.v("Boring pattern found")
        return Pattern.UNDETERMINED
    }

}