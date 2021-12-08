import com.soywiz.klock.seconds
import com.soywiz.korge.*
import com.soywiz.korge.animate.Animator
import com.soywiz.korge.animate.animateSequence
import com.soywiz.korge.view.*
import com.soywiz.korge.input.*
import com.soywiz.korge.service.storage.storage
import com.soywiz.korge.tween.get
import com.soywiz.korim.color.*
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.geom.vector.*
import com.soywiz.korim.font.*
import com.soywiz.korim.text.TextAlignment
import com.soywiz.korio.async.ObservableProperty
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korma.interpolation.Easing
import kotlin.properties.Delegates

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

val score = ObservableProperty(0)
val best = ObservableProperty(0)

var gridColumns: Int = 6
var gridRows: Int = 6

var cellIndentSize: Int = 8
var cellSize: Int = 0
var fieldWidth: Int = 0
var fieldHeight: Int = 0
var leftIndent: Int = 0
var topIndent: Int = 0
var nextBlockId = 0

var isPressed = false
fun press() {
	isPressed = true
}
fun lift() {
	isPressed = false
}

var font: BitmapFont by Delegates.notNull()

fun getXFromIndex(index: Int) = leftIndent + cellIndentSize + (cellSize + cellIndentSize) * index
fun getYFromIndex(index: Int) = topIndent + cellIndentSize + (cellSize + cellIndentSize) * index
fun getXFromPosition(position: Position) = getXFromIndex(position.x)
fun getYFromPosition(position: Position) = getYFromIndex(position.y)


var blocksMap: MutableMap<Position, Block> = mutableMapOf()

var hoveredPositions: MutableList<Position> = mutableListOf()
/*fun convertPositionToPair(position: Position): Pair<Int,Int> {
	return Pair(position.x, position.y)
}*/

var isAnimating: Boolean = false
fun startAnimating() { isAnimating = true }
fun stopAnimating() { isAnimating = false }


fun getPositionFromPoint (point: Point): Position? {
	Napier.d("Point x = ${point.x}, y = ${point.y}")
	var xCoord = -1
	var yCoord = -1
	for (i in 0 until gridColumns) {
		if (point.x > (i * (cellSize + cellIndentSize) + (cellIndentSize/2) + leftIndent) &&
			point.x < ((i + 1) * (cellSize + cellIndentSize) + (cellIndentSize/2) + leftIndent))
			{
				Napier.d("Matched x value to position index $i")
				xCoord = i
				break
			}
	}
	if (xCoord == -1) {
		Napier.d("x value outside of cell range")
		return null
	}
	else
	{
		for (j in 0 until gridRows) {
			if (point.y > (j * (cellSize + cellIndentSize) + (cellIndentSize/2) + topIndent) &&
				point.y < ((j + 1) * (cellSize + cellIndentSize) + (cellIndentSize/2) + topIndent))
				{
					Napier.d("Matched y value to position index $j")
					yCoord = j
					break
				}
		}
	}
	return	if (yCoord == -1) {
				Napier.d("y value outside of cell range")
				null
			}
			else
			{
				Napier.d("Returned Position($xCoord,$yCoord)")
				Position(xCoord,yCoord)
			}
}


suspend fun main() = Korge(width = 480, height = 640, title = "2048", bgcolor = RGBA(253, 247, 240)) {
	Napier.base(DebugAntilog())

	val storage = views.storage
	best.update(storage.getOrNull("best")?.toInt() ?: 0)

	score.observe {
		if (it > best.value) best.update(it)
	}
	best.observe {
		// new code line here
		storage["best"] = it.toString()
	}

	font = resourcesVfs["clear_sans.fnt"].readBitmapFont()

	cellSize = views.virtualWidth / (gridColumns+2)
	Napier.d("Cell size = $cellSize")
	fieldWidth = (cellIndentSize * (gridColumns+1)) + gridColumns * cellSize
	Napier.d("Field width = $fieldWidth")
	fieldHeight = (cellIndentSize * (gridRows+1)) + gridRows * cellSize
	Napier.d("Field height = $fieldHeight")
	leftIndent = (views.virtualWidth - fieldWidth) / 2
	Napier.d("Left indent = $leftIndent")
	topIndent = 155

	val backgroundRect = roundRect(fieldWidth, fieldHeight, 5, fill = Colors["#e0d8e8"]) {
		position(leftIndent, topIndent)
	}

	graphics {
		position(leftIndent, topIndent)
		fill(Colors["#cec0b2"]) {
			for (i in 0 until gridColumns) {
				for (j in 0 until gridRows) {
					roundRect(cellIndentSize + (cellIndentSize + cellSize) * i, cellIndentSize + (cellIndentSize + cellSize) * j, cellSize, cellSize, 5)
				}
			}
		}
	}

	val bgLogo = roundRect(cellSize, cellSize, 5, fill = Colors["#6a00b0"]) {
		position(leftIndent + cellIndentSize, 12)
	}
	text("tr.io", cellSize * 0.5, Colors.WHITE, font).centerOn(bgLogo)

	val bgBest = roundRect(cellSize * 2.5, cellSize * 1.5, 5.0, fill = Colors["#bbae9e"]) {
		alignRightToRightOf(backgroundRect)
		alignTopToTopOf(bgLogo,cellSize*0.5)
	}
	text("BEST", cellSize * 0.5, RGBA(239, 226, 210), font) {
		centerXOn(bgBest)
		alignTopToTopOf(bgBest, 3.0)
	}
	text(best.value.toString(), cellSize * 1.0, Colors.WHITE, font) {
		setTextBounds(Rectangle(0.0, 0.0, bgBest.width, cellSize * 0.5))
		alignment = TextAlignment.MIDDLE_CENTER
		alignTopToTopOf(bgBest, 5.0 + (cellSize*0.5) + 5.0)
		centerXOn(bgBest)
		best.observe {
			text = it.toString()
		}
	}

	val bgScore = roundRect(cellSize * 2.5, cellSize*1.5, 5.0, fill = Colors["#bbae9e"]) {
		alignRightToLeftOf(bgBest, 24.0)
		alignTopToTopOf(bgBest)
	}
	text("SCORE", cellSize * 0.5, RGBA(239, 226, 210), font) {
		centerXOn(bgScore)
		alignTopToTopOf(bgScore, 3.0)
	}
	text(score.value.toString(), cellSize * 1.0, Colors.WHITE, font) {
		setTextBounds(Rectangle(0.0, 0.0, bgScore.width, cellSize * 0.5))
		alignment = TextAlignment.MIDDLE_CENTER
		centerXOn(bgScore)
		alignTopToTopOf(bgScore, 5.0 + (cellSize*0.5) + 5.0)
		score.observe {
			text = it.toString()
		}
	}

	val restartImg = resourcesVfs["restart.png"].readBitmap()

	val btnSize = cellSize * 0.5
	val restartBlock = container {
		val background = roundRect(btnSize, btnSize, 5.0, fill = RGBA(185, 174, 160))
		image(restartImg) {
			size(btnSize * 0.8, btnSize * 0.8)
			centerOn(background)
		}
		alignTopToBottomOf(bgLogo, 5)
		alignRightToRightOf(bgLogo)
	}
	Napier.d("UI Initialized")


	blocksMap = initializeRandomBlocksMap ()
	drawAllBlocks()


	touch {
		onDown { if (!isAnimating) pressDown(getPositionFromPoint(mouseXY)) }
		onMove { if (!isAnimating) hoverBlock(getPositionFromPoint(mouseXY))  }
		onUp { if (!isAnimating) pressUp(getPositionFromPoint(mouseXY)) }
	}

}



fun Container.deleteBlock(block: Block?) {
	if (block is Block) {
		Napier.v("deleteBlock with Id = ${block.id}")
		blocksMap = blocksMap.filter { (position, existingBlock) -> block.id != existingBlock.id }.toMutableMap()
		removeBlock(block)
	}
}

fun Container.drawBlock (block: Block, position: Position) {
	Napier.v("drawBlock at Position(${position.x},${position.y}) with Number ${block.number.value}, IsSelected ${block.isSelected}")
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

fun Container.hoverBlock (maybePosition: Position?) {
	if (isPressed && maybePosition != null && (hoveredPositions.size > 0 && hoveredPositions.last() != maybePosition)) {
		if (blocksMap[maybePosition] == null) {
			Napier.w("F found at Position(${maybePosition.x},${maybePosition.y})")
		} else if (hoveredPositions.size > 0 && !isValidTransition(
				hoveredPositions.last(),
				maybePosition
			)
		) {
			Napier.d("Block transition is invalid")
		} else if (hoveredPositions.size > 0 && blocksMap[hoveredPositions.last()]?.number != blocksMap[maybePosition]?.number) {
			Napier.d("Hovered a square of a different value")
		} else if (hoveredPositions.elementAtOrNull(hoveredPositions.size - 2) == maybePosition) {
			Napier.d("Reverted previous hover")
			updateBlock(
				blocksMap[hoveredPositions[(hoveredPositions.size - 1)]]!!.unselect(),
				hoveredPositions[(hoveredPositions.size - 1)]
			)
			hoveredPositions.removeAt(hoveredPositions.size - 1)
		} else if (blocksMap[maybePosition]?.isSelected!!) {
			Napier.d("Block is already selected)")
		} else {
			Napier.v("Hovering Block at Position(${maybePosition.x},${maybePosition.y} from Position(${hoveredPositions.last().x},${hoveredPositions.last().y})")
			hoveredPositions.add(maybePosition)
			updateBlock(blocksMap[maybePosition]!!.select(), maybePosition)

		}
	}
}

fun Stage.pressUp (maybePosition: Position?) {
	if (maybePosition is Position) {
		Napier.v("Releasing Block at Position(${maybePosition.x},${maybePosition.y})")
	}
	else{
		Napier.v("Releasing Block outside of field")
	}
	isPressed = false
	if (atLeastThreeSelected()) {
		successfulShape()
	}
	else
	{
		unsuccessfulShape()
	}

}

fun Container.pressDown (maybePosition: Position?) {
	if (maybePosition != null)
	{
		if (blocksMap[maybePosition] != null)
		{
			Napier.v("Selecting Block at Position(${maybePosition.x},${maybePosition.y})")
			isPressed = true
			hoveredPositions.add(maybePosition)
			updateBlock(blocksMap[maybePosition]!!.select(), maybePosition)
		}
		else
		{
			Napier.w("No block found at Position(${maybePosition.x},${maybePosition.y})")
		}
	}
	else
	{
		Napier.w("Position parameter was null ")
	}
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
	val mergeMap= determineMerge(hoveredPositions.toMutableList())
	Napier.d("Hovered position size ${hoveredPositions.size}")
	hoveredPositions.clear()
	animateMerge(mergeMap)
	score.update(score.value + scoredPoints)
	if (!hasAvailableMoves()){
		Napier.d("Game Over!")
	}
}

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
	}
	stopAnimating()
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