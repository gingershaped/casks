package computer.gingershaped.casks

import net.neoforged.fml.common.Mod
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

/**
 * Main mod class. Should be an `object` declaration annotated with `@Mod`.
 * The modid should be declared in this object and should match the modId entry
 * in neoforge.mods.toml.
 *
 * An example for blocks is in the `blocks` package of this mod.
 */
@Mod(Casks.ID)
object Casks {
    const val ID = "casks"

    init {
        CasksRegistries.register(MOD_BUS)
    }
}