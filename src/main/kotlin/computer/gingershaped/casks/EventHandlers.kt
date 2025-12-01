package computer.gingershaped.casks

import net.minecraft.world.item.CreativeModeTabs
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent

@EventBusSubscriber
object EventHandlers {
    @SubscribeEvent
    private fun buildCreativeModeTabContents(event: BuildCreativeModeTabContentsEvent) {
        if (event.tabKey == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            CasksRegistries.CaskTypes.entries.forEach { event.accept(it.item) }
        }
    }

    @SubscribeEvent
    private fun registerCapabilities(event: RegisterCapabilitiesEvent) {
        event.registerBlockEntity(
            Capabilities.Item.BLOCK,
            CasksRegistries.BlockEntityTypes.CASK
        ) { be, _ -> be.inventory }
    }
}