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
import network.rs485.logisticspipes.gui.HorizontalAlignment
import network.rs485.logisticspipes.gui.LPGuiDrawer
import network.rs485.logisticspipes.gui.VerticalAlignment
import network.rs485.logisticspipes.util.math.Rectangle

interface TabButtonReturn {
    fun onLeftClick(): Boolean
    fun onRightClick(shiftClick: Boolean, ctrlClick: Boolean): Boolean
    fun getColor(): Int
    fun isPageActive(): Boolean
}

private val buttonTextureArea = Rectangle(40, 64, 24, 32)
private val circleAreaTexture = Rectangle(32, 96, 16, 16)

class TabButton(
    internal val tabPage: Page,
    x: Int,
    y: Int,
    private val whisky: TabButtonReturn,
) : LPGuiButton(99, x, y - 24, 24, 32) {

    override val bodyTrigger = Rectangle(1, 1, 22, 22)
    private val circleArea = Rectangle(4, 4, 16, 16)
    val isActive: Boolean
        get() = whisky.isPageActive()
    val isInactive: Boolean
        get() = !isActive


    fun onLeftClick() = whisky.onLeftClick()

    fun onRightClick(shiftClick: Boolean, ctrlClick: Boolean) = whisky.onRightClick(shiftClick, ctrlClick)

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int, partialTicks: Float) {
        hovered = isHovered(mouseX, mouseY)
        if (!visible) return
        if (isInactive) {
            val yOffset = if (whisky.isPageActive()) 0 else 3
            val color: Int = (MinecraftColor.values()[whisky.getColor()].colorCode and 0x00FFFFFF) or 0xFF000000.toInt()
            LPGuiDrawer.drawGuiTexturedRect(
                rect = body.translated(0, yOffset),
                text = buttonTextureArea,
                blend = true,
                color = if (whisky.isPageActive()) 0xFFFFFFFF.toInt() else color
            )
        }
    }

    override fun getTooltipText(): String {
        return tabPage.title
    }

    override fun drawButtonForegroundLayer(mouseX: Int, mouseY: Int) {
        if (isActive) {
            val color: Int = (MinecraftColor.values()[whisky.getColor()].colorCode and 0x00FFFFFF) or 0xFF000000.toInt()
            LPGuiDrawer.drawGuiTexturedRect(
                rect = body,
                text = buttonTextureArea,
                blend = true,
                color = -1
            )
            LPGuiDrawer.drawGuiTexturedRect(
                rect = circleArea.translated(body),
                text = circleAreaTexture,
                blend = true,
                color = color
            )
        }
        if (hovered && visible) {
            drawTooltip(
                x = body.roundedRight,
                y = body.roundedTop,
                horizontalAlign = HorizontalAlignment.RIGHT,
                verticalAlign = VerticalAlignment.BOTTOM
            )
        }
    }

    override fun setPos(newX: Int, newY: Int) {
        body.setPos(newX, newY - 24)
    }

    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean =
        bodyTrigger.translated(body).translated(0, if (whisky.isPageActive()) -3 else 0).contains(mouseX, mouseY)
}