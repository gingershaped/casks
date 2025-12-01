package computer.gingershaped.casks.client

import computer.gingershaped.casks.CasksRegistries
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent

@EventBusSubscriber(Dist.CLIENT)
object EventHandlers {
    @SubscribeEvent
    private fun registerScreens(event: RegisterMenuScreensEvent) {
        event.register(CasksRegistries.MenuTypes.CASK, ::CaskScreen)
    }
}