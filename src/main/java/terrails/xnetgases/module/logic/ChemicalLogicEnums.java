package terrails.xnetgases.module.logic;

import mcjty.lib.gui.ITranslatableEnum;
import mcjty.lib.varia.ComponentFactory;
import mcjty.xnet.utils.I18nUtils;
import terrails.xnetgases.module.chemical.ChemicalEnums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChemicalLogicEnums {

    public enum SensorMode implements ITranslatableEnum<SensorMode> {
        OFF("xnet-gases.enum.logic.sensormode.off"),
        GAS("xnet-gases.enum.logic.sensormode.gas"),
        SLURRY("xnet-gases.enum.logic.sensormode.slurry"),
        INFUSE("xnet-gases.enum.logic.sensormode.infuse"),
        PIGMENT("xnet-gases.enum.logic.sensormode.pigment");

        private final String i18n;

        SensorMode(String i18n) {
            this.i18n = i18n;
        }

        private static final Map<String, SensorMode> NAME_MAP = Arrays.stream(SensorMode.values()).collect(Collectors.toMap(Enum::name, Function.identity()));

        public static SensorMode byName(String name) {
            return NAME_MAP.get(name);
        }

        public ChemicalEnums.Type toType() {
            if (this == OFF) return null;
            return ChemicalEnums.Type.byName(this.name());
        }

        @Override
        public String getI18n() {
            return ComponentFactory.translatable(i18n).getString();
        }

        @Override
        public String[] getI18nSplitedTooltip() {
            return I18nUtils.getSplitedEnumTooltip(i18n);
        }
    }

    // Custom Operator because the original uses integer instead of long.
    // Creative tanks use Long#MAX_VALUE which results in an overflow.
    public enum SensorOperator {
        EQUAL("=", Long::equals),
        NOTEQUAL("!=", (i1, i2) -> !i1.equals(i2)),
        LESS("<", (i1, i2) -> i1 < i2),
        GREATER(">", (i1, i2) -> i1 > i2),
        LESSOREQUAL("<=", (i1, i2) -> i1 <= i2),
        GREATEROREQUAL(">=", (i1, i2) -> i1 >= i2);

        private final String code;
        private final BiPredicate<Long, Long> matcher;

        private static final Map<String, SensorOperator> OPERATOR_MAP = Arrays.stream(SensorOperator.values()).collect(Collectors.toMap(op -> op.code, Function.identity()));
        private static final Map<String, SensorOperator> NAME_MAP = Arrays.stream(SensorOperator.values()).collect(Collectors.toMap(Enum::name, Function.identity()));

        SensorOperator(String code, BiPredicate<Long, Long> matcher) {
            this.code = code;
            this.matcher = matcher;
        }

        public static SensorOperator byCode(String code) {
            return OPERATOR_MAP.get(code);
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
    }

}
