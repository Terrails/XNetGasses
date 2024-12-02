package terrails.xnetgases.module.logic.enums;

import com.mojang.serialization.Codec;
import mcjty.lib.gui.ITranslatableEnum;
import mcjty.lib.varia.ComponentFactory;
import mcjty.xnet.utils.I18nUtils;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum SensorMode implements ITranslatableEnum<SensorMode>, StringRepresentable {
    OFF("xnetgases.enum.sensor_mode.off"),
    ON("xnetgases.enum.sensor_mode.on");

    public static final Codec<SensorMode> CODEC = StringRepresentable.fromEnum(SensorMode::values);
    public static final StreamCodec<FriendlyByteBuf, SensorMode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(SensorMode.class);

    private static final Map<String, SensorMode> NAME_MAP = Arrays.stream(SensorMode.values()).collect(Collectors.toMap(Enum::name, Function.identity()));

    private final String i18n;

    SensorMode(String i18n) {
        this.i18n = i18n;
    }

    public static SensorMode byName(String name) {
        return NAME_MAP.get(name);
    }

    @Override
    public String getI18n() {
        return ComponentFactory.translatable(i18n).getString();
    }

    @Override
    public String[] getI18nSplitedTooltip() {
        return I18nUtils.getSplitedEnumTooltip(i18n);
    }


    @Override
    public String getSerializedName() {
        return name();
    }
}
