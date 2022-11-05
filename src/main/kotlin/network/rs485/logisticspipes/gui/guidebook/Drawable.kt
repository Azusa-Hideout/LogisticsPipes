/*
 * Copyright (c) 2020  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2020  RS485
 *
 * This MIT license was reworded to only match this file. If you use the regular
 * MIT license in your project, replace this copyright notice (this line and any
 * lines below and NOT the copyright line above) with the lines from the original
 * MIT license located here: http://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this file and associated documentation files (the "Source Code"), to deal in
 * the Source Code without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Source Code, and to permit persons to whom the Source Code is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Source Code, which also can be
 * distributed under the MIT.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package network.rs485.logisticspipes.gui.guidebook

import logisticspipes.utils.MinecraftColor
import net.minecraft.client.Minecraft
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.audio.SoundHandler
import net.minecraft.init.SoundEvents
import net.minecraft.util.SoundEvent
import network.rs485.logisticspipes.gui.LPGuiDrawer
import network.rs485.logisticspipes.gui.guidebook.GuideBookConstants.DRAW_BODY_WIREFRAME
import network.rs485.logisticspipes.util.IRectangle
import network.rs485.logisticspipes.util.Rectangle
import network.rs485.logisticspipes.util.math.MutableRectangle

interface MouseHoverable {
    /**
     * Check if mouse is over the current object.
     * @param mouseX X position of the mouse (absolute, screen)
     * @param mouseY Y position of the mouse (absolute, screen)
     */
    fun isMouseHovering(mouseX: Float, mouseY: Float): Boolean = false
}

interface MouseInteractable : MouseHoverable {

    /**
     * A mouse click event should run this.
     * @param mouseX X position of the mouse (absolute, screen)
     * @param mouseY Y position of the mouse (absolute, screen)
     * @param mouseButton button of the mouse that was pressed.
     * @return true, if click was handled
     */
    fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean = false

    /**
     * Mouse scroll event, run this.
     * @param mouseX X position of the mouse (absolute, screen)
     * @param mouseY Y position of the mouse (absolute, screen)
     * @param scrollAmount how much the scroll wheel has turned since the last event.
     */
    fun mouseScrolled(mouseX: Float, mouseY: Float, scrollAmount: Float): Boolean = false

    /**
     * A mouse release event should run this.
     * @param mouseX X position of the mouse (absolute, screen)
     * @param mouseY Y position of the mouse (absolute, screen)
     * @param mouseButton button of the mouse that was pressed.
     */
    fun mouseReleased(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean = false

    /**
     * Always call this method when mouse clicked is successful.
     * @param soundHandler minecraft's sound handler
     */
    fun playPressedSound(soundHandler: SoundHandler, sound: SoundEvent = SoundEvents.UI_BUTTON_CLICK) {
        soundHandler.playSound(PositionedSoundRecord.getMasterRecord(sound, 1.0f))
    }

}

interface Drawable {
    companion object {
        /**
         * Assigns the parent of all children to this.
         */
        fun <T : Drawable> List<Drawable>.createParent(parentGetter: () -> T) =
            parentGetter().also { parentDrawable -> this.forEach { it.parent = parentDrawable } }
    }

    val relativeBody: MutableRectangle

    var parent: Drawable?

    /** Relative x position. */
    val x: Float get() = relativeBody.x0

    /** Relative y position. */
    val y: Float get() = relativeBody.y0

    /** Drawable's width. */
    val width: Int get() = relativeBody.roundedWidth

    /** Drawable's height */
    val height: Int get() = relativeBody.roundedHeight

    /** Absolute left position. */
    val left: Float get() = (parent?.left ?: 0.0f) + x

    /** Absolute right position. */
    val right: Float get() = left + width

    /** Absolute top position. */
    val top: Float get() = (parent?.top ?: 0.0f) + y

    /** Absolute bottom position. */
    val bottom: Float get() = top + height

    /** Absolute drawable body. */
    val absoluteBody: Rectangle
        get() = Rectangle(left to top, right to bottom)

    /**
     * Assigns a new child's parent to this.
     */
    fun <T : Drawable> createChild(childGetter: () -> T) = childGetter().also { it.parent = this }

    /**
     * This is just like the normal draw functions for minecraft Gui classes but with the added current Y offset.
     * @param mouseX        X position of the mouse (absolute, screen)
     * @param mouseY        Y position of the mouse (absolute, screen)
     * @param delta         Timing floating value
     * @param visibleArea   used to avoid draw calls on non-visible children
     */
    fun draw(mouseX: Float, mouseY: Float, delta: Float, visibleArea: IRectangle) {
        if (DRAW_BODY_WIREFRAME) {
            val visibleAbsoluteBody = MutableRectangle.fromRectangle(visibleArea)
                .translate(0, -5)
                .grow(0, 10)
                .overlap(absoluteBody)
            LPGuiDrawer.drawOutlineRect(visibleAbsoluteBody, MinecraftColor.WHITE.colorCode)
        }
    }

    /**
     * This function is responsible for updating the Drawable's position by giving it the exact X and Y where it
     * should start and returning the offset for the next element.
     * @param x         the X position of the Drawable.
     * @param y         the Y position of the Drawable.
     * @return returns width and height of the drawable.
     */
    fun setPos(x: Int, y: Int): Pair<Int, Int> {
        relativeBody.setPos(x, y)
        return relativeBody.roundedWidth to relativeBody.roundedHeight
    }

    /**
     * This function is responsible to check if the current Drawable is within the vertical constrains of the given area.
     * @param visibleArea   Desired visible area to check
     * @return true if within constraints false otherwise.
     */
    fun visible(visibleArea: IRectangle): Boolean {
        return visibleArea.intersects(absoluteBody)
    }
}

object Screen : Drawable {
    val screen: MutableRectangle
        get() = MutableRectangle(
            width = Minecraft.getMinecraft().currentScreen?.width ?: Minecraft.getMinecraft().displayWidth,
            height = Minecraft.getMinecraft().currentScreen?.height ?: Minecraft.getMinecraft().displayHeight,
        )

    override val relativeBody: MutableRectangle
        get() = screen

    override var parent: Drawable? = null

    val xCenter: Int
        get() = relativeBody.roundedWidth / 2
    val yCenter: Int
        get() = relativeBody.roundedHeight / 2
}

val <T> Pair<T, T>.x: T
    get() = first

val <T> Pair<T, T>.y: T
    get() = second

fun Pair<Int, Int>.plus() = Pair(this.x + x, this.y + y)

fun Pair<Int, Int>.minus() = Pair(this.x - x, this.y - y)