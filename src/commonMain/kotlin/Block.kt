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
    UNSELECTED, NORMAL, LARGE, BOMB, ROCKET, PATTERN;

    fun color (default: RGBA) =
        when (this){
            UNSELECTED -> default
            NORMAL -> Colors["#6a00b0"]
            LARGE -> Colors["#d19feb"]
            BOMB -> Colors["#990a00"]
            ROCKET -> Colors["#00bda7"]
            PATTERN -> Colors["#db8504"]
        }
}

data class Block(val id: Int, var number: Number, var selection: BlockSelection = BlockSelection.UNSELECTED) : Container() {

    init {
        roundRect(cellSize, cellSize, 5, fill = number.color, stroke = selection.color(number.color), strokeThickness = 3.0)
        roundRect(cellSize, cellSize, 5, fill = selection.color(number.color).withA(70),  )

        val textColor = when (number) {
            ZERO, ONE, TWO, FOUR -> Colors.BLACK
            else -> Colors.WHITE
        }
        text(number.display, textSizeFor(number), textColor, font).apply {
            when (number.ordinal) {
                0 -> centerBetween(0.0, 0.0, cellSize*1.0-4, cellSize*1.0)
                else -> centerBetween(0.0, 0.0, cellSize*1.0, cellSize*1.0)
            }

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

    fun isGenerallySelected (): Boolean{
        return (this.selection == BlockSelection.NORMAL || this.selection == BlockSelection.PATTERN || this.selection == BlockSelection.LARGE)
    }

    fun selectLarge (): Block {
        this.selection = BlockSelection.LARGE
        return this
    }

    fun selectBomb (): Block {
        this.selection = BlockSelection.BOMB
        return this
    }

    fun selectRocket (): Block {
        this.selection = BlockSelection.ROCKET
        return this
    }

    fun selectPattern (): Block {
        this.selection = BlockSelection.PATTERN
        return this
    }

    fun copy (): Block {
        return Block(id, number, selection)
    }

    fun copyToNextId (): Block {
        val id = nextBlockId
        nextBlockId++
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

