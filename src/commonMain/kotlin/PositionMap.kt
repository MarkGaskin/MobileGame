import com.soywiz.kds.*
import kotlin.random.*
import Block
import com.soywiz.korma.geom.Point

class Position(val x: Int, val y: Int){
    init {
        require(x >= 0)
        require(x < gridColumns)
        require(y >= 0)
        require(y < gridRows)
    }
}

enum class Direction {
    LEFT, RIGHT, TOP, BOTTOM
}

fun arrayInit (): Int {
    val random = Random.nextDouble()
    return if (random < 0.1)  3
            else if (random >= 0.1 && random < 0.2) 2
            else if (random >= 0.2 && random < 0.5) 1
            else 0
}

class PositionMap(private var array: IntArray = IntArray(gridColumns * gridRows) { arrayInit() }) {

    private fun getEnumId(position: Position) = array[position.x + position.y * gridRows]

    private fun getNumber(id: Int) = Number.values()[id]

    private fun getPosition(idx: Int) = Position(idx % gridColumns, idx / gridColumns)
    fun getIndex(position: Position) = position.x + position.y * gridColumns

    private fun tryAdjacentPositions(position:Position, direction: Direction) =
        try {
            when (direction) {
                Direction.LEFT -> Position(position.x - 1, position.y)
                Direction.RIGHT -> Position(position.x + 1, position.y)
                Direction.TOP -> Position(position.x, position.y + 1)
                Direction.BOTTOM ->  Position(position.x - 1, position.y - 1)
            }
        }
        catch (e: IllegalArgumentException) {
             null
        }

    private fun tryAllAdjacentPositions(position: Position) =
        Direction.values().mapNotNull{ direction -> tryAdjacentPositions(position, direction) }

    fun hasAvailableMoves(): Boolean {
        return array.foldIndexed(false, { idx, accumulated, _ -> accumulated || (hasTwoMatchingAdjacents(getPosition(idx))) })
    }

    private fun hasTwoMatchingAdjacents(position: Position) = getEnumId(position).let {
        (it != 0) &&
            tryAllAdjacentPositions(position)
                .map { adjPosition -> getEnumId(adjPosition) }
                .filter { adjEnumId -> adjEnumId ==  it}
                .count() >= 2
    }


    fun getAllEmptyPositions(): List<Position> {
        return array
                .mapIndexed{ idx, v -> if (v == 0) getPosition(idx) else null }
                .filterNotNull()
    }

    fun reInitializePositionMap () {
        this.array = IntArray(gridColumns * gridRows) { arrayInit() }
    }

    /*fun getNextNumber(): Number {
        Random.nextDouble()
    }*/



    fun forEach(action: (Int) -> Unit) { array.forEach(action) }

    override fun equals(other: Any?): Boolean {
        return (other is PositionMap) && this.array.contentEquals(other.array)
    }

    override fun hashCode() = array.hashCode()
    operator fun set(index: Int, value: Number) {
        array[index] = value.ordinal
    }

    fun getIndexedArray (): List<Pair<Position, Number>> {
        return array.mapIndexed {
                        idx, v ->
                            Pair(getPosition(idx), getNumber(v))
                    }
    }

}