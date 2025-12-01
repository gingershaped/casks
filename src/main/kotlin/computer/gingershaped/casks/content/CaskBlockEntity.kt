package computer.gingershaped.casks.content

import computer.gingershaped.casks.CasksRegistries
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler

class CaskBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(CasksRegistries.BlockEntityTypes.CASK, pos, state),
    MenuProvider {
    val inventory = object : ItemStacksResourceHandler(3) {
        override fun onContentsChanged(index: Int, previousContents: ItemStack) {
            setChanged()
        }
    }

    override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)
        inventory.serialize(output)
    }

    override fun loadAdditional(input: ValueInput) {
        super.loadAdditional(input)
        inventory.deserialize(input)
    }

    override fun getDisplayName(): Component {
        return Component.literal("placeholder")
    }

    override fun createMenu(
        containerId: Int,
        playerInventory: Inventory,
        player: Player
    ): AbstractContainerMenu =
        CaskMenu(
            containerId,
            playerInventory,
            this.inventory,
            this.blockState.block,
            ContainerLevelAccess.create(this.level!!, this.blockPos)
        )
}