package terrails.xnetgases;

import mcjty.xnet.XNet;

import net.minecraft.resources.ResourceLocation;

import java.util.Set;

import static mcjty.rftoolsbase.api.xnet.helper.AbstractConnectorSettings.TAG_COLOR;
import static mcjty.rftoolsbase.api.xnet.helper.AbstractConnectorSettings.TAG_RS;
import static mcjty.xnet.apiimpl.Constants.*;

public class Constants {

    public static final ResourceLocation XNET_GUI_ELEMENTS = ResourceLocation.fromNamespaceAndPath(XNet.MODID, "textures/gui/guielements.png");

    public static final String TAG_CHEMICAL_MODE = "chemicalMode";
    public static final String TAG_REQUIRE_RATE = "rate_required";
    public static final String TAG_DISTRIBUTE_OFFSET = "distribute_offset";

    public static final Set<String> CHEMICAL_INSERT_FEATURES = Set.of(TAG_CHEMICAL_MODE, TAG_RS, TAG_RATE, TAG_REQUIRE_RATE, TAG_MINMAX, TAG_PRIORITY, TAG_FILTER, TAG_COLOR + "0", TAG_COLOR + "1", TAG_COLOR + "2", TAG_COLOR + "3");
    public static final Set<String> CHEMICAL_EXTRACT_FEATURES = Set.of(TAG_CHEMICAL_MODE, TAG_RS, TAG_COLOR + "0", TAG_COLOR + "1", TAG_COLOR + "2", TAG_COLOR + "3", TAG_RATE, TAG_MINMAX, TAG_FILTER, TAG_SPEED);
    public static final Set<String> CHEMICAL_LOGIC_FEATURES = Set.of(TAG_LOGIC_MODE, TAG_REDSTONE_OUT, TAG_RS, TAG_COLOR + "0", TAG_COLOR + "1", TAG_COLOR + "2", TAG_COLOR + "3");
}
