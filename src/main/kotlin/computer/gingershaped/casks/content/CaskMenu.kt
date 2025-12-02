package computer.gingershaped.casks.content

import computer.gingershaped.casks.CasksRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.ContainerOpenersCounter
import net.neoforged.neoforge.transfer.StacksResourceHandler
import net.neoforged.neoforge.transfer.item.ItemResource
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot

class CaskMenu private constructor(
    containerId: Int,
    val playerInventory: Inventory,
    val caskInventory: StacksResourceHandler<ItemStack, ItemResource>,
    val attachedBlock: Block,
    val blockEntity: CaskBlockEntity?,
) : AbstractContainerMenu(CasksRegistries.MenuTypes.CASK, containerId) {
    companion object {
        const val SLOTS = 3

        fun client(containerId: Int, playerInventory: Inventory, data: RegistryFriendlyByteBuf) = CaskMenu(
            containerId,
            playerInventory,
            CaskBlockEntity.ResourceHandler(null),
            data.registryAccess().let { access ->
                val key = data.readResourceKey(Registries.BLOCK)

                access[key].get().value()
            },
            null
        )

        fun server(containerId: Int, playerInventory: Inventory, blockEntity: CaskBlockEntity) = with(blockEntity) {
            CaskMenu(
                containerId,
                playerInventory,
                inventory,
                blockState.block,
                this
            )
        }
    }

    init {
        check(this.caskInventory.size() == SLOTS)

        blockEntity?.startOpen(playerInventory.player)

        for (col in 0..<SLOTS) {
            addSlot(ResourceHandlerSlot(
                caskInventory,
                caskInventory::set,
                col,
                62 + col * 18,
                20
            ))
        }

        addStandardInventorySlots(playerInventory, 8, 51)
    }

    override fun quickMoveStack(
        player: Player,
        index: Int
    ): ItemStack {
        val slot = this.slots[index]

        return if (slot.hasItem()) {
            val slotContents = slot.item

            if (index < caskInventory.size()) {
                if (!this.moveItemStackTo(slotContents, caskInventory.size(), this.slots.size, true)) {
                    return ItemStack.EMPTY
                }
            } else if (!this.moveItemStackTo(slotContents, 0, caskInventory.size(), false)) {
                return ItemStack.EMPTY
            }

            if (slotContents.isEmpty) {
                slot.setByPlayer(ItemStack.EMPTY)
            } else {
                slot.setChanged()
            }

            slotContents.copy()
        } else {
            ItemStack.EMPTY
        }
    }

    val access = blockEntity?.run { ContainerLevelAccess.create(level!!, blockPos) } ?: ContainerLevelAccess.NULL

    override fun stillValid(player: Player) =
        stillValid(access, player, attachedBlock)

    override fun removed(player: Player) {
        super.removed(player)

        blockEntity?.stopOpen(player)
    }
}