package computer.gingershaped.casks

import net.neoforged.fml.common.Mod
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

@Mod(Casks.ID)
object Casks {
    const val ID = "casks"

    init {
        CasksRegistries.register(MOD_BUS)
    }
}
