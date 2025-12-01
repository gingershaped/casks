package computer.gingershaped.casks.client

import computer.gingershaped.casks.Casks
import computer.gingershaped.casks.content.CaskMenu
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory

class CaskScreen(menu: CaskMenu, playerInventory: Inventory, title: Component) :
    AbstractContainerScreen<CaskMenu>(menu, playerInventory, title) {

    companion object {
        val TEXTURE = ResourceLocation.fromNamespaceAndPath(Casks.ID, "textures/gui/cask.png")
    }

    override fun init() {
        super.init()

        this.inventoryLabelY = imageHeight - 127
    }

    override fun renderBg(
        guiGraphics: GuiGraphics,
        partialTick: Float,
        mouseX: Int,
        mouseY: Int
    ) {
        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            TEXTURE,
            (width - imageWidth) / 2, (height - imageHeight) / 2,
            0F, 0F,
            imageWidth, imageHeight,
            256, 256
        )
    }
}