import io.github.aakira.napier.Napier
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
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
    NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST;
}

fun getCardinalDirections() = arrayOf(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)

fun initBlock (): Block {
    val selectedId = nextBlockId
    nextBlockId++;
    val random = Random.nextDouble()
    return when (true){
        selectedId == random27ID -> Block(id=selectedId, Number.THREE)
        random < 0.1 -> Block(id = selectedId, Number.THREE)
        random < 0.2 -> Block(id = selectedId, Number.TWO)
        random < 0.5 -> Block(id = selectedId, Number.ONE)
        else -> Block(id = selectedId, Number.ZERO)
    }
}

fun initOneBlock (): Block {
    val selectedId = nextBlockId
    nextBlockId++;
    return Block(id = selectedId, Number.ZERO)
}

fun initBlock (index: Int): Block {
    val selectedId = nextBlockId
    nextBlockId++;
    return Block(id = selectedId, Number.values()[index % Number.values().size])
}


fun initializeRandomBlocksMap (): MutableMap<Position, Block> {
    return allPositions().map { position ->Pair(position, initBlock()) }.toMap().toMutableMap()
}

fun initializeOnesBlocksMap (): MutableMap<Position, Block> {
    return allPositions().map { position -> Pair(position, initOneBlock()) }.toMap().toMutableMap()
}

fun initializeFixedBlocksMap (): MutableMap<Position, Block> {
    return allPositions().mapIndexed { index, position -> Pair(position, initBlock(index)) }.toMap().toMutableMap()
}

//private fun getEnumId(position: Position) = array[position.x + position.y * gridRows]

private fun getNumber(id: Int) = Number.values()[id]

private fun getPosition(idx: Int) = Position(idx % gridColumns, idx / gridColumns)
fun getIndex(position: Position) = position.x + position.y * gridColumns

private fun tryPositionInDirection(position:Position, direction: Direction) =
    try {
        when (direction) {
            Direction.NORTH -> Position(position.x, position.y + 1)
            Direction.NORTHEAST -> Position(position.x + 1, position.y + 1)
            Direction.EAST -> Position(position.x + 1, position.y)
            Direction.SOUTHEAST -> Position(position.x + 1, position.y - 1)
            Direction.SOUTH ->  Position(position.x, position.y - 1)
            Direction.SOUTHWEST ->  Position(position.x - 1, position.y - 1)
            Direction.WEST -> Position(position.x - 1, position.y)
            Direction.NORTHWEST -> Position(position.x - 1, position.y + 1)
        }
    }
    catch (e: IllegalArgumentException) {
         null
    }

private fun tryAllAdjacentPositions(position: Position) =
    getCardinalDirections().mapNotNull{ direction -> tryPositionInDirection(position, direction) }

fun tryAllSurroundingPositions(position: Position) =
    Direction.values().mapNotNull{ direction -> tryPositionInDirection(position, direction) }

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
    val random = Random.nextDouble()
    return when (blocksMap.maxOf { (_, block) -> block.number.ordinal }) {
        in 0..5 -> {
            when (true) {
                random < 0.05 -> Number.THREE
                random < 0.2 -> Number.TWO
                random < 0.55 -> Number.ONE
                else -> Number.ZERO
            }
        }
        else -> {
            when (true) {
                random < 0.1 -> Number.THREE
                random < 0.3 -> Number.TWO
                random < 0.65 -> Number.ONE
                else -> Number.ZERO
            }
        }
    }
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

    val nextNumber =
        when (positionList.size) {
            in 0..5 -> blocksMap[positionList.first()]?.number?.next() ?: Number.ZERO
            in 6..18 -> blocksMap[positionList.first()]?.number?.next()?.next() ?: Number.ZERO
            else -> blocksMap[positionList.first()]?.number?.next()?.next()?.next() ?: Number.ZERO
        }
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
        Pattern.D6 -> {
            val last = positionList.last()
            val xList = positionList.map { position -> position.x }
            val xMax = xList.maxOrNull() ?: 100
            val xMin = xList.minOrNull() ?: 0
            val yList = positionList.map { position -> position.y }
            val yMax = yList.maxOrNull() ?: 100
            val yMin = yList.minOrNull() ?: 0

            if (xMax - xMin == 1){
                val mergeX = if (last.x == xMax) xMin else xMax
                for (i in 0 until 3){
                    mergeMap[Position(last.x,yMin + i)] = Pair(nextNumber.previous(), listOf(Position(mergeX,yMin + i)))
                }
            }
            else{
                val mergeY = if (last.y == yMax) yMin else yMax
                for (i in 0 until 3){
                    mergeMap[Position(xMin + i, last.y)] = Pair(nextNumber.previous(), listOf(Position(xMin + i,mergeY)))
                }
            }
        }
        Pattern.D8 -> {
            val last = positionList.last()
            val xList = positionList.map { position -> position.x }
            val xMax = xList.maxOrNull() ?: 100
            val xMin = xList.minOrNull() ?: 0
            val yList = positionList.map { position -> position.y }
            val yMax = yList.maxOrNull() ?: 100
            val yMin = yList.minOrNull() ?: 0

            if (xMax - xMin == 1){
                val mergeX = if (last.x == xMax) xMin else xMax
                for (i in 0 until 4){
                    mergeMap[Position(last.x,yMin + i)] = Pair(nextNumber.previous(), listOf(Position(mergeX,yMin + i)))
                }
            }
            else{
                val mergeY = if (last.y == yMax) yMin else yMax
                for (i in 0 until 4){
                    mergeMap[Position(xMin + i, last.y)] = Pair(nextNumber.previous(), listOf(Position(xMin + i,mergeY)))
                }
            }
        }
        Pattern.O9 -> {
            val xList = positionList.map { position -> position.x }
            val xAvg = xList.average().roundToInt()
            val yList = positionList.map { position -> position.y }
            val yAvg = yList.average().roundToInt()
            val center = Position(xAvg, yAvg)
            val updatedNextNumber = Number.values()[max(nextNumber.ordinal, blocksMap[center]?.number?.ordinal ?: 0)]
            mergeMap[center] = Pair(updatedNextNumber.next().next(), positionList.filter {position -> position != center}.toMutableList())
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