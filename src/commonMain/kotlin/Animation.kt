import com.soywiz.klock.seconds
import com.soywiz.korge.animate.Animator
import com.soywiz.korge.animate.animateSequence
import com.soywiz.korge.tween.get
import com.soywiz.korge.view.Stage
import com.soywiz.korge.view.View
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korma.interpolation.Easing
import io.github.aakira.napier.Napier
import com.soywiz.korge.view.position
import com.soywiz.korge.view.scale
import kotlin.math.sin
import kotlin.math.cos
import kotlin.random.Random

fun Stage.animateMerge(mergeMap: MutableMap<Position, Pair<Number, List<Position>>>) = launchImmediately {
    startAnimating()
    animateSequence {
        parallel {
            Napier.v("Animating the blocks merging together")
            mergeMap.forEach { (headPosition, valueAndMergePositions) ->
                val mergePositions = valueAndMergePositions.second
                mergePositions.forEach { position ->
                    Napier.d("Moving block from ${position.log()} to new block")
                    blocksMap[position]!!.moveTo(
                        getXFromPosition(headPosition),
                        getYFromPosition(headPosition),
                        0.15.seconds,
                        Easing.LINEAR
                    )
                }

            }
        }
        block {
            Napier.v("Animating deletion of previous blocks and adding new upgraded block")
            parallel{
                mergeMap.forEach { (headPosition, valueAndMergePositions) ->
                    valueAndMergePositions.second.forEach { position -> deleteBlock(blocksMap[position]!!) }
                    val value = valueAndMergePositions.first
                    val newBlock = blocksMap[headPosition]!!.updateNumber(value).unselect().copy()
                    deleteBlock(blocksMap[headPosition]!!)
                    blocksMap[headPosition] = newBlock
                    drawBlock(newBlock, headPosition)
                }
            }
        }
        sequenceLazy {
            val newPositionBlocks = generateBlocksForEmptyPositions()
            Napier.w("Generating new blocks ${newPositionBlocks.map { (position, block) -> "${block.number.value} at (${position.log()}\n" }}")
            blocksMap.putAll(newPositionBlocks)


            parallel {
                newPositionBlocks
                    .forEach { (position, block) ->

                        val x = getXFromPosition(position)
                        val y = getYFromPosition(position)
                        val scale = block.scale

                        val newBlock =
                            addBlock(block).position(x + cellSize / 2, y + cellSize / 2).scale(0)

                        tween(
                            newBlock::x[x],
                            newBlock::y[y],
                            newBlock::scale[scale],
                            time = 0.3.seconds,
                            easing = Easing.EASE_SINE
                        )
                    }

                mergeMap.forEach { (headPosition, _) ->
                    if (blocksMap[headPosition] != null) {
                        animateConsumption(blocksMap[headPosition]!!)
                    } else {
                        Napier.w("No block found for consumption at ${headPosition.log()}")
                    }
                }
            }
        }
        block {
            stopAnimating()
            if (!hasAvailableMoves()) {
                Napier.d("Game Over!")
            }
        }
    }
}

// Not used any more but left it in case of future changes
fun Animator.animateGravity() {
    parallel {
        blocksMap = blocksMap.mapKeys { (position, block) ->
            blocksMap.filter { (comparisonPosition, _) ->
                position.x == comparisonPosition.x && position.y < comparisonPosition.y }
                .size.let {
                    val newPosition = Position(position.x, gridRows - 1 - it)
                    if (newPosition != position){
                        blocksMap[position]!!.moveTo(getXFromPosition(newPosition), getYFromPosition(newPosition), 0.5.seconds, Easing.EASE_SINE)
                    }
                    newPosition

                }}.toMutableMap()

    }
}

fun Animator.animateConsumption(block: Block) {
    val x = block.x
    val y = block.y
    val scale = block.scale
    tween(
        block::x[x - 4],
        block::y[y - 4],
        block::scale[scale + 0.1],
        time = 0.1.seconds,
        easing = Easing.LINEAR
    )
    tween(
        block::x[x],
        block::y[y],
        block::scale[scale],
        time = 0.1.seconds,
        easing = Easing.LINEAR
    )
}

fun Stage.animateBombSelection(image: View, toggle: Boolean) = launchImmediately {
    animateSequence {
        val x = image.x
        val y = image.y
        val scale = image.scale
        if (toggle) {
            tween(
                image::x[x - 4],
                image::y[y - 4],
                image::scale[scale * 1.1],
                time = 0.1.seconds,
                easing = Easing.LINEAR
            )
        }
        else {
            tween(
                image::x[x + 4],
                image::y[y + 4],
                image::scale[scale / 1.1],
                time = 0.1.seconds,
                easing = Easing.LINEAR
            )
        }
    }
}

fun Stage.animateBomb() = launchImmediately {
    startAnimating()
    animateSequence {
        parallel {
            Napier.v("Animating the bomb")
            hoveredBombPositions.forEach { position ->
                val random = Random.nextDouble(0.0,2*3.1415)
                val xDirection = sin(random)
                val yDirection = cos(random)
                Napier.d("Bombing block at ${position.log()}")
                blocksMap[position]!!.moveTo(
                    xDirection*1000,
                    yDirection*1000,
                    1.seconds,
                    Easing.EASE_OUT_QUAD
                )
            }

        }
        block {
            hoveredBombPositions.forEach { position -> deleteBlock(blocksMap[position]!!) }
            hoveredBombPositions.clear()
        }
        sequenceLazy {
            val newPositionBlocks = generateBlocksForEmptyPositions()
            Napier.w("Generating new blocks ${newPositionBlocks.map { (position, block) -> "${block.number.value} at (${position.log()}\n" }}")
            blocksMap.putAll(newPositionBlocks)


            parallel {
                newPositionBlocks
                    .forEach { (position, block) ->

                        val x = getXFromPosition(position)
                        val y = getYFromPosition(position)
                        val scale = block.scale

                        val newBlock =
                            addBlock(block).position(x + cellSize / 2, y + cellSize / 2).scale(0)

                        tween(
                            newBlock::x[x],
                            newBlock::y[y],
                            newBlock::scale[scale],
                            time = 0.3.seconds,
                            easing = Easing.EASE_SINE
                        )
                    }
            }
        }
    }
    stopAnimating()
}