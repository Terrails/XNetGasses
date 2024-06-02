package terrails.xnetgases;

import mcjty.lib.varia.ComponentFactory;

public enum I18nConstants {

    CHANNEL_GAS("xnet-gases.channel.gas"),
    CHANNEL_LOGIC("xnet-gases.channel.logic"),
    CONNECTOR_TYPE_LABEL("xnet-gases.connectortype.label"),
    REQUIRE_INSERT_RATE_LABEL("xnet-gases.requireinsertrate.label"),
    GAS_RATE_TOOLTIP_FORMATTED("xnet-gases.gas.rate.tooltip.formatted"),
    GAS_MINMAX_TOOLTIP_FORMATTED("xnet-gases.gas.minmax.tooltip.formatted"),
    ;

    private final String langKey;

    I18nConstants(String langKey) {this.langKey = langKey;}

    public String i18n(Object... formatArgs) {
        if (formatArgs == null) {
            return ComponentFactory.translatable(this.langKey).getString();
        }
        return ComponentFactory.translatable(this.langKey, formatArgs).getString();
    }
}
