import com.soywiz.korio.stream.AsyncGetPositionStream
import io.github.aakira.napier.Napier
import kotlin.math.abs
import kotlin.random.*

data class Position(val x: Int, val y: Int){
    init {
        require(x >= 0)
        require(x < gridColumns)
        require(y >= -1)
        require(y < gridRows)
    }
    fun log (): String {
        return "Position(${this.x},${this.y})"
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

fun initBlock (index: Int): Block {
    val selectedId = nextBlockId
    nextBlockId++;
    return Block(id = selectedId, Number.values()[index % Number.values().size])
}


fun initializeRandomBlocksMap (): MutableMap<Position, Block> {
    return allPositions().map { position -> Pair(position, initBlock()) }.toMap().toMutableMap()
}

fun initializeFixedBlocksMap (): MutableMap<Position, Block> {
    return allPositions().mapIndexed { index, position -> Pair(position, initBlock(index)) }.toMap().toMutableMap()
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
            Direction.BOTTOM ->  Position(position.x, position.y - 1)
        }
    }
    catch (e: IllegalArgumentException) {
         null
    }

private fun tryAllAdjacentPositions(position: Position) =
    Direction.values().mapNotNull{ direction -> tryAdjacentPositions(position, direction) }

fun isValidTransition(oldPosition: Position, newPosition: Position?): Boolean{
    return  if(newPosition is Position) {
                (abs(newPosition.x - oldPosition.x) == 1) xor (abs(newPosition.y - oldPosition.y) == 1) &&
                        ((newPosition.x - oldPosition.x) * (newPosition.y - oldPosition.y) == 0)
            }
            else
            {
                 false
            }
}


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
    return allPositions().mapNotNull { position -> if (!blocksMap.containsKey(position)) position else null }
}

fun getRandomNumber(): Number {
    return blocksMap.map { (_, block) ->
                block.number.previous()
            }.random()
}

fun generateBlocksForEmptyPositions(): List<Pair<Position, Block>> {
    return getAllEmptyPositions().map { position ->
        val selectedId = nextBlockId
        nextBlockId++
        Pair(position, Block(selectedId, getRandomNumber()))
    }

}

fun determineMerge(positionList: MutableList<Position>) : MutableMap<Position, Pair<Number, List<Position>>> {
    val mergeMap = mutableMapOf<Position, Pair<Number,List<Position>>>()
    val pattern = determinePattern(positionList)
    val nextNumber = blocksMap[positionList.first()]?.number?.next() ?: Number.ZERO
    when (pattern) {
        Pattern.TRIPLE -> {
            val last = positionList.removeLast()
            mergeMap[last] = Pair(nextNumber, positionList)
        }
        Pattern.O4 -> {
            val last = positionList.removeLast()
            val secondLast = positionList.removeLast()
            mergeMap[last] = Pair(nextNumber, positionList.subList(0,1).toMutableList())
            mergeMap[secondLast] = Pair(nextNumber, positionList.subList(1,2).toMutableList())
        }
        Pattern.I4 -> {

            val last = positionList.last()
            val first = positionList.first()

            val mergeList =
                if (first.x == last.x) {
                    blocksMap.filter { (position, _) -> position.x == first.x}
                             .toList()
                }
                else {
                    blocksMap.filter { (position, _) -> position.y == first.y }
                        .toList()
                }
            val mergeSum =
                mergeList
                    .map { (_, block) -> block.number.value}
                    .fold (0, {a,b -> a + b })
            val upgradedNumber = findClosestRoundedUp(mergeSum)
            mergeMap[last] = Pair(upgradedNumber, mergeList.map {(position, _) -> position }.filter { position -> position != last })
        }
        Pattern.I5 -> {

            val last = positionList.last()
            val first = positionList.first()

            val mergeList =
                if (first.x == last.x) {
                    blocksMap.filter { (position, _) -> position.x == first.x || position.y == last.y}
                             .toList()
                }
                else {
                    blocksMap.filter { (position, _) -> position.y == first.y || position.x == last.x }
                        .toList()
                }
            val mergeSum =
                mergeList
                    .map { (_, block) -> block.number.value}
                    .fold (0, {a,b -> a + b })
            val upgradedNumber = findClosestRoundedUp(mergeSum)
            mergeMap[last] = Pair(upgradedNumber, mergeList.map {(position, _) -> position }.filter { position -> position != last })
        }
        Pattern.I6 -> {

            val last = positionList.last()
            val first = positionList.first()

            val mergeListFirst =
                if (first.x == last.x) {
                    blocksMap.filter { (position, _) -> (position.x == last.x && (position.y - first.y) < (last.y - position.y)) || position.y == first.y}
                        .toList()
                }
                else {
                    blocksMap.filter { (position, _) -> (position.y == last.y && (position.x - first.x) < (last.x - position.x)) || position.x == first.x }
                        .toList()
                }

            Napier.v("MergeListFirst size ${mergeListFirst.size}")

            val mergeSumFirst =
                mergeListFirst
                    .map { (_, block) -> block.number.value}
                    .fold (0, {a,b -> a + b })
            val upgradedNumberFirst = findClosestRoundedUp(mergeSumFirst)


            val mergeListLast =
                if (first.x == last.x) {
                    blocksMap.filter { (position, _) -> (position.x == first.x && (position.y - first.y) > (last.y - position.y)) || position.y == last.y}
                        .toList()
                }
                else {
                    blocksMap.filter { (position, _) -> (position.y == first.y && (position.x - first.x) > (last.x - position.x)) || position.x == last.x }
                        .toList()
                }

            Napier.v("MergeListLast size ${mergeListFirst.size}")

            val mergeSumLast =
                mergeListLast
                    .map { (_, block) -> block.number.value}
                    .fold (0, {a,b -> a + b })
            val upgradedNumberLast = findClosestRoundedUp(mergeSumLast)
            mergeMap[first] = Pair(upgradedNumberFirst, mergeListFirst.map {(position, _) -> position }.filter { position -> position != first })
            mergeMap[last] = Pair(upgradedNumberLast, mergeListLast.map {(position, _) -> position }.filter { position -> position != last })
        }
        else -> {
            val last = positionList.removeLast()
            mergeMap[last] = Pair(nextNumber, positionList)
        }
    }

    return mergeMap

}

fun determineScore(positionList: MutableList<Position>) : Int {
    return positionList.map { position -> blocksMap[position]?.number?.value?: 0 }.fold(0,{a,b -> a + b})
}