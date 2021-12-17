import com.soywiz.korge.view.*
import io.github.aakira.napier.Napier


fun Container.deleteBlock(block: Block?) {
    if (block is Block) {
        Napier.v("deleteBlock with Id = ${block.id}")
        blocksMap = blocksMap.filter { (position, existingBlock) -> block.id != existingBlock.id }.toMutableMap()
        removeBlock(block)
    }
}


fun Container.drawBlock (block: Block, position: Position) {
    Napier.v("drawBlock at Position(${position.x},${position.y}) with Number ${block.number.value}, IsSelected ${block.selection}")
    blocksMap[position] = addBlock(block).position(getXFromPosition(position), getYFromPosition(position))
    addBlock(block).position(getXFromPosition(position), getYFromPosition(position))
}


fun Container.drawAllBlocks () {
    Napier.v("drawBlocks")
    blocksMap
        .forEach { (position, block) -> drawBlock(block, position) }
}

fun Container.updateBlock(block: Block, position: Position){
    val newBlock = block.copy()
    deleteBlock(block)
    blocksMap[position] = newBlock
    drawBlock(newBlock, position)
}

fun Container.atLeastThreeSelected(): Boolean {
    return (hoveredPositions.size > 2)
}

fun Stage.unsuccessfulShape() {
    Napier.d("Shape was unsuccessful ")
    hoveredPositions
        .forEach { position ->
            blocksMap[position] = blocksMap[position]?.unselect()!!
            Napier.d("Removing hovered ${position.log()}")
            updateBlock(blocksMap[position]!!, position)
        }
    hoveredPositions.clear()
}

fun Stage.successfulShape() {
    val pattern = determinePattern(hoveredPositions.toMutableList())
    val scoredPoints = determineScore(hoveredPositions.toMutableList())
    Napier.d("Hovered position size ${hoveredPositions.size}")
    val mergeMap = determineMerge(hoveredPositions.toMutableList())
    Napier.d("Hovered position size ${hoveredPositions.size}")
    hoveredPositions.clear()
    animateMerge(mergeMap)
    score.update(score.value + scoredPoints)
}



fun Container.drawBombHover (maybePosition: Position?) {
    if (maybePosition != null) {
        hoveredBombPositions = tryAllSurroundingPositions(maybePosition).toMutableList()
        hoveredBombPositions.add(maybePosition)
        hoveredBombPositions.forEach { pos ->
            if (blocksMap[pos] != null) {
                updateBlock(blocksMap[pos]!!.selectBomb(), pos)
            } else {
                Napier.e("drawBombHover tried to draw a surrounding block that is null")
            }
        }
    } else {
        Napier.e("drawBombHover tried to draw a main block that is null")
    }
}
fun Container.removeBombHover () {
    hoveredBombPositions.forEach { pos ->
        if (blocksMap[pos] != null) {
            updateBlock(blocksMap[pos]!!.unselect(), pos)
        } else {
            Napier.e("drawBombHover tried to draw a surrounding block that is null")
        }
    }
    hoveredBombPositions.clear()
}
