package computer.gingershaped.casks

import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.ItemModelGenerators
import net.minecraft.client.data.models.ModelProvider
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.client.data.models.blockstates.PropertyDispatch
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.client.data.models.model.TexturedModel
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents
import net.minecraft.data.PackOutput
import net.minecraft.data.loot.BlockLootSubProvider
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import java.util.concurrent.CompletableFuture

class CasksModelProvider(output: PackOutput) : ModelProvider(output, Casks.ID) {
    val closedCaskProvider = caskProvider(ResourceLocation.fromNamespaceAndPath(Casks.ID, "block/cask_closed"))
    val openCaskProvider = caskProvider(ResourceLocation.fromNamespaceAndPath(Casks.ID, "block/cask_open"))

    private fun caskProvider(parent: ResourceLocation) = TexturedModel.createDefault(
        TextureMapping::cube, ModelTemplates.CUBE_ALL.extend().parent(parent).build()
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
}

class CasksLootTableProvider(output: PackOutput, lookupProvider: CompletableFuture<HolderLookup.Provider>) :
    LootTableProvider(
        output, emptySet(), listOf(
            SubProviderEntry(::BlockLoot, LootContextParamSets.BLOCK)
        ), lookupProvider
    ) {

    class BlockLoot(lookupProvider: HolderLookup.Provider) :
        BlockLootSubProvider(emptySet(), FeatureFlags.DEFAULT_FLAGS, lookupProvider) {
        override fun getKnownBlocks() = CasksRegistries.CaskTypes.entries.map { it.block }

        override fun generate() {
            for (cask in knownBlocks) {
                add(
                    cask, LootTable.lootTable().withPool(
                        this.applyExplosionCondition(
                            cask, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add(
                                LootItem.lootTableItem(cask).apply(
                                    CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)
                                        .include(DataComponents.CUSTOM_NAME)
                                        .include(DataComponents.CONTAINER)
                                )
                            )
                        )
                    )
                )
            }
        }

    }
}