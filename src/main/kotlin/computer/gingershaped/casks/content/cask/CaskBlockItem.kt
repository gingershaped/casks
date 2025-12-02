package computer.gingershaped.casks.content.cask

import net.minecraft.world.item.BlockItem

class CaskBlockItem(block: CaskBlock, properties: Properties) : BlockItem(block, properties) {
    override fun canFitInsideContainerItems() = false
}