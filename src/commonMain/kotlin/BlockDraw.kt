import com.soywiz.korge.view.*
import io.github.aakira.napier.Napier
import kotlin.math.max
import kotlin.math.min


fun Container.deleteBlock(block: Block?) {
    if (block is Block) {
        Napier.v("deleteBlock with Id = ${block.id}")
        blocksMap = blocksMap.filter { (position, existingBlock) -> block.id != existingBlock.id }.toMutableMap()
        removeBlock(block)
    }
}


fun Container.drawBlock (block: Block, position: Position) {
    Napier.v("drawBlock at ${position.log()} with Number ${block.number.value}, IsSelected ${block.selection}")
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
    if (hoveredPositions.size >= 9) tryAddMagnets(1)
    val pattern = determinePattern(hoveredPositions.toMutableList())
    val scoredPoints = determineScore(hoveredPositions.toMutableList())
    Napier.d("Hovered position size ${hoveredPositions.size}")
    val mergeMap = determineMerge(hoveredPositions.toMutableList())
    Napier.d("Hovered position size ${hoveredPositions.size}")
    hoveredPositions.clear()
    animateMerge(mergeMap)
    score.update(score.value + scoredPoints)
}

fun tryAddBombs(numberOfBombs: Int){
    Napier.d("Trying to add $numberOfBombs bombs")
    val newBombCount = min(bombsLoadedCount.value + numberOfBombs, maxBombCount)
    bombsLoadedCount.update(newBombCount)
}

fun removeBomb(){
    Napier.d("Removing a bomb")
    val newBombCount = max(bombsLoadedCount.value - 1, 0)
    bombsLoadedCount.update(newBombCount)
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

fun tryAddMagnets(numberOfMagnets: Int){
    Napier.d("Trying to add $numberOfMagnets magnets")
    val newMagnetCount = min(magnetsLoadedCount.value + numberOfMagnets, maxMagnetCount)
    magnetsLoadedCount.update(newMagnetCount)
}

fun removeMagnet(){
    Napier.d("Removing a magnet")
    val newMagnetCount = max(magnetsLoadedCount.value - 1, 0)
    magnetsLoadedCount.update(newMagnetCount)
}

fun Stage.drawMagnetSelection (maybePosition: Position?) {
    when (true) {
        (maybePosition == null) -> Napier.e("drawMagnetSelection tried to draw a main block that is null")
        (!magnetSelection.selected) -> Napier.e("drawMagnetSelection tried to draw  when magnet is unselected")
        (magnetSelection.firstPosition == null) -> {
            magnetSelection.selectFirst(maybePosition)
            updateBlock(blocksMap[maybePosition]!!.selectMagnet(), maybePosition)
        }
        (magnetSelection.firstPosition == maybePosition) -> {
            magnetSelection.unselectFirst()
            updateBlock(blocksMap[maybePosition]!!.unselect(), maybePosition)
        }
        (magnetSelection.secondPosition == null) -> {
            magnetSelection.selectSecond(maybePosition)
            animateMagnet(magnetSelection.copy())
            removeMagnet()
            magnetSelection.unselect()
            animateSelection(magnetContainer, false)
        }
        else ->
        {

        }
    }
}

fun Container.removeMagnetSelection () {
    if (magnetSelection.firstPosition != null && blocksMap[magnetSelection.firstPosition] != null) {
        updateBlock(
            blocksMap[magnetSelection.firstPosition]!!.unselect(),
            magnetSelection.firstPosition!!
        )
    }else {
        Napier.d("No first magnet position to remove")
    }
    if (magnetSelection.secondPosition != null && blocksMap[magnetSelection.secondPosition] != null) {
        updateBlock(
            blocksMap[magnetSelection.secondPosition]!!.unselect(),
            magnetSelection.secondPosition!!
        )
    }else {
        Napier.d("No second magnet position to remove")
    }
    magnetSelection.unselect()
}
