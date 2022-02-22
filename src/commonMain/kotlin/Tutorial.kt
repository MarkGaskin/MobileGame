import com.soywiz.korge.input.*
import com.soywiz.korge.ui.textAlignment
import com.soywiz.korge.ui.textColor
import com.soywiz.korge.ui.textSize
import com.soywiz.korge.ui.uiText
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korim.text.TextAlignment
import io.github.aakira.napier.Napier

enum class TutorialType(val index: Int,
                        val uiText: String,
                        val color: RGBA,
                        val textColor: RGBA = Colors.BLACK,
                        val topIndent: Double = 20.0) {
    MERGE(0,
        """Combine sequences of
          |3 or more blocks and
          |get a high score!""".trimMargin(), Colors["#bca8ff"], topIndent = 30.0),
    PATTERN(1, """|Special patterns are
                        |highlighted BLUE and
                        |result in BONUS blocks!""".trimMargin().trimIndent(), Colors["#99ddff"]),
    ROCKET(4, """Use a ROCKET to
                      |move one block to
                      |any other space""".trimMargin(), Colors["#ca9dd7"]),
    ROCKET2(3, """Merge a sequence of
                       |EIGHT or more blocks
                       |to get a ROCKET""".trimMargin(), Colors["#ca9dd7"]),
    BOMB(5, """Use a BOMB to
                    |destroy blocks in a jam""".trimMargin(), Colors["#990a00"]),
    BOMB2(2, """Reaching a new block
                     |tier adds a BOMB
                     |to your inventory""".trimMargin(), Colors["#990a00"])
}

data class TutorialsComplete
    (var MergeTutorial: MutableList<Boolean> = MutableList(6) { false }) {

    fun fromString (string: String): TutorialsComplete {
        string.toList().forEachIndexed { i, c -> this.MergeTutorial[i] = c == 'Y' }
        return this
    }

    override fun toString (): String {
        return this.MergeTutorial.map { complete -> if (complete) 'Y' else 'N'}.toCharArray().concatToString()
    }

    fun isIncomplete (tutorialType: TutorialType) : Boolean {
        return !this.MergeTutorial[tutorialType.index]
    }

    fun markComplete (tutorialType: TutorialType) : Unit {
        this.MergeTutorial[tutorialType.index] = true
    }

    }

fun getContainerWidth(tutorialType: TutorialType) : Int {
    return when (tutorialType) {
        TutorialType.MERGE -> fieldWidth * 4 / 5
        TutorialType.PATTERN -> fieldWidth * 4 / 5
        TutorialType.BOMB2 -> fieldWidth * 4 / 5
        TutorialType.ROCKET2 -> fieldWidth * 3 / 5
        TutorialType.ROCKET -> fieldWidth * 3 / 5
        TutorialType.BOMB -> fieldWidth * 4 / 5
    }
}

fun getContainerHeight(tutorialType: TutorialType) : Int {
    return when (tutorialType) {
        TutorialType.MERGE -> fieldWidth / 3
        TutorialType.PATTERN -> fieldWidth / 3
        TutorialType.BOMB2 -> fieldWidth / 3
        TutorialType.ROCKET2 -> fieldWidth / 3
        TutorialType.ROCKET -> fieldWidth / 3
        TutorialType.BOMB -> fieldWidth / 4
    }
}

fun getTextSize(tutorialType: TutorialType) : Int {
    return when (tutorialType) {
        TutorialType.MERGE -> cellSize / 2
        TutorialType.PATTERN -> cellSize * 3 / 5
        TutorialType.BOMB2 -> cellSize / 2
        TutorialType.ROCKET2 -> cellSize / 2
        TutorialType.ROCKET -> cellSize / 2
        TutorialType.BOMB -> cellSize / 2
    }
}

fun getSecondTutorial(tutorialType: TutorialType) : TutorialType? {
    return when (tutorialType) {
        TutorialType.MERGE,
        TutorialType.PATTERN,
        TutorialType.ROCKET2,
        TutorialType.BOMB2 -> null
        TutorialType.BOMB -> TutorialType.BOMB2
        TutorialType.ROCKET -> TutorialType.ROCKET2
    }
}

fun Container.showTutorial(tutorialType: TutorialType) = container {
    var currentTutorial = tutorialType
    showingTutorial = true
    Napier.d("Showing Tutorial Container...")
    fun closeTutorial(redrawTutorial: () -> Unit){
        tutorialsComplete.markComplete(currentTutorial)
        tutorialProperty.update(tutorialsComplete.toString())
        if (getSecondTutorial(currentTutorial) == null) {
            Napier.d("Closing Tutorial Container...")
            this@container.removeFromParent()
            showingTutorial = false
        }
        else{
            Napier.d("Showing Second Tutorial Container...")
            currentTutorial = getSecondTutorial(tutorialType)!!
            this.removeChildren()
            redrawTutorial()
        }
    }
    fun drawTutorial() {
        val restartBackground = roundRect(fieldWidth, fieldHeight, 5, fill = Colors["#aaa6a4cc"]) {
            centerXOn(gameField)
            centerYOn(gameField)
            onUp {
                Napier.d("onUp background - Tutorial pane hidden")
                closeTutorial { drawTutorial() }
            }
        }
        val bgTutorialContainer = container {
            val rect = roundRect(
                getContainerWidth(currentTutorial),
                getContainerHeight(currentTutorial),
                25,
                stroke = Colors.BLACK,
                strokeThickness = 1.0,
                fill = currentTutorial.color
            ) {
                centerXOn(restartBackground)
                centerYOn(restartBackground)
            }
            uiText(currentTutorial.uiText) {
                alignTopToTopOf(rect, currentTutorial.topIndent)
                centerXOn(rect)
                textAlignment = TextAlignment.MIDDLE_CENTER
                textSize = getTextSize(currentTutorial).toDouble()
                textColor = currentTutorial.textColor
            }
            onUp {
                Napier.d("onUp - Tutorial - YES Clicked")
                closeTutorial { drawTutorial() }
            }
        }
    }
    drawTutorial()
}