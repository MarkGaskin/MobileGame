import Number.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*

fun Container.block(number: Number) = Block(number).addTo(this)

class Block(val number: Number, var isSelected: Boolean = false) : Container() {

    init {
        roundRect(cellSize, cellSize, 5, fill = if (isSelected) RGBA(255, 34, 34) else number.color)

        val textColor = when (number) {
            ZERO, ONE -> Colors.BLACK
            else -> Colors.WHITE
        }
        text(number.display, textSizeFor(number), textColor, font).apply {
            centerBetween(0.0, 0.0, cellSize*1.0, cellSize*1.0)
        }
    }

    fun select (): Block {
        return Block(number, true)
    }
}

private fun textSizeFor(number: Number) = when (number) {
    ZERO, ONE, TWO, THREE, FOUR, FIVE -> cellSize / 2.0
    SIX, SEVEN, EIGHT -> cellSize * 4 / 9.0
    NINE, TEN -> cellSize * 2 / 5.0
    ELEVEN, TWELVE, THIRTEEN, FOURTEEN, FIFTEEN, SIXTEEN, SEVENTEEN, EIGHTEEN, NINETEEN -> cellSize / 2.0
}