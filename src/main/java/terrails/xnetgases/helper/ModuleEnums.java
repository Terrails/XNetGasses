package terrails.xnetgases.helper;

import mcjty.lib.gui.ITranslatableEnum;
import mcjty.lib.varia.ComponentFactory;
import mcjty.xnet.utils.I18nUtils;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ModuleEnums {

    public enum ChannelMode implements ITranslatableEnum<ChannelMode> {
        PRIORITY("xnet.enum.channelmode.priority"),
        DISTRIBUTE("xnet.enum.channelmode.roundrobin");

        private final String i18n;

        ChannelMode(String i18n) {
            this.i18n = i18n;
        }

        private static final Map<String, ChannelMode> NAME_MAP = Arrays.stream(ChannelMode.values()).collect(Collectors.toMap(Enum::name, Function.identity()));

        public static ChannelMode byName(String name) {
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
    }

}
