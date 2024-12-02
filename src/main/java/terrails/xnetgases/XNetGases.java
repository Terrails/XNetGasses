package terrails.xnetgases;

import mcjty.rftoolsbase.api.xnet.IXNet;
import mcjty.rftoolsbase.api.xnet.channels.IConnectable;
import mcjty.xnet.XNet;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import terrails.xnetgases.module.ChemicalHelper;
import terrails.xnetgases.module.chemical.ChemicalChannelType;
import terrails.xnetgases.module.logic.ChemicalLogicChannelType;

import java.util.function.Function;

@Mod(XNetGases.MOD_ID)
public class XNetGases {

    public static final String MOD_ID = "xnetgases";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final ModConfigSpec.IntValue maxRateNormal;
    public static final ModConfigSpec.IntValue maxRateAdvanced;

    private static final ModConfigSpec CONFIG_SPEC;

    static {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("General settings").push("general");
        maxRateNormal = builder.comment("Maximum chemical per operation that a normal connector can input or output").defineInRange("maxChemicalRateNormal", 1000, 1, 1000000000);
        maxRateAdvanced = builder.comment("Maximum chemical per operation that an advanced connector can input or output").defineInRange("maxChemicalRateAdvanced", 5000, 1, 1000000000);
        CONFIG_SPEC = builder.pop().build();
    }

    public XNetGases(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::setup);
        modContainer.registerConfig(ModConfig.Type.SERVER, CONFIG_SPEC, XNetGases.MOD_ID + ".toml");
    }

    private void setup(final FMLCommonSetupEvent event) {
        InterModComms.sendTo(XNet.MODID, "getXNet", () -> (Function<IXNet, Void>) api -> {
            api.registerChannelType(ChemicalChannelType.TYPE);
            api.registerChannelType(ChemicalLogicChannelType.TYPE);

            api.registerConnectable(((blockGetter, connectorPos, blockPos, blockEntity, direction) -> {
                if (ChemicalHelper.blockSupportsChemicals(blockEntity, direction)) {
                    return IConnectable.ConnectResult.YES;
                } else return IConnectable.ConnectResult.DEFAULT;
            }));
            return null;
        });
    }
}
