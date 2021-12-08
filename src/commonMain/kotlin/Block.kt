import Number.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import io.github.aakira.napier.Napier

fun Container.addBlock(id: Int, number: Number, isSelected: Boolean = false) = Block(id, number, isSelected).addTo(this)
fun Container.addBlock(block: Block) = block.addTo(this)
fun Container.removeBlock(id: Int, number: Number, isSelected: Boolean = false) = this.removeChild(Block(id,number,isSelected))
fun Container.removeBlock(block: Block) {
    Napier.d("Removing block")
    block.removeFromParent()
}



data class Block(val id: Int, var number: Number, var isSelected: Boolean = false) : Container() {


    init {
        roundRect(cellSize, cellSize, 5, fill = number.color, stroke = if (isSelected) Colors["#6a00b0"] else number.color, strokeThickness = 4.0)

        val textColor = when (number) {
            ZERO, ONE, TWO, FOUR -> Colors.BLACK
            else -> Colors.WHITE
        }
        text(number.display, textSizeFor(number), textColor, font).apply {
            centerBetween(0.0, 0.0, cellSize*1.0, cellSize*1.0)
        }
    }

    fun unselect (): Block {
        this.isSelected = false
        return this
    }

    fun select (): Block {
        this.isSelected = true
        return this
    }

    fun copy (): Block {
        return Block(id, number, isSelected)
    }

    fun add (numberValue: Int): Block {
        this.number = findClosest(numberValue + number.value)
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