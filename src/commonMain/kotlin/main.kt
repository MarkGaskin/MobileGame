import com.soywiz.korge.*
import com.soywiz.korge.admob.Admob
import com.soywiz.korge.admob.AdmobCreate
import com.soywiz.korge.view.*
import com.soywiz.korge.input.*
import com.soywiz.korge.service.storage.storage
import com.soywiz.korge.ui.textAlignment
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
import com.soywiz.korio.lang.Thread_sleep
import kotlin.properties.Delegates
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlin.random.Random


val score = ObservableProperty(0)
val best = ObservableProperty(0)

var gridColumns: Int = 7
var gridRows: Int = 7

val random27ID = Random.nextInt(0,gridRows * gridColumns)

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
var restartPopupContainer: Container = Container()

const val startingBombCount = 1
const val maxBombCount = 5
var bombsLoadedCount = ObservableProperty(startingBombCount)
var bombSelected = false
var bombContainer: Container = Container()

const val startingRocketCount = 1
const val maxRocketCount = 5
const val rocketPowerUpLength = 8
var rocketsLoadedCount = ObservableProperty(startingRocketCount)
var rocketSelection = RocketSelection()
var rocketContainer: Container = Container()

const val startingHighestTierReached = 3
var highestTierReached = startingHighestTierReached

var bombScaleNormal = 0.0
var bombScaleSelected = 0.0
var rocketScaleNormal = 0.0
var rocketScaleSelected = 0.0

var blockScaleNormal = 0.0
var blockScaleSelected = 0.0

var gameField = RoundRect(0.0,0.0,0.0)

const val smallSelectionSize = 3
const val mediumSelectionSize = 6
const val largeSelectionSize = 18




