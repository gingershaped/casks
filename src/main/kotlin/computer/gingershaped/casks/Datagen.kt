package computer.gingershaped.casks

import computer.gingershaped.casks.content.CasksTags
import computer.gingershaped.casks.content.cask.CaskBlock
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.ItemModelGenerators
import net.minecraft.client.data.models.ModelProvider
import net.minecraft.client.data.models.blockstates.MultiPartGenerator
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.client.data.models.model.TexturedModel
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents
import net.minecraft.data.PackOutput
import net.minecraft.data.loot.BlockLootSubProvider
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import net.neoforged.neoforge.common.data.BlockTagCopyingItemTagProvider
import net.neoforged.neoforge.common.data.BlockTagsProvider
import java.util.concurrent.CompletableFuture

class CaskModelProvider(output: PackOutput) : ModelProvider(output, Casks.ID) {
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
                val frame = ResourceLocation.fromNamespaceAndPath(Casks.ID, "block/cask_frame")

                blockStateOutput.accept(
                    MultiPartGenerator.multiPart(cask.block)
                        .with(
                            BlockModelGenerators.condition().term(CaskBlock.OPEN, true),
                            BlockModelGenerators.plainVariant(open)
                        )
                        .with(
                            BlockModelGenerators.condition().term(CaskBlock.OPEN, false),
                            BlockModelGenerators.plainVariant(closed)
                        )
                        .with(
                            BlockModelGenerators.condition().term(CaskBlock.ABOVE_CAMPFIRE, true),
                            BlockModelGenerators.plainVariant(frame)
                        )
                )
            }
        }
    }
}

class CaskLootTableProvider(output: PackOutput, lookupProvider: CompletableFuture<HolderLookup.Provider>) :
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

class CaskBlockTagsProvider(output: PackOutput, lookupProvider: CompletableFuture<HolderLookup.Provider>)
    : BlockTagsProvider(output, lookupProvider, Casks.ID) {

    override fun addTags(lookupProvider: HolderLookup.Provider) {
        for (cask in CasksRegistries.CaskTypes.entries) {
            tag(BlockTags.MINEABLE_WITH_AXE).add(cask.block)
            tag(CasksTags.CASK_BLOCKS).add(cask.block)
        }
    }
}

class CaskItemTagsProvider(
    output: PackOutput,
    lookupProvider: CompletableFuture<HolderLookup.Provider>,
    blockTags: CompletableFuture<TagLookup<Block>>
) : BlockTagCopyingItemTagProvider(output, lookupProvider, blockTags, Casks.ID) {

    override fun addTags(provider: HolderLookup.Provider) {
        copy(CasksTags.CASK_BLOCKS, CasksTags.CASK_ITEMS)
    }
}

class CaskRecipeProvider(lookupProvider: HolderLookup.Provider, output: RecipeOutput)
    : RecipeProvider(lookupProvider, output) {

    override fun buildRecipes() {
        for (cask in CasksRegistries.CaskTypes.entries) {
            shaped(RecipeCategory.DECORATIONS, cask.block)
                .define('S', Items.STRING)
                .define('F', Items.FLOWER_POT)
                .define('L', cask.slab)
                .pattern("S S")
                .pattern("FFF")
                .pattern("LLL")
                .unlockedBy("has_planks", has(ItemTags.PLANKS))
                .unlockedBy("has_wood_slab", has(ItemTags.WOODEN_SLABS))
                .group("casks")
                .save(output)
        }
    }

    class Runner(output: PackOutput, lookupProvider: CompletableFuture<HolderLookup.Provider>)
        : RecipeProvider.Runner(output, lookupProvider) {

        override fun createRecipeProvider(lookupProvider: HolderLookup.Provider, output: RecipeOutput) =
            CaskRecipeProvider(lookupProvider, output)

        override fun getName() = "Casks recipes"
    }
}
