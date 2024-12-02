package terrails.xnetgases.module.chemical.enums;

import com.mojang.serialization.Codec;
import mcjty.lib.gui.ITranslatableEnum;
import mcjty.lib.varia.ComponentFactory;
import mcjty.xnet.utils.I18nUtils;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ConnectorMode implements ITranslatableEnum<ConnectorMode>, StringRepresentable {
    INS("xnetgases.enum.connector_mode.ins"),
    EXT("xnetgases.enum.connector_mode.ext");

    public static final Codec<ConnectorMode> CODEC = StringRepresentable.fromEnum(ConnectorMode::values);
    public static final StreamCodec<FriendlyByteBuf, ConnectorMode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(ConnectorMode.class);

    private static final Map<String, ConnectorMode> NAME_MAP = Arrays.stream(ConnectorMode.values()).collect(Collectors.toMap(Enum::name, Function.identity()));

    private final String i18n;

    ConnectorMode(String i18n) {
        this.i18n = i18n;
    }

    public static ConnectorMode byName(String name) {
        return NAME_MAP.get(name);
    }

    @Override
    public String getI18n() {
        return ComponentFactory.translatable(this.i18n).getString();
    }

    @Override
    public String[] getI18nSplitedTooltip() {
        return I18nUtils.getSplitedEnumTooltip(this.i18n);
    }

    @Nonnull
    @Override
    public String getSerializedName() {
        return this.name();
    }
}
