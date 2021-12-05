import kotlin.random.*

data class Position(val x: Int, val y: Int){
    init {
        require(x >= 0)
        require(x < gridColumns)
        require(y >= 0)
        require(y < gridRows)
    }
}

fun allPositions (): List<Position> {
    val positionList: MutableList<Position> = mutableListOf()
    for (i in 0 until gridColumns){
        for (j in 0 until gridRows){
            positionList.add(Position(i,j))
        }
    }
    return positionList
}

enum class Direction {
    LEFT, RIGHT, TOP, BOTTOM
}

fun initBlock (): Block {
    val selectedId = nextBlockId
    nextBlockId++;
    val random = Random.nextDouble()
    return if (random < 0.1)  Block(id = selectedId, Number.THREE)
            else if (random >= 0.1 && random < 0.2) Block(id = selectedId, Number.TWO)
            else if (random >= 0.2 && random < 0.5) Block(id = selectedId, Number.ONE)
            else Block(id = selectedId, Number.ZERO)
}


fun initializeBlocksMap (): MutableMap<Position, Block> {
    return allPositions().map { position -> Pair(position, initBlock()) }.toMap().toMutableMap()
}

//private fun getEnumId(position: Position) = array[position.x + position.y * gridRows]

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
    return blocksMap.any { (position, block) -> hasTwoMatchingAdjacents(position, block) }
}

private fun hasTwoMatchingAdjacents(position: Position, block: Block): Boolean {
    return tryAllAdjacentPositions(position)
            .map { adjPosition -> blocksMap[adjPosition]?.number }
            .filter { adjNumber -> adjNumber == block.number}
            .count() >= 2
}



fun getAllEmptyPositions(): List<Position> {
    return allPositions().mapNotNull { position -> if (blocksMap.containsKey(position)) position else null }
}