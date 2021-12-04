import com.soywiz.klock.*
import com.soywiz.korge.*
import com.soywiz.korge.html.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korge.input.*
import com.soywiz.korim.color.*
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.geom.vector.*
import com.soywiz.korma.interpolation.*
import com.soywiz.korim.font.*
import com.soywiz.korim.text.TextAlignment
import kotlin.properties.Delegates
import kotlin.random.Random
import Number
import kotlin.collections.set
import Napier

var gridColumns: Int = 6
var gridRows: Int = 6
var cellIndentSize: Int = 8
var cellSize: Int = 0
var fieldWidth: Int = 0
var fieldHeight: Int = 0
var leftIndent: Int = 0
var topIndent: Int = 0
var positionMap = PositionMap()
var freeId = 0

var font: BitmapFont by Delegates.notNull()

fun getXFromIndex(index: Int) = leftIndent + cellIndentSize + (cellSize + cellIndentSize) * index
fun getYFromIndex(index: Int) = topIndent + cellIndentSize + (cellSize + cellIndentSize) * index
fun getXFromPosition(position: Position) = getXFromIndex(position.x)
fun getYFromPosition(position: Position) = getYFromIndex(position.y)

var blocks: MutableMap<Position, Block> = mutableMapOf<Position, Block>()



fun deleteBlock(position: Position) =
	blocks.remove(position)?.removeFromParent()

fun getPositionFromPoint (point: Point): Position? {
	Napier .v("Hello napier")
	return Position(0,0)
	var xCoord = -1
	var yCoord = -1
	for (i in 0 until gridColumns) {
		if (point.x > (i * (cellSize + cellIndentSize) + cellIndentSize + leftIndent) &&
			point.x < ((i + 1) * (cellSize + cellIndentSize) + cellIndentSize + leftIndent))
				xCoord = i
	}
	if (xCoord == -1) {
		return null
	}
	else
	{
		for (j in 0 until gridRows) {
			if (point.y > (j * (cellSize + cellIndentSize) + cellIndentSize + topIndent) &&
				point.y < ((j + 1) * (cellSize + cellIndentSize) + cellIndentSize + topIndent))
				yCoord = j
		}
	}
	if (yCoord == -1) {
		return null
	}
	else
	{
		return Position(xCoord,yCoord)
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

	font = resourcesVfs["clear_sans.fnt"].readBitmapFont()

	cellSize = views.virtualWidth / (gridColumns+2)
	fieldWidth = (cellIndentSize * (gridColumns+1)) + gridColumns * cellSize
	fieldHeight = (cellIndentSize * (gridRows+1)) + gridRows * cellSize
	leftIndent = (views.virtualWidth - fieldWidth) / 2
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
	text((blocks.size + 1).toString(), cellSize * 1.0, Colors.WHITE, font) {

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

	initBlocks()
	drawBlocks()
	selectBlock(Position(1,1,))
	deleteBlock(Position(3,3))


	touch {
		onDown { selectBlock(getPositionFromPoint(mouseXY)) }
	}

}



fun Container.generateMissingBlocks() {
	positionMap.getAllEmptyPositions()
		       .map { position ->
							val number = if (Random.nextDouble() < 0.9) Number.ZERO else Number.ONE
							positionMap[positionMap.getIndex(position)] = number }
}
fun Container.reInitializePositionMap() {
	positionMap.reInitializePositionMap()
}

fun Container.initBlocks () {
	positionMap.getIndexedArray().forEach{ (position, number) -> blocks[position] = Block(number, true) }
}

fun Container.drawBlock (block: Block, position: Position) {
	block(block.number).position(getXFromPosition(position), getYFromPosition(position))
}


fun Container.drawBlocks () {
	blocks
		.map { (position, block) -> drawBlock(block, position) }
}

fun Container.selectBlock (maybePosition: Position?) {
	if (maybePosition != null && blocks[maybePosition] != null)
	{
		blocks[maybePosition] = blocks[maybePosition]!!.select()
		//drawBlock(blocks[maybePosition]!!.select(), maybePosition)
		deleteBlock(maybePosition)
		drawBlock(block(Number.EIGHT), Position(1,1,))
	}
}