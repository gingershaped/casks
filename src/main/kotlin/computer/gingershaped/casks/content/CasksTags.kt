package computer.gingershaped.casks.content

import computer.gingershaped.casks.Casks
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey

object CasksTags {
    val CASK_BLOCKS = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(Casks.ID, "casks")
    )

    val CASK_ITEMS = TagKey.create(
        Registries.ITEM,
        ResourceLocation.fromNamespaceAndPath(Casks.ID, "casks")
    )
}