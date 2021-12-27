import com.soywiz.korge.*
import com.soywiz.korge.view.*
import com.soywiz.korge.input.*
import com.soywiz.korge.service.storage.storage
import com.soywiz.korge.ui.textColor
import com.soywiz.korge.ui.textSize
import com.soywiz.korge.ui.uiText
import com.soywiz.korim.color.*
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.geom.vector.*
import com.soywiz.korim.font.*
import com.soywiz.korim.text.TextAlignment
import com.soywiz.korio.async.ObservableProperty
import kotlin.properties.Delegates

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlin.random.Random


val score = ObservableProperty(0)
val best = ObservableProperty(0)

var gridColumns: Int = 7
var gridRows: Int = 7

val random27ID = Random.nextInt(0,gridRows * gridColumns - 1)

var cellIndentSize: Int = 8
var cellSize: Int = 0
var fieldWidth: Int = 0
var fieldHeight: Int = 0
var leftIndent: Int = 0
var topIndent: Int = 0
var nextBlockId = 0

var fieldSize: Double = 0.0

var isPressed = false
var isBombPressed = false

var font: BitmapFont by Delegates.notNull()


var blocksMap: MutableMap<Position, Block> = mutableMapOf()

var hoveredPositions: MutableList<Position> = mutableListOf()
var hoveredBombPositions: MutableList<Position> = mutableListOf()

var isAnimating: Boolean = false
fun startAnimating() { isAnimating = true }
fun stopAnimating() { isAnimating = false }

var showingRestart: Boolean = false


const val startingBombCount = 1
const val maxBombCount = 3
var bombsLoadedCount = ObservableProperty(startingBombCount)
var bombSelected = false
var bombContainer: Container = Container()

const val startingMagnetCount = 1
const val maxMagnetCount = 3
var magnetsLoadedCount = ObservableProperty(startingMagnetCount)
var magnetSelection = MagnetSelection()
var magnetContainer: Container = Container()

var highestTierReached = 3

var bombScaleNormal = 0.0
var bombScaleSelected = 0.0
var magnetScaleNormal = 0.0
var magnetScaleSelected = 0.0




suspend fun main() = Korge(width = 480, height = 800, title = "2048", bgcolor = RGBA(253, 247, 240)) {
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

		touch {
			onDown { handleDown(mouseXY) }
			onMove { handleHover(mouseXY)  }
		}
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
		onClick {
			if(!showingRestart) {
				this@Korge.showRestart { this@Korge.restart() }
				Napier.d("Restart Button Clicked")
			}
			else{
				Napier.d("Restart Button Clicked when already showing restart")
			}
		}
	}


	val emptyBombImg = resourcesVfs["emptyBomb.png"].readBitmap()
	val loadedBombImg = resourcesVfs["loadedBomb.png"].readBitmap()
	val emptyMagnetImg = resourcesVfs["emptyBomb.png"].readBitmap()
	val loadedMagnetImg = resourcesVfs["loadedBomb.png"].readBitmap()

	bombContainer = container {
		val bombBackground = circle(40.0, Colors["#fae6b4"])
		alignTopToBottomOf(backgroundRect, 18)
		alignLeftToLeftOf(backgroundRect, fieldWidth/5)
		image(if (bombsLoadedCount.value > 0) loadedBombImg else emptyBombImg ){
			size(40 * .8, 40 * .8)
			centerOn(bombBackground)
		}
		onClick {
			if(bombsLoadedCount.value > 0 && !showingRestart) {
				bombSelected = !bombSelected
				animateSelection(this, bombSelected)
			}
		}
		bombsLoadedCount.observe {
			this.removeChildrenIf{ index, _ -> index == 1}
			image(if (bombsLoadedCount.value > 0) loadedBombImg else emptyBombImg ){
				size(40 * .8, 40 * .8)
				centerOn(bombBackground)
			}
		}
	}
	magnetContainer = container {
		val bombBackground = circle(40.0, Colors["#fae6b4"])
		alignTopToBottomOf(backgroundRect, 18)
		alignRightToRightOf(backgroundRect, fieldWidth/5)
		image(if (magnetsLoadedCount.value > 0) loadedMagnetImg else emptyMagnetImg ){
			size(40 * .8, 40 * .8)
			centerOn(bombBackground)
		}
		onClick {
			if(magnetsLoadedCount.value > 0 && !showingRestart) {
				magnetSelection.toggleSelect()
				animateSelection(this, magnetSelection.selected)
				if (!magnetSelection.selected) this.parent?.removeMagnetSelection()
			}
		}
		magnetsLoadedCount.observe {
			this.removeChildrenIf{ index, _ -> index == 1}
			image(if (magnetsLoadedCount.value > 0) loadedMagnetImg else emptyMagnetImg ){
				size(40 * .8, 40 * .8)
				centerOn(bombBackground)
			}
		}
	}


	bombScaleNormal = bombContainer.scale
	bombScaleSelected = bombScaleNormal * 1.2
	magnetScaleNormal = magnetContainer.scale
	magnetScaleSelected = magnetScaleNormal * 1.2

	Napier.d("UI Initialized")

	blocksMap = initializeRandomBlocksMap ()
	drawAllBlocks()


	touch {
		onUp { handleUp(mouseXY) }
	}

}

