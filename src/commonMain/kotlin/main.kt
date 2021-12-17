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


val score = ObservableProperty(0)
val best = ObservableProperty(0)

var gridColumns: Int = 7
var gridRows: Int = 7

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

var bomb1Loaded = ObservableProperty(true)
var bomb1Selected = false
var bomb2Loaded = ObservableProperty(false)
var bomb2Selected = false
var bomb1container: Container = Container()
var bomb2container: Container = Container()

var highestTierReached = 3

var bombScaleNormal = 0.0
var bombScaleSelected = 0.0




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
				Napier.w("Restart Button Clicked")
			}
			else{
				Napier.w("Restart Button Clicked when already showing restart")
			}
		}
	}


	val emptyBombImg = resourcesVfs["emptyBomb.png"].readBitmap()
	val loadedBombImg = resourcesVfs["loadedBomb.png"].readBitmap()

	bomb1container = container {
		val bombBackground = circle(40.0, Colors["#fae6b4"])
		alignTopToBottomOf(backgroundRect, 18)
		alignLeftToLeftOf(backgroundRect, fieldWidth/5)
		image(if (bomb1Loaded.value) loadedBombImg else emptyBombImg ){
			size(40 * .8, 40 * .8)
			centerOn(bombBackground)
		}
		onClick {
			if(bomb1Loaded.value) {
				bomb1Selected = !bomb1Selected
				animateBombSelection(this, bomb1Selected)
			}
		}
		bomb1Loaded.observe {
			this.removeChildrenIf{ index, _ -> index == 1}
			image(if (bomb1Loaded.value) loadedBombImg else emptyBombImg ){
				size(40 * .8, 40 * .8)
				centerOn(bombBackground)
			}
		}
	}
	bomb2container = container {
		val bombBackground = circle(40.0, Colors["#fae6b4"])
		alignTopToBottomOf(backgroundRect, 18)
		alignRightToRightOf(backgroundRect, fieldWidth/5)
		image(if (bomb2Loaded.value) loadedBombImg else emptyBombImg ){
			size(40 * .8, 40 * .8)
			centerOn(bombBackground)
		}
		onClick {
			if(bomb2Loaded.value) {
				bomb2Selected = !bomb2Selected
				animateBombSelection(this, bomb2Selected)
			}
		}
		bomb2Loaded.observe {
			this.removeChildrenIf{ index, _ -> index == 1}
			image(if (bomb2Loaded.value) loadedBombImg else emptyBombImg ){
				size(40 * .8, 40 * .8)
				centerOn(bombBackground)
			}
		}
	}


	bombScaleNormal = bomb1container.scale
	bombScaleSelected = bombScaleNormal * 1.2

	Napier.d("UI Initialized")

	blocksMap = initializeRandomBlocksMap ()
	drawAllBlocks()


	touch {
		onUp { handleUp(mouseXY) }
	}

}



fun Container.showRestart(onRestart: () -> Unit) = container {
	showingRestart = true
	Napier.w("Showing Restart Container...")
	fun restart() {
		this@container.removeFromParent()
		onRestart()
	}

	position(leftIndent, topIndent)

	roundRect(fieldSize, fieldSize, 5.0, fill = Colors["#FFFFFF33"])
	text("Restart?", 60.0, Colors.BLACK, font) {
		centerBetween(400.0, 400.0, fieldSize, fieldSize)
		y -= 60
	}
	uiText("Yes", 120.0, 35.0) {
		centerBetween(400.0, 400.0, fieldSize, fieldSize)
		y += 20
		textSize = 40.0
		textColor = RGBA(0, 0, 0)
		onOver { textColor = RGBA(90, 90, 90) }
		onOut { textColor = RGBA(0, 0, 0) }
		onDown { textColor = RGBA(120, 120, 120) }
		onUp { textColor = RGBA(120, 120, 120) }
		onClick {
			Napier.w("Restart Button - YES Clicked")
			restart()
			showingRestart = false
			this@container.removeFromParent()
		}
	}
	uiText("No", 120.0, 35.0) {
		centerBetween(550.0, 400.0, fieldSize, fieldSize)
		y += 20
		textSize = 40.0
		textColor = RGBA(0, 0, 0)
		onOver { textColor = RGBA(90, 90, 90) }
		onOut { textColor = RGBA(0, 0, 0) }
		onDown { textColor = RGBA(120, 120, 120) }
		onUp { textColor = RGBA(120, 120, 120) }
		onClick {
			Napier.w("Restart Button - NO Clicked")
			showingRestart = false
			this@container.removeFromParent()
		}
	}
	//	TODO: Somehow add background opacity that way he text is more visible
}
fun Container.restart() {
	Napier.w("Running Restart Function...")
	score.update(0)
	blocksMap.values.forEach { it.removeFromParent() }
	blocksMap.clear()
	blocksMap = initializeRandomBlocksMap ()
	drawAllBlocks()
}





