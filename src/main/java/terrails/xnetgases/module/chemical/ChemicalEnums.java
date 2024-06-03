package terrails.xnetgases.module.chemical;

import mcjty.lib.gui.ITranslatableEnum;
import mcjty.lib.varia.ComponentFactory;
import mcjty.xnet.utils.I18nUtils;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChemicalEnums {

    public enum Type implements ITranslatableEnum<Type> {
        GAS("xnet-gases.gas"),
        INFUSE("xnet-gases.infuse"),
        PIGMENT("xnet-gases.pigment"),
        SLURRY("xnet-gases.slurry");

        private final String i18n;

        Type(String i18n) {
            this.i18n = i18n;
        }

        public static final Map<String, Type> NAME_MAP = Arrays.stream(Type.values()).collect(Collectors.toMap(Enum::name, Function.identity()));

        public static Type byName(String name) {
            return NAME_MAP.get(name.toUpperCase(Locale.ROOT));
        }

        @Override
        public String getI18n() {
            return ComponentFactory.translatable(i18n).getString();
        }

        @Override
        public String[] getI18nSplitedTooltip() {
            return I18nUtils.getSplitedEnumTooltip(i18n);
        }

        public static Type safeChemicalType(Object o) {
            if (o != null) {
                return Type.values()[(int) o];
            } else {
                return Type.GAS;
            }
        }
    }

}
