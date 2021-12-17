import Number.*
import com.soywiz.klogger.AnsiEscape
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import io.github.aakira.napier.Napier


fun Container.addBlock(block: Block) = block.addTo(this)
fun Container.removeBlock(block: Block) {
    Napier.d("Removing block")
    block.removeFromParent()
}

enum class BlockSelection () {
    UNSELECTED, NORMAL, BOMB, PATTERN;

    fun color (default: RGBA) =
        when (this){
            UNSELECTED -> default
            NORMAL -> Colors["#6a00b0"]
            BOMB -> Colors["#990a00"]
            PATTERN -> Colors["#fff170"]
        }
}

data class Block(val id: Int, var number: Number, var selection: BlockSelection = BlockSelection.UNSELECTED) : Container() {

    init {
        roundRect(cellSize, cellSize, 5, fill = number.color, stroke = selection.color(number.color), strokeThickness = 4.0)

        val textColor = when (number) {
            ZERO, ONE, TWO, FOUR -> Colors.BLACK
            else -> Colors.WHITE
        }
        text(number.display, textSizeFor(number), textColor, font).apply {
            centerBetween(0.0, 0.0, cellSize*1.0, cellSize*1.0)
        }
    }

    fun unselect (): Block {
        this.selection = BlockSelection.UNSELECTED
        return this
    }

    fun select (): Block {
        this.selection = BlockSelection.NORMAL
        return this
    }

    fun selectBomb (): Block {
        this.selection = BlockSelection.BOMB
        return this
    }

    fun copy (): Block {
        return Block(id, number, selection)
    }

    fun add (numberValue: Int): Block {
        this.number = findClosest(numberValue + number.value)
        return this
    }

    fun updateNumber (numberValue: Number): Block {
        this.number = numberValue
        return this
    }

    override fun equals(other: Any?): Boolean {
        return other is Block && this.id == other.id
    }

    override fun hashCode(): Int { return id }
}

private fun textSizeFor(number: Number) = when (number) {
    ZERO, ONE, TWO, THREE, FOUR, FIVE -> cellSize / 2.0
    SIX, SEVEN, EIGHT -> cellSize * 4 / 9.0
    NINE, TEN -> cellSize * 2 / 5.0
    ELEVEN, TWELVE, THIRTEEN, FOURTEEN, FIFTEEN, SIXTEEN, SEVENTEEN, EIGHTEEN, NINETEEN -> cellSize / 2.0
}

