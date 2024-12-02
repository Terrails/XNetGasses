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

public enum ChannelMode implements ITranslatableEnum<ChannelMode>, StringRepresentable {
    PRIORITY("xnetgases.enum.channel_mode.priority"),
    DISTRIBUTE("xnetgases.enum.channel_mode.round_robin");

    public static final Codec<ChannelMode> CODEC = StringRepresentable.fromEnum(ChannelMode::values);
    public static final StreamCodec<FriendlyByteBuf, ChannelMode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(ChannelMode.class);

    private static final Map<String, ChannelMode> NAME_MAP = Arrays.stream(ChannelMode.values()).collect(Collectors.toMap(Enum::name, Function.identity()));

    private final String i18n;

    ChannelMode(String i18n) {
        this.i18n = i18n;
    }

    public static ChannelMode byName(String name) {
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

    @Nonnull
    @Override
    public String getSerializedName() {
        return name();
    }
}
