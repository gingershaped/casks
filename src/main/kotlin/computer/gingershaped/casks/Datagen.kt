package computer.gingershaped.casks

import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.ItemModelGenerators
import net.minecraft.client.data.models.ModelProvider
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.client.data.models.blockstates.PropertyDispatch
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.client.data.models.model.TexturedModel
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.data.event.GatherDataEvent

class CaskModels(output: PackOutput) : ModelProvider(output, Casks.ID) {
    val closedCaskProvider = caskProvider(ResourceLocation.fromNamespaceAndPath(Casks.ID, "block/cask_closed"))
    val openCaskProvider = caskProvider(ResourceLocation.fromNamespaceAndPath(Casks.ID, "block/cask_open"))

    private fun caskProvider(parent: ResourceLocation) = TexturedModel.createDefault(
        TextureMapping::cube,
        ModelTemplates.CUBE_ALL
            .extend()
            .parent(parent)
            .build()
    )

    override fun registerModels(blockModels: BlockModelGenerators, itemModels: ItemModelGenerators) {
        for (cask in CasksRegistries.CaskTypes.entries) {
            with(blockModels) {
                val closed = closedCaskProvider.create(
                    cask.block, modelOutput
                )
                val open = openCaskProvider.createWithSuffix(
                    cask.block, "_open", modelOutput
                )

                blockStateOutput.accept(
                    MultiVariantGenerator.dispatch(cask.block).with(
                        PropertyDispatch.initial(BlockStateProperties.OPEN)
                            .select(false, BlockModelGenerators.plainVariant(closed))
                            .select(true, BlockModelGenerators.plainVariant(open))
                    )
                )
            }
        }
    }

    @EventBusSubscriber
    companion object {
        @SubscribeEvent
        fun gatherData(event: GatherDataEvent.Client) {
            event.createProvider(::CaskModels)
        }
    }
}