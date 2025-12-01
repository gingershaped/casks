package computer.gingershaped.casks.content

import com.mojang.serialization.MapCodec
import computer.gingershaped.casks.CasksRegistries
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext

class CaskBlock(properties: Properties) : BaseEntityBlock(properties) {
    companion object {
        val OPEN = BlockStateProperties.OPEN
        val CODEC = simpleCodec(::CaskBlock)
        val SHAPE = column(8.0, 0.0, 8.0)
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(OPEN, false))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block?, BlockState?>) {
        builder.add(OPEN)
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

}

