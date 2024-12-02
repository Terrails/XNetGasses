package terrails.xnetgases.module.logic.enums;

import com.mojang.serialization.Codec;
import mcjty.lib.gui.ITranslatableEnum;
import mcjty.xnet.utils.I18nUtils;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import static mcjty.xnet.utils.I18nConstants.LOGIC_SENSOR_OPERATOR_TOOLTIP;

/**
 * Custom Operator because the original uses integer instead of long.
 * Creative tanks use {@link Long#MAX_VALUE} which results in an overflow.
 */
public enum SensorOperator implements ITranslatableEnum<SensorOperator>, StringRepresentable {
    EQUAL("=", Long::equals),
    NOTEQUAL("!=", (i1, i2) -> !i1.equals(i2)),
    LESS("<", (i1, i2) -> i1 < i2),
    GREATER(">", (i1, i2) -> i1 > i2),
    LESSOREQUAL("<=", (i1, i2) -> i1 <= i2),
    GREATEROREQUAL(">=", (i1, i2) -> i1 >= i2);

    public static final Codec<SensorOperator> CODEC = StringRepresentable.fromEnum(SensorOperator::values);
    public static final StreamCodec<FriendlyByteBuf, SensorOperator> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(SensorOperator.class);

    private static final Map<String, SensorOperator> NAME_MAP = Arrays.stream(SensorOperator.values()).collect(Collectors.toMap(Enum::name, Function.identity()));

    private final String code;
    private final BiPredicate<Long, Long> matcher;

    SensorOperator(String code, BiPredicate<Long, Long> matcher) {
        this.code = code;
        this.matcher = matcher;
    }

    public static SensorOperator byName(String name) {
        return NAME_MAP.get(name);
    }

    public boolean match(long i1, long i2) {
        return matcher.test(i1, i2);
    }

    @Override
    public String toString() {
        return code;
    }

    @Override
    public String getI18n() {
        return code;
    }

    @Override
    public String[] getI18nSplitedTooltip() {
        return I18nUtils.getSplitedTooltip(LOGIC_SENSOR_OPERATOR_TOOLTIP.i18n());
    }


    @Nonnull
    @Override
    public String getSerializedName() {
        return name();
    }
}
