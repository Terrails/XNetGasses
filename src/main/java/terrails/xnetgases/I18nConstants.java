package terrails.xnetgases;

import mcjty.lib.varia.ComponentFactory;

public enum I18nConstants {
    CHANNEL_CHEMICAL("xnetgases.channel.chemical"),
    CHANNEL_CHEMICAL_LOGIC("xnetgases.channel.chemical_logic"),
    REQUIRE_INSERT_RATE_LABEL("xnetgases.require_insert_rate.label"),
    CHEMICAL_RATE_TOOLTIP_FORMATTED("xnetgases.chemical.rate.tooltip.formatted"),
    CHEMICAL_MINMAX_TOOLTIP_FORMATTED("xnetgases.chemical.min_max.tooltip.formatted");

    private final String langKey;

    I18nConstants(String langKey) {
        this.langKey = langKey;
    }

    public String i18n(Object... formatArgs) {
        if (formatArgs == null) {
            return ComponentFactory.translatable(this.langKey).getString();
        }
        return ComponentFactory.translatable(this.langKey, formatArgs).getString();
    }
}