fun Container.showGameOver(onGameOver: () -> Unit) = container {
	showingRestart = true
	Napier.d("Showing GameOver Container...")
	fun restart() {
		this@container.removeFromParent()
		onGameOver()
	}
	position(leftIndent, topIndent)

	val bgGameOverContainer = roundRect(435.0, 435.0, 5.0, fill = Colors["#aaa6a4cc"])
	val bgGameOverText = roundRect(cellSize * 6.5, cellSize * 4.0, 25.0, fill = Colors["#aaa6a4"]) {
		centerXOn(bgGameOverContainer)
		y -= -110
	}
	val bgTryAgainText = roundRect(cellSize * 4.0, cellSize * 1.0, 20.0, fill = Colors["#aaa6a4"], stroke = Colors.BLACK, strokeThickness = 1.5) {
		centerXOn(bgGameOverText)
		y -= -230
	}

	text("Game Over...", 60.0, Colors.BLACK, font, ) {
		centerBetween(425.0, 0.0, fieldSize, fieldSize)
		y -= -160
	}

	uiText("Try again?", 120.0, 35.0) {
		centerXOn(bgTryAgainText)
		y -= -237
		x += -5
		textSize = 30.0
		textColor = RGBA(0, 0, 0)
		onOver { textColor = RGBA(90, 90, 90) }
		onOut { textColor = RGBA(0, 0, 0) }
		onDown { textColor = RGBA(120, 120, 120) }
		onUp { textColor = RGBA(120, 120, 120) }
		onClick {
			Napier.d("Try again Button Clicked")
			restart()
			showingRestart = false
			this@container.removeFromParent()
		}
	}

}

fun Container.showRestart(onRestart: () -> Unit) = container {
	showingRestart = true
	Napier.d("Showing Restart Container...")
	fun restart() {
		this@container.removeFromParent()
		onRestart()
	}

	position(leftIndent, topIndent)
	val bgRestartContainer = roundRect(435.0, 435.0, 5.0, fill = Colors["#aaa6a4cc"])
	val bgRestartText = roundRect(cellSize * 6.5, cellSize * 4.0, 25.0, fill = Colors["#aaa6a4"]) {
		centerXOn(bgRestartContainer)
		y -= -110
	}
	// bgYesText
	roundRect(cellSize * 2.0, cellSize * 1.0, 20.0, fill = Colors["#aaa6a4"], stroke = Colors.BLACK, strokeThickness = 1.5) {
		centerXOn(bgRestartContainer)
		y -= -225
		x += -63
	}
	// bgNoText
	roundRect(cellSize * 2.0, cellSize * 1.0, 20.0, fill = Colors["#aaa6a4"], stroke = Colors.BLACK, strokeThickness = 1.5) {
		centerXOn(bgRestartContainer)
		y -= -225
		x += 63
	}
	text("Restart?", 60.0, Colors.BLACK, font, ) {
		centerXOn(bgRestartText)
		y -= -140
	}
	uiText("Yes", 120.0, 35.0) {
		centerBetween(380.0, 505.0, fieldSize, fieldSize)
		textSize = 30.0
		textColor = RGBA(0, 0, 0)
		onOver { textColor = RGBA(90, 90, 90) }
		onOut { textColor = RGBA(0, 0, 0) }
		onDown { textColor = RGBA(120, 120, 120) }
		onUp { textColor = RGBA(120, 120, 120) }
		onClick {
			Napier.d("Restart Button - YES Clicked")
			restart()
			showingRestart = false
			this@container.removeFromParent()
		}
	}
	uiText("No", 120.0, 35.0) {
		centerBetween(650.0, 505.0, fieldSize, fieldSize)
		textSize = 30.0
		textColor = RGBA(0, 0, 0)
		onOver { textColor = RGBA(90, 90, 90) }
		onOut { textColor = RGBA(0, 0, 0) }
		onDown { textColor = RGBA(120, 120, 120) }
		onUp { textColor = RGBA(120, 120, 120) }
		onClick {
			Napier.d("Restart Button - NO Clicked")
			showingRestart = false
			this@container.removeFromParent()
		}
	}
}
fun Container.restart() {
	Napier.d("Running Restart Function...")
	score.update(0)
	bombsLoadedCount.update(startingBombCount)
	magnetsLoadedCount.update(startingMagnetCount)
	blocksMap.values.forEach { it.removeFromParent() }
	blocksMap.clear()
	blocksMap = initializeRandomBlocksMap ()
	drawAllBlocks()
}
