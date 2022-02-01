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
    UNSELECTED, SMALL, MEDIUM, LARGE, EXTRALARGE, BOMB, ROCKET, PATTERN;

    fun colorContent (number: Number) =
        when (this){
            UNSELECTED -> number.color
            SMALL -> number.next().color
            MEDIUM -> number.next().next().color
            LARGE -> Colors["#ca9dd7"]
            EXTRALARGE -> number.next().next().next().color
            BOMB -> Colors["#990a00"]
            ROCKET -> Colors["#d19feb"]
            PATTERN -> Colors["#37b1ee"]
        }

    fun colorBorder (number: Number) =
        when (this){
            UNSELECTED -> number.color
            SMALL -> number.next().color
            MEDIUM -> number.next().next().color
            LARGE -> Colors["#ca9dd7"]
            EXTRALARGE -> number.next().next().next().color
            BOMB -> Colors["#990a00"]
            ROCKET -> Colors["#d19feb"]
            PATTERN -> Colors["#37b1ee"]
        }
}

data class Block(val id: Int, var number: Number, var selection: BlockSelection = BlockSelection.UNSELECTED) : Container() {

    init {
        roundRect(cellSize, cellSize, 5, fill = number.color, stroke = selection.colorBorder(number), strokeThickness = 3.5)
        roundRect(cellSize, cellSize, 5, fill = selection.colorContent(number).withA(80),  )

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
        this.selection = BlockSelection.SMALL
        return this
    }

    fun isGenerallySelected (): Boolean{
        return when (this.selection){
            BlockSelection.PATTERN,
            BlockSelection.SMALL,
            BlockSelection.MEDIUM,
            BlockSelection.LARGE,
            BlockSelection.EXTRALARGE,
            BlockSelection.ROCKET -> true
            else -> false
        }
    }

    fun selectMedium (): Block {
        this.selection = BlockSelection.MEDIUM
        return this
    }

    fun selectLarge (): Block {
        this.selection = BlockSelection.LARGE
        return this
    }

    fun selectExtraLarge (): Block {
        this.selection = BlockSelection.EXTRALARGE
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