suspend fun main() = Korge(width = 360, height = 640, title = "2048", bgcolor = RGBA(253, 247, 240)) {
	Napier.base(DebugAntilog())



//	if (admob.available()){
//		Napier.d("Admob available")
//	} else {
//		Napier.w("Admob unavailable")
//	}

	val backgroundImg = resourcesVfs["background.png"].readBitmap()

	val background = container {
		image(backgroundImg) {
			size(views.virtualWidth, views.virtualHeight)
		}
		alignTopToTopOf(this)
		alignRightToRightOf(this)
	}

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

	gameField = roundRect(fieldWidth, fieldHeight, 5, fill = Colors["#e0d8e880"]) {
		position(leftIndent, topIndent)

		touch {
			onDown { handleDown(mouseXY) }
			onMove { handleHover(mouseXY)  }
		}
	}

	graphics {
		position(leftIndent, topIndent)
		fill(Colors["#cec0b250"]) {
			for (i in 0 until gridColumns) {
				for (j in 0 until gridRows) {
					roundRect(cellIndentSize + (cellIndentSize + cellSize) * i, cellIndentSize + (cellIndentSize + cellSize) * j, cellSize, cellSize, 5)
				}
			}
		}
	}

	val restartImg = resourcesVfs["restart.png"].readBitmap()

	val btnSize = cellSize * 1.0
	val restartBlock = container {
		val backgroundBlock = roundRect(btnSize, btnSize, 5.0, fill = Colors["#639cd9"])
		image(restartImg) {
			size(btnSize * 0.8, btnSize * 0.8)
			centerOn(backgroundBlock)
		}
		alignLeftToLeftOf(gameField, cellSize*0.5)
		alignBottomToTopOf(gameField, cellSize * 0.75)
		onClick {
			if(!showingRestart) {
				unselectAllPowerUps()
				restartPopupContainer = this@Korge.showRestart { this@Korge.restart() }
				Napier.d("Restart Button Clicked")
			}
			else{
				Napier.d("Restart Button Clicked when already showing restart")
				showingRestart = false
				restartPopupContainer.removeFromParent()
			}
		}
	}

	val bgScore = roundRect(cellSize * 2.5, cellSize*1.5, 5.0, fill = Colors["#9182c4"]) {
		alignLeftToRightOf(restartBlock, cellSize)
		alignBottomToTopOf(gameField, cellSize * 0.5)
	}
	text("SCORE", cellSize * 0.5, Colors["#ebd9dd"], font) {
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

	val bgBest = roundRect(cellSize * 2.5, cellSize * 1.5, 5.0, fill = Colors["#9182c4"]) {
		alignRightToRightOf(gameField, 12.0)
		alignBottomToTopOf(gameField, cellSize * 0.5)
	}
	text("BEST", cellSize * 0.5, Colors["#ebd9dd"], font) {
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


	val emptyBombImg = resourcesVfs["emptyBomb.png"].readBitmap()
	val loadedBombImg = resourcesVfs["loadedBomb.png"].readBitmap()
	val emptyRocketImg = resourcesVfs["emptyRocket.png"].readBitmap()
	val loadedRocketImg = resourcesVfs["loadedRocket.png"].readBitmap()

	bombContainer = container {
		val bombBackground = roundRect(cellSize*2.0, cellSize*1.5, 10.0, fill=Colors["#e6e6e6A0"])
		alignTopToBottomOf(gameField, 18)
		alignLeftToLeftOf(gameField, fieldWidth/5)
		image(if (bombsLoadedCount.value > 0) loadedBombImg else emptyBombImg ){
			size(65, 60)
			centerOn(bombBackground)
		}
		onClick {
			if(bombsLoadedCount.value > 0 && !showingRestart) {
				bombSelected = !bombSelected
				animatePowerUpSelection(this, bombSelected)
			}
		}
		bombsLoadedCount.observe {
			this.removeChildrenIf{ index, _ -> index == 1}
			image(if (bombsLoadedCount.value > 0) loadedBombImg else emptyBombImg ){
				size(65, 60)
				centerOn(bombBackground)
			}
		}
	}

	container {
		alignTopToBottomOf(bombContainer, 2)
		alignRightToRightOf(bombContainer)
		alignLeftToLeftOf(bombContainer)

		val emptyFill = Colors["#e6e6e6A0"]
		val loadedFill = Colors["#e04b5a"]

		var cart1Fill = emptyFill
		var cart2Fill = emptyFill
		var cart3Fill = emptyFill
		var cart4Fill = emptyFill
		var cart5Fill = emptyFill

		fun fillByBombCount () {
			cart1Fill = if (bombsLoadedCount.value > 0) loadedFill else emptyFill
			cart2Fill = if (bombsLoadedCount.value > 1) loadedFill else emptyFill
			cart3Fill = if (bombsLoadedCount.value > 2) loadedFill else emptyFill
			cart4Fill = if (bombsLoadedCount.value > 3) loadedFill else emptyFill
			cart5Fill = if (bombsLoadedCount.value > 4) loadedFill else emptyFill
		}

		val strokeThickness = 1.5
		fun drawCartridges () {
			val cart1 = roundRect(
				cellSize * 2.0 / 5.0,
				cellSize * 0.5,
				5.0,
				fill = cart1Fill,
				stroke = Colors.WHITE,
				strokeThickness = strokeThickness
			) {
				alignLeftToLeftOf(this)
			}
			val cart2 = roundRect(
				cellSize * 2.0 / 5.0,
				cellSize * 0.5,
				5.0,
				fill = cart2Fill,
				stroke = Colors.WHITE,
				strokeThickness = strokeThickness
			) {
				alignLeftToRightOf(cart1)
			}
			val cart3 = roundRect(
				cellSize * 2.0 / 5.0,
				cellSize * 0.5,
				5.0,
				fill = cart3Fill,
				stroke = Colors.WHITE,
				strokeThickness = strokeThickness
			) {
				alignLeftToRightOf(cart2)
			}
			val cart4 = roundRect(
				cellSize * 2.0 / 5.0,
				cellSize * 0.5,
				5.0,
				fill = cart4Fill,
				stroke = Colors.WHITE,
				strokeThickness = strokeThickness
			) {
				alignLeftToRightOf(cart3)
			}
			val cart5 = roundRect(
				cellSize * 2.0 / 5.0,
				cellSize * 0.5,
				5.0,
				fill = cart5Fill,
				stroke = Colors.WHITE,
				strokeThickness = strokeThickness
			) {
				alignLeftToRightOf(cart4)
			}
		}

		fillByBombCount ()
		drawCartridges()

		bombsLoadedCount.observe {
			this.removeChildren()
			fillByBombCount ()
			drawCartridges()
		}

	}

	rocketContainer = container {
		val rocketBackground = roundRect(cellSize*2.0, cellSize*1.5, 10.0, fill=Colors["#e6e6e6A0"])
		val rocketWidth = 43
		val rocketHeight = 60
		alignTopToBottomOf(gameField, 18)
		alignRightToRightOf(gameField, fieldWidth/5)
		image(if (rocketsLoadedCount.value > 0) loadedRocketImg else emptyRocketImg ){
			size(rocketWidth, rocketHeight)
			centerOn(rocketBackground)
		}
		onClick {
			if(rocketsLoadedCount.value > 0 && !showingRestart) {
				rocketSelection.toggleSelect()
				animatePowerUpSelection(this, rocketSelection.selected)
				if (!rocketSelection.selected) removeRocketSelection()
			}
		}
		rocketsLoadedCount.observe {
			this.removeChildrenIf{ index, _ -> index == 1}
			image(if (rocketsLoadedCount.value > 0) loadedRocketImg else emptyRocketImg ){
				size(rocketWidth, rocketHeight)
				centerOn(rocketBackground)
			}
		}
	}

	container {
		alignTopToBottomOf(rocketContainer, 2)
		alignRightToRightOf(rocketContainer)
		alignLeftToLeftOf(rocketContainer)

		val emptyFill = Colors["#e6e6e6A0"]
		val loadedFill = Colors["#ca9dd7"]

		var cart1Fill = emptyFill
		var cart2Fill = emptyFill
		var cart3Fill = emptyFill
		var cart4Fill = emptyFill
		var cart5Fill = emptyFill

		fun fillByRocketCount () {
			cart1Fill = if (rocketsLoadedCount.value > 0) loadedFill else emptyFill
			cart2Fill = if (rocketsLoadedCount.value > 1) loadedFill else emptyFill
			cart3Fill = if (rocketsLoadedCount.value > 2) loadedFill else emptyFill
			cart4Fill = if (rocketsLoadedCount.value > 3) loadedFill else emptyFill
			cart5Fill = if (rocketsLoadedCount.value > 4) loadedFill else emptyFill
		}

		val strokeThickness = 1.5
		fun drawCartridges () {
			val cart1 = roundRect(
				cellSize * 2.0 / 5.0,
				cellSize * 0.5,
				5.0,
				fill = cart1Fill,
				stroke = Colors.WHITE,
				strokeThickness = strokeThickness
			) {
				alignLeftToLeftOf(this)
			}
			val cart2 = roundRect(
				cellSize * 2.0 / 5.0,
				cellSize * 0.5,
				5.0,
				fill = cart2Fill,
				stroke = Colors.WHITE,
				strokeThickness = strokeThickness
			) {
				alignLeftToRightOf(cart1)
			}
			val cart3 = roundRect(
				cellSize * 2.0 / 5.0,
				cellSize * 0.5,
				5.0,
				fill = cart3Fill,
				stroke = Colors.WHITE,
				strokeThickness = strokeThickness
			) {
				alignLeftToRightOf(cart2)
			}
			val cart4 = roundRect(
				cellSize * 2.0 / 5.0,
				cellSize * 0.5,
				5.0,
				fill = cart4Fill,
				stroke = Colors.WHITE,
				strokeThickness = strokeThickness
			) {
				alignLeftToRightOf(cart3)
			}
			val cart5 = roundRect(
				cellSize * 2.0 / 5.0,
				cellSize * 0.5,
				5.0,
				fill = cart5Fill,
				stroke = Colors.WHITE,
				strokeThickness = strokeThickness
			) {
				alignLeftToRightOf(cart4)
			}
		}

		fillByRocketCount ()
		drawCartridges()

		rocketsLoadedCount.observe {
			this.removeChildren()
			fillByRocketCount ()
			drawCartridges()
		}

	}


	bombScaleNormal = bombContainer.scale
	bombScaleSelected = bombScaleNormal * 1.2
	rocketScaleNormal = rocketContainer.scale
	rocketScaleSelected = rocketScaleNormal * 1.2

	Napier.d("UI Initialized")

	blocksMap = initializeRandomBlocksMap ()
	drawAllBlocks()


	blockScaleNormal = blocksMap[Position(0,0)]!!.scale
	blockScaleSelected = blockScaleNormal * 1.2

	val admob = AdmobCreate(this.views, testing = false)

//	if (admob.available()){
//		Napier.d("Admob available")
//	} else {
//		Napier.w("Admob unavailable")
//	}
//
//	var bannerConfig = Admob.Config(id="ca-app-pub-3940256099942544/6300978111")

//	admob.bannerPrepare(bannerConfig)
//	admob.bannerShow()

	//admob.interstitialWaitAndShow(Admob.Config(id="ca-app-pub-3940256099942544/1033173712"))



	touch {
		onUp { handleUp(mouseXY) }
	}

}

fun Container.showGameOver(onGameOver: () -> Unit) = container {
	showingRestart = true
	Napier.d("Showing Restart Container...")
	fun restart() {
		this@container.removeFromParent()
		onGameOver()
	}

	val restartBackground = roundRect(fieldWidth, fieldHeight, 5, fill = Colors["#aaa6a4cc"]) {
		centerXOn(gameField)
		centerYOn(gameField)
	}
	val bgRestartContainer = container {
		roundRect(fieldWidth / 2, fieldHeight / 4, 25, fill = Colors["#bbd0f2"]) {
			centerXOn(restartBackground)
			centerYOn(restartBackground)
		}
		uiText("Restart?") {
			centerXOn(restartBackground)
			centerYOn(restartBackground)

			textAlignment = TextAlignment.MIDDLE_CENTER
			textSize = 30.0
			textColor = RGBA(0, 0, 0)
			onOver { textColor = RGBA(90, 90, 90) }
			onOut { textColor = RGBA(0, 0, 0) }
			onDown { textColor = RGBA(120, 120, 120) }
			onUp { textColor = RGBA(120, 120, 120) }
		}
		onUp {
			Napier.d("Restart Button - YES Clicked")
			showingRestart = false
			restart()
			this@container.removeFromParent()
		}
		onClick {
			Napier.d("Restart Button - YES Clicked")
			showingRestart = false
			restart()
			this@container.removeFromParent()
		}
	}
	val gameOverText = container {
		alignBottomToTopOf(bgRestartContainer, cellSize * 1.0)
		centerXOn(bgRestartContainer)
		text("Out of moves") {
			alignment = TextAlignment.MIDDLE_CENTER
			textSize = 50.0
			color = RGBA(0, 0, 0)
		}
	}
}

fun Stage.unselectAllPowerUps (): Unit {
	if (bombSelected) {
		bombSelected = false
		animatePowerUpSelection(bombContainer, false)
	}
	if (rocketSelection.selected){
		rocketSelection.unselect()
		animatePowerUpSelection(rocketContainer, false)
	}
}

fun Container.showRestart(onRestart: () -> Unit) = container {
	showingRestart = true
	Napier.d("Showing Restart Container...")
	fun restart() {
		this@container.removeFromParent()
		onRestart()
	}

	val restartBackground = roundRect(fieldWidth, fieldHeight, 5, fill = Colors["#aaa6a4cc"]) {
		centerXOn(gameField)
		centerYOn(gameField)
		onClick {
			Napier.d("Restart Button - NO Clicked")
			showingRestart = false
			this@container.removeFromParent()
		}
	}
	val bgRestartContainer = container {
		roundRect(fieldWidth / 2, fieldHeight / 4, 25, fill = Colors["#bbd0f2"]) {
			centerXOn(restartBackground)
			centerYOn(restartBackground)
		}
		uiText("Restart?") {
			centerXOn(restartBackground)
			centerYOn(restartBackground)

			textAlignment = TextAlignment.MIDDLE_CENTER
			textSize = 30.0
			textColor = RGBA(0, 0, 0)
			onOver { textColor = RGBA(90, 90, 90) }
			onOut { textColor = RGBA(0, 0, 0) }
			onDown { textColor = RGBA(120, 120, 120) }
			onUp { textColor = RGBA(120, 120, 120) }
		}
		onUp {
			Napier.d("Restart Button - YES Clicked")
			showingRestart = false
			restart()
			this@container.removeFromParent()
		}
		onClick {
			Napier.d("Restart Button - YES Clicked")
			showingRestart = false
			restart()
			this@container.removeFromParent()
		}
	}
}
fun Container.restart() {
	Napier.d("Running Restart Function...")
	score.update(0)
	highestTierReached = startingHighestTierReached
	bombsLoadedCount.update(startingBombCount)
	rocketsLoadedCount.update(startingRocketCount)
	blocksMap.values.forEach { it.removeFromParent() }
	blocksMap.clear()
	blocksMap = initializeRandomBlocksMap ()
	drawAllBlocks()
}
