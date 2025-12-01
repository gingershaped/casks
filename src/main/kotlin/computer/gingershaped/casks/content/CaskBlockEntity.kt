package computer.gingershaped.casks.content

import computer.gingershaped.casks.CasksRegistries
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BarrelBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.ContainerOpenersCounter
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

    val openersCounter = object : ContainerOpenersCounter() {
        override fun onOpen(
            level: Level,
            pos: BlockPos,
            state: BlockState
        ) {
            playSound(level, pos, SoundEvents.BARREL_OPEN)
            setOpen(level, pos, state, true)
        }

        override fun onClose(
            level: Level,
            pos: BlockPos,
            state: BlockState
        ) {
            playSound(level, pos, SoundEvents.BARREL_CLOSE)
            setOpen(level, pos, state, false)
        }

        override fun openerCountChanged(
            level: Level,
            pos: BlockPos,
            state: BlockState,
            count: Int,
            openCount: Int
        ) {}

        override fun isOwnContainer(player: Player): Boolean {
            val menu = player.containerMenu

            return if (menu is CaskMenu) {
                menu.caskInventory == this@CaskBlockEntity.inventory
            } else {
                false
            }
        }

        fun playSound(level: Level, pos: BlockPos, sound: SoundEvent) {
            level.playSound(
                null,
                pos,
                sound,
                SoundSource.BLOCKS,
                0.5f,
                level.random.nextFloat() * 0.1f + 0.9f + 0.5f
            )
        }

        fun setOpen(level: Level, pos: BlockPos, currentState: BlockState, open: Boolean) {
            level.setBlockAndUpdate(pos, currentState.setValue(CaskBlock.OPEN, open))
        }
    }

    fun startOpen(player: Player) {
        if (!remove && !player.isSpectator) {
            openersCounter.incrementOpeners(player, level!!, blockPos, blockState, player.containerInteractionRange)
        }
    }

    fun stopOpen(player: Player) {
        if (!remove && !player.isSpectator) {
            openersCounter.decrementOpeners(player, level!!, blockPos, blockState)
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
    ): AbstractContainerMenu = CaskMenu.server(containerId, playerInventory, this)
}