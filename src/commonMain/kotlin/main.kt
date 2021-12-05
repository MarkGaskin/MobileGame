import com.soywiz.korge.*
import com.soywiz.korge.view.*
import com.soywiz.korge.input.*
import com.soywiz.korim.color.*
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.geom.vector.*
import com.soywiz.korim.font.*
import com.soywiz.korim.text.TextAlignment
import kotlin.properties.Delegates

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

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
	/*
	val minDegrees = (-16).degrees
	val maxDegrees = (+16).degrees

	val image = image(resourcesVfs["korge.png"].readBitmap()) {
		rotation = maxDegrees
		anchor(.5, .5)
		scale(.8)
		position(256, 256)
	}

	while (false) {
		image.tween(image::rotation[minDegrees], time = 1.seconds, easing = Easing.EASE_IN_OUT)
		image.tween(image::rotation[maxDegrees], time = 1.seconds, easing = Easing.EASE_IN_OUT)
	}
	*/

	Napier.base(DebugAntilog())

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

	val backgroundRect = roundRect(fieldWidth, fieldHeight, 5, fill = Colors["#b9aea0"]) {
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
	text("Tr.io", cellSize * 0.5, Colors.WHITE, font).centerOn(bgLogo)

	val bgBest = roundRect(cellSize * 2.5, cellSize * 1.5, 5.0, fill = Colors["#bbae9e"]) {
		alignRightToRightOf(backgroundRect)
		alignTopToTopOf(bgLogo,cellSize*0.5)
	}
	text("BEST", cellSize * 0.5, RGBA(239, 226, 210), font) {
		centerXOn(bgBest)
		alignTopToTopOf(bgBest, 3.0)
	}
	text((blocksMap.size + 1).toString(), cellSize * 1.0, Colors.WHITE, font) {

		setTextBounds(Rectangle(0.0, 0.0, bgBest.width, cellSize * 0.5))
		alignment = TextAlignment.MIDDLE_CENTER
		alignTopToTopOf(bgBest, 5.0 + (cellSize*0.5) + 5.0)
		centerXOn(bgBest)
	}

	val bgScore = roundRect(cellSize * 2.5, cellSize*1.5, 5.0, fill = Colors["#bbae9e"]) {
		alignRightToLeftOf(bgBest, 24.0)
		alignTopToTopOf(bgBest)
	}
	text("SCORE", cellSize * 0.5, RGBA(239, 226, 210), font) {
		centerXOn(bgScore)
		alignTopToTopOf(bgScore, 3.0)
	}
	text(cellSize.toString(), cellSize * 1.0, Colors.WHITE, font) {
		setTextBounds(Rectangle(0.0, 0.0, bgScore.width, cellSize * 0.5))
		alignment = TextAlignment.MIDDLE_CENTER
		centerXOn(bgScore)
		alignTopToTopOf(bgScore, 5.0 + (cellSize*0.5) + 5.0)
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


	blocksMap = initializeBlocksMap ()
	drawAllBlocks()
	//deleteBlock(Position(3,3))



	touch {
		onDown { pressDown(getPositionFromPoint(mouseXY)) }
		onMove { hoverBlock(getPositionFromPoint(mouseXY))  }
		onUp { pressUp(getPositionFromPoint(mouseXY)) }
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

/*fun Container.findPosition (position: Position): Map<Position, Block> {
	return blocks.filter { (key, value) -> key.x == position.x && key.y == position.y }
}

fun Container.checkPositionExists (position: Position): Boolean {
	return findPosition(position).isNotEmpty()
}*/

fun Container.updateBlock(block: Block, position: Position){
	val newBlock = block.copy()
	deleteBlock(block)
	blocksMap[position] = newBlock
	drawBlock(newBlock, position)
}

fun Container.checkForPattern(): Boolean {
	return (hoveredPositions.size > 2)
}

fun Container.hoverBlock (maybePosition: Position?) {
	if (isPressed && maybePosition != null && (hoveredPositions.size > 0 && hoveredPositions.last() != maybePosition)) {
		if (blocksMap[maybePosition] == null) {
			Napier.w("No block found at Position(${maybePosition.x},${maybePosition.y})")
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

fun Container.pressUp (maybePosition: Position?) {
	if (maybePosition is Position) {
		Napier.v("Releasing Block at Position(${maybePosition.x},${maybePosition.y})")
	}
	else{
		Napier.v("Releasing Block outside of field")
	}
	isPressed = false
	if (checkForPattern()) {
		successfulShape()
	}
	else
	{
		unsuccessfulShape()
	}

}

fun Container.unsuccessfulShape() {
	Napier.d("Shape was unsuccessful ")
	hoveredPositions
		.forEach { position ->
			blocksMap[position] = blocksMap[position]?.unselect()!!
			Napier.d("Removing hovered Position(${position.x},${position.y})")
			updateBlock(blocksMap[position]!!, position)
			}
	hoveredPositions.clear()
}

fun Container.successfulShape() {
	hoveredPositions.forEach { position -> deleteBlock(blocksMap[position]) }
	hoveredPositions.clear()
}

fun Container.pressDown (maybePosition: Position?) {
	if (maybePosition != null)
	{
		if (blocksMap[maybePosition] != null)
		{
			Napier.v("Selecting Block at Position(${maybePosition.x},${maybePosition.y})")
			isPressed = !isPressed
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