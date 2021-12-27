import com.soywiz.korge.view.Container
import com.soywiz.korge.view.centerBetween
import com.soywiz.korge.view.roundRect
import com.soywiz.korge.view.text
import com.soywiz.korim.color.Colors
import kotlin.Number

data class MagnetSelection
    (var selected: Boolean = false,
     var firstPosition: Position? = null,
     var secondPosition: Position? = null) {

    fun copy (): MagnetSelection {
        return MagnetSelection(selected, firstPosition, secondPosition)
    }

    fun unselect (): MagnetSelection {
        this.selected = false
        this.firstPosition = null
        this.secondPosition = null
        return this
    }

    fun select (): MagnetSelection {
        this.selected = true
        return this
    }

    fun toggleSelect (): MagnetSelection {
        this.selected = !this.selected
        return this
    }

    fun selectFirst (position: Position): MagnetSelection {
        this.firstPosition = position
        return this
    }

    fun unselectFirst (): MagnetSelection {
        this.firstPosition = null
        return this
    }

    fun selectSecond (position: Position): MagnetSelection {
        this.secondPosition = position
        return this
    }
}