package computer.gingershaped.casks.content.cask

import com.mojang.serialization.MapCodec
import computer.gingershaped.casks.CasksRegistries
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.ScheduledTickAccess
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SimpleWaterloggedBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.entity.CampfireBlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext

class CaskBlock(properties: Properties) : BaseEntityBlock(properties), SimpleWaterloggedBlock {
    companion object {
        val CODEC = simpleCodec(::CaskBlock)
        val SHAPE = column(8.0, 0.0, 8.0)
        val CONTENTS: ResourceLocation = ResourceLocation.withDefaultNamespace("contents")

        val OPEN = BlockStateProperties.OPEN
        val WATERLOGGED = BlockStateProperties.WATERLOGGED
        val ABOVE_CAMPFIRE = BooleanProperty.create("above_campfire")
    }

    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(OPEN, false)
                .setValue(WATERLOGGED, false)
                .setValue(ABOVE_CAMPFIRE, false)
        )
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block?, BlockState?>) {
        builder
            .add(OPEN)
            .add(WATERLOGGED)
            .add(ABOVE_CAMPFIRE)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        return super.getStateForPlacement(context)
            ?.setValue(WATERLOGGED, context.level.getFluidState(context.clickedPos).`is`(Fluids.WATER))
            ?.setValue(ABOVE_CAMPFIRE, campfireBelowPos(context.level, context.clickedPos) != null)
    }

    override fun updateShape(
        state: BlockState,
        level: LevelReader,
        scheduledTickAccess: ScheduledTickAccess,
        pos: BlockPos,
        direction: Direction,
        neighborPos: BlockPos,
        neighborState: BlockState,
        random: RandomSource
    ): BlockState {
        if (state.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        if (direction == Direction.DOWN) {
            return state.setValue(ABOVE_CAMPFIRE, campfireBelowPos(level, pos) != null)
        }

        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random)
    }

    override fun getFluidState(state: BlockState) =
        if (state.getValue(WATERLOGGED)) {
            Fluids.WATER.getSource(false)
        } else {
            super.getFluidState(state)
        }

    fun campfireBelowPos(level: LevelReader, pos: BlockPos): CampfireBlockEntity? {
        val blockEntity = level.getBlockEntity(pos.below())

        if (blockEntity is CampfireBlockEntity) {
            return blockEntity
        }

        return null
    }

    override fun newBlockEntity(
        pos: BlockPos, state: BlockState
    ): BlockEntity = CaskBlockEntity(pos, state)

    override fun codec(): MapCodec<out BaseEntityBlock?> = CODEC

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext) = SHAPE

    override fun getMenuProvider(state: BlockState, level: Level, pos: BlockPos) =
        level.getBlockEntity(pos, CasksRegistries.BlockEntityTypes.CASK).get()

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult {
        if (!level.isClientSide && player is ServerPlayer) {
            player.openMenu(state.getMenuProvider(level, pos)) { data ->
                data.writeResourceKey(BuiltInRegistries.BLOCK.getResourceKey(this).get())
            }
        }

        return InteractionResult.SUCCESS
    }

    override fun playerWillDestroy(level: Level, pos: BlockPos, state: BlockState, player: Player): BlockState {
        level.getBlockEntity(pos, CasksRegistries.BlockEntityTypes.CASK).ifPresent { blockEntity ->
            if (!level.isClientSide && player.preventsBlockDrops() && !blockEntity.isEmpty()) {
                val itemStack = ItemStack(state.block.asItem())
                itemStack.applyComponents(blockEntity.collectComponents())
                val itemEntity =
                    ItemEntity(level, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, itemStack)
                itemEntity.setDefaultPickUpDelay()
                level.addFreshEntity(itemEntity)
            }
        }

        return super.playerWillDestroy(level, pos, state, player)
    }

    override fun getDrops(state: BlockState, params: LootParams.Builder): List<ItemStack?> {
        var updatedParams = params
        val blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY)
        
        if (blockEntity is CaskBlockEntity) {
            updatedParams = params.withDynamicDrop(CONTENTS) { drops ->
                for (stack in blockEntity.inventory.copyToList()) {
                    drops.accept(stack)
                }
            }
        }

        return super.getDrops(state, updatedParams)
    }

    override fun hasAnalogOutputSignal(state: BlockState) = true

    override fun getAnalogOutputSignal(state: BlockState, level: Level, pos: BlockPos, direction: Direction): Int {
        val blockEntity = level.getBlockEntity(pos, CasksRegistries.BlockEntityTypes.CASK).get()

        var signal = 0F
        with(blockEntity.inventory) {
            for (index in 0..<size()) {
                val contents = getResource(index)
                val amount = getAmountAsLong(index)
                val capacity = getCapacityAsLong(index, contents)
                println("$amount $capacity")

                signal += amount / capacity
            }

            signal /= size()
        }
        return Mth.lerpDiscrete(signal, 0, 15)
    }

    override fun isPathfindable(state: BlockState, pathComputationType: PathComputationType) = false

    override fun <T : BlockEntity?> getTicker(
        level: Level,
        state: BlockState,
        blockEntityType: BlockEntityType<T?>
    ): BlockEntityTicker<T?>? {
        return if (state.getValue(ABOVE_CAMPFIRE)) {
            createTickerHelper(
                blockEntityType,
                CasksRegistries.BlockEntityTypes.CASK,
                CaskBlockEntity::campfireTransferTick
            )
        } else {
            null
        }
    }
}

