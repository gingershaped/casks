package computer.gingershaped.casks

import computer.gingershaped.casks.content.CaskBlock
import computer.gingershaped.casks.content.CaskMenu
import computer.gingershaped.casks.content.CaskBlockEntity
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.Item
import net.minecraft.world.item.component.ItemContainerContents
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks as VanillaBlocks
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.level.material.MapColor
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension
import net.minecraft.core.registries.Registries as RegistryTypes
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue

object CasksRegistries {
    object Blocks {
        val REGISTRY = DeferredRegister.createBlocks(Casks.ID)
    }

    object Items {
        val REGISTRY = DeferredRegister.createItems(Casks.ID)
    }

    enum class CaskTypes(val mapColor: MapColor, val slab: Block, val soundType: SoundType = SoundType.WOOD) {
        ACACIA(MapColor.COLOR_ORANGE, VanillaBlocks.ACACIA_SLAB),
        BAMBOO(MapColor.COLOR_YELLOW, VanillaBlocks.BAMBOO_SLAB, SoundType.BAMBOO_WOOD),
        BIRCH(MapColor.SAND, VanillaBlocks.BIRCH_SLAB),
        CHERRY(MapColor.TERRACOTTA_WHITE, VanillaBlocks.CHERRY_SLAB, SoundType.CHERRY_WOOD),
        CRIMSON(MapColor.CRIMSON_STEM, VanillaBlocks.CRIMSON_SLAB, SoundType.NETHER_WOOD),
        DARK_OAK(MapColor.COLOR_BROWN, VanillaBlocks.DARK_OAK_SLAB),
        JUNGLE(MapColor.DIRT, VanillaBlocks.JUNGLE_SLAB),
        MANGROVE(MapColor.COLOR_RED, VanillaBlocks.MANGROVE_SLAB),
        OAK(MapColor.WOOD, VanillaBlocks.OAK_SLAB),
        PALE_OAK(MapColor.QUARTZ, VanillaBlocks.PALE_OAK_SLAB),
        SPRUCE(MapColor.PODZOL, VanillaBlocks.SPRUCE_SLAB),
        WARPED(MapColor.WARPED_STEM, VanillaBlocks.WARPED_SLAB, SoundType.NETHER_WOOD)
        ;

        val id = this.name.lowercase() + "_cask"

        val block by Blocks.REGISTRY.registerBlock(id, ::CaskBlock) { ->
            BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .instrument(NoteBlockInstrument.BASS)
                .strength(2.5F)
                .sound(soundType)
                .ignitedByLava()
        }

        val item by Items.REGISTRY.registerSimpleBlockItem(id, { block }) { properties ->
            properties
                .stacksTo(1)
                .component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
        }
    }

    object BlockEntityTypes {
        val REGISTRY = DeferredRegister.create(RegistryTypes.BLOCK_ENTITY_TYPE, Casks.ID)

        val CASK by REGISTRY.register("cask") { ->
            BlockEntityType(
                ::CaskBlockEntity,
                CaskTypes.entries.map { it.block }.toSet()
            )
        }
    }


    object MenuTypes {
        val REGISTRY = DeferredRegister.create(RegistryTypes.MENU, Casks.ID)

        val CASK by REGISTRY.register("cask") { -> IMenuTypeExtension.create(CaskMenu::client) }
    }

    fun register(bus: IEventBus) {
        // make sure the entry classes get loaded
        CaskTypes.entries

        Blocks.REGISTRY.register(bus)
        Items.REGISTRY.register(bus)
        BlockEntityTypes.REGISTRY.register(bus)
        MenuTypes.REGISTRY.register(bus)
    }
}