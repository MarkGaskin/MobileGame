import com.soywiz.korge.view.Container
import com.soywiz.korge.view.Stage
import com.soywiz.korma.geom.Point
import io.github.aakira.napier.Napier


fun getXFromIndex(index: Int) = leftIndent + cellIndentSize + (cellSize + cellIndentSize) * index
fun getYFromIndex(index: Int) = topIndent + cellIndentSize + (cellSize + cellIndentSize) * index
fun getXFromPosition(position: Position) = getXFromIndex(position.x)
fun getYFromPosition(position: Position) = getYFromIndex(position.y)

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

fun Container.handleDown(point: Point){
    when (true) {
        isAnimating -> return
        showingRestart -> return
        bomb1Selected -> {
            isPressed = true
            drawBombHover(getPositionFromPoint(point)) }
        else -> {
            isPressed = true
            return pressDown(getPositionFromPoint(point)) }
    }
}

fun Container.handleHover(point: Point){
    if (isPressed) {
        when (true) {
            isAnimating -> return
            showingRestart -> return
            bomb1Selected ->{
                removeBombHover()
                drawBombHover(getPositionFromPoint(point))
            }
            else -> hoverBlock(getPositionFromPoint(point))
        }
    }
}

fun Stage.handleUp(point: Point){
    isPressed = false
    when (true) {
        isAnimating -> return
        showingRestart -> return
        bomb1Selected ->{
            val maybePosition = getPositionFromPoint(point)
            if (maybePosition == null)
                removeBombHover()
            else
                animateBomb()
        }
        else -> pressUp(getPositionFromPoint(point))
    }
}

fun Container.pressDown (maybePosition: Position?) {
    if (maybePosition != null)
    {
        if (blocksMap[maybePosition] != null)
        {
            Napier.v("Selecting Block at Position(${maybePosition.x},${maybePosition.y})")
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


fun Stage.pressUp (maybePosition: Position?) {
    isPressed = false
    if (maybePosition is Position) {
        Napier.v("Releasing Block at Position(${maybePosition.x},${maybePosition.y})")
    }
    else{
        Napier.v("Releasing Block outside of field")
    }
    if (atLeastThreeSelected()) {
        successfulShape()
    }
    else
    {
        unsuccessfulShape()
    }

}



fun Container.hoverBlock (maybePosition: Position?) {
    if (maybePosition != null && (hoveredPositions.size > 0 && hoveredPositions.last() != maybePosition)) {
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