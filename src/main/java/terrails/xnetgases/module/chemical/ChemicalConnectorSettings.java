package terrails.xnetgases.module.chemical;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.lib.varia.JSonTools;
import mcjty.rftoolsbase.api.xnet.gui.IEditorGui;
import mcjty.rftoolsbase.api.xnet.gui.IndicatorIcon;
import mcjty.xnet.apiimpl.Constants;
import mcjty.xnet.apiimpl.enums.InsExtMode;
import mcjty.xnet.utils.CastTools;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import terrails.xnetgases.helper.BaseConnectorSettings;
import terrails.xnetgases.module.chemical.utils.ChemicalHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static mcjty.xnet.apiimpl.Constants.TAG_ADVANCED_NEEDED;
import static mcjty.xnet.apiimpl.Constants.TAG_MODE;
import static mcjty.xnet.utils.I18nConstants.EXT_ENDING;
import static mcjty.xnet.utils.I18nConstants.FILTER_LABEL;
import static mcjty.xnet.utils.I18nConstants.HIGH_FORMAT;
import static mcjty.xnet.utils.I18nConstants.INS_ENDING;
import static mcjty.xnet.utils.I18nConstants.LOW_FORMAT;
import static mcjty.xnet.utils.I18nConstants.MAX;
import static mcjty.xnet.utils.I18nConstants.MIN;
import static mcjty.xnet.utils.I18nConstants.PRIORITY_LABEL;
import static mcjty.xnet.utils.I18nConstants.PRIORITY_TOOLTIP;
import static mcjty.xnet.utils.I18nConstants.RATE_LABEL;
import static mcjty.xnet.utils.I18nConstants.SPEED_TOOLTIP;

import static terrails.xnetgases.Constants.EXTRACT_TAGS;
import static terrails.xnetgases.Constants.INSERT_TAGS;
import static terrails.xnetgases.Constants.TAG_FILTER;
import static terrails.xnetgases.Constants.TAG_MIN_MAX;
import static terrails.xnetgases.Constants.TAG_PRIORITY;
import static terrails.xnetgases.Constants.TAG_RATE;
import static terrails.xnetgases.Constants.TAG_REQUIRE_RATE;
import static terrails.xnetgases.Constants.TAG_SPEED;
import static terrails.xnetgases.Constants.TAG_TYPE;
import static terrails.xnetgases.Constants.XNET_GUI_ELEMENTS;
import static terrails.xnetgases.I18nConstants.GAS_MINMAX_TOOLTIP_FORMATTED;
import static terrails.xnetgases.I18nConstants.GAS_RATE_TOOLTIP_FORMATTED;
import static terrails.xnetgases.I18nConstants.REQUIRE_INSERT_RATE_LABEL;

public class ChemicalConnectorSettings extends BaseConnectorSettings<ChemicalStack<?>> {
    
    private InsExtMode connectorMode = InsExtMode.INS;
    private ChemicalEnums.Type connectorType = ChemicalEnums.Type.GAS;

    @Nullable private Integer priority = 0;
    @Nullable private Integer transferRate = null;
    @Nullable private Integer minMaxLimit = null;

    private int operationSpeed = 20;
    private boolean transferRateRequired = false;

    private ItemStack filter = ItemStack.EMPTY;

    public ChemicalConnectorSettings(@Nonnull Direction side) {
        super(side);
    }

    public InsExtMode getConnectorMode() {
        return this.connectorMode;
    }

    public ChemicalEnums.Type getConnectorType() {
        return this.connectorType;
    }

    public int getOperationSpeed() {
        return this.operationSpeed;
    }

    public int getPriority() {
        return this.priority == null ? 0 : priority;
    }

    public boolean isTransferRateRequired() {
        return this.transferRateRequired;
    }

    public int getRate() {
        // 'advanced' is always false here, so default to non-advanced speed
        return Objects.requireNonNullElse(this.transferRate, getMaxRate(false));
    }

    private int getMaxRate(boolean advanced) {
        return switch (connectorType) {
            case GAS -> advanced ? ChemicalChannelModule.maxGasRateAdvanced.get() : ChemicalChannelModule.maxGasRateNormal.get();
            case INFUSE -> advanced ? ChemicalChannelModule.maxInfuseRateAdvanced.get() : ChemicalChannelModule.maxInfuseRateNormal.get();
            case PIGMENT -> advanced ? ChemicalChannelModule.maxPigmentRateAdvanced.get() : ChemicalChannelModule.maxPigmentRateNormal.get();
            case SLURRY -> advanced ? ChemicalChannelModule.maxSlurryRateAdvanced.get() : ChemicalChannelModule.maxSlurryRateNormal.get();
        };
    }

    @Nullable public Integer getMinMaxLimit() {
        return this.minMaxLimit;
    }

    @Nullable
    @Override
    public ChemicalStack<?> getMatcher() {
        if (!filter.isEmpty()) {
            switch (connectorType) {
                case GAS:
                    if (Capabilities.GAS_HANDLER != null && filter.getCapability(Capabilities.GAS_HANDLER).isPresent()) {
                        IGasHandler handler = filter.getCapability(Capabilities.GAS_HANDLER).orElseThrow(() -> new IllegalArgumentException("IGasHandler Capability doesn't exist!"));
                        return handler.getChemicalInTank(0);
                    }
                    break;
                case INFUSE:
                    if (Capabilities.INFUSION_HANDLER != null && filter.getCapability(Capabilities.INFUSION_HANDLER).isPresent()) {
                        IInfusionHandler handler = filter.getCapability(Capabilities.INFUSION_HANDLER).orElseThrow(() -> new IllegalArgumentException("IInfusionHandler Capability doesn't exist!"));
                        return handler.getChemicalInTank(0);
                    }
                    break;
                case PIGMENT:
                    if (Capabilities.PIGMENT_HANDLER != null && filter.getCapability(Capabilities.PIGMENT_HANDLER).isPresent()) {
                        IPigmentHandler handler = filter.getCapability(Capabilities.PIGMENT_HANDLER).orElseThrow(() -> new IllegalArgumentException("IPigmentHandler Capability doesn't exist!"));
                        return handler.getChemicalInTank(0);
                    }
                    break;
                case SLURRY:
                    if (Capabilities.SLURRY_HANDLER != null && filter.getCapability(Capabilities.SLURRY_HANDLER).isPresent()) {
                        ISlurryHandler handler = filter.getCapability(Capabilities.SLURRY_HANDLER).orElseThrow(() -> new IllegalArgumentException("ISlurryHandler Capability doesn't exist!"));
                        return handler.getChemicalInTank(0);
                    }
                    break;
            }
        }
        return null;
    }

    @Override
    public boolean isEnabled(String tag) {
        if (connectorMode == InsExtMode.INS) {
            if (tag.equals(TAG_FACING)) {
                return advanced;
            }
            return INSERT_TAGS.contains(tag);
        } else {
            if (tag.equals(TAG_FACING)) {
                return advanced;
            }
            return EXTRACT_TAGS.contains(tag);
        }
    }

    @Override
    public void update(Map<String, Object> data) {
        super.update(data);
        this.connectorMode = CastTools.safeInsExtMode(data.get(TAG_MODE));
        this.connectorType = ChemicalEnums.Type.safeChemicalType(data.get(TAG_TYPE));
        this.transferRate = (Integer) data.get(TAG_RATE);
        this.minMaxLimit = (Integer) data.get(TAG_MIN_MAX);

        if (data.containsKey(TAG_REQUIRE_RATE)) {
            this.transferRateRequired = (boolean) data.get(TAG_REQUIRE_RATE);
        } else this.transferRateRequired = false;

        if (data.containsKey(TAG_PRIORITY)) {
            this.priority = (Integer) data.get(TAG_PRIORITY);
        } else this.priority = null;

        if (data.containsKey(TAG_SPEED)) {
            this.operationSpeed = Integer.parseInt((String) data.get(TAG_SPEED));
            if (this.operationSpeed == 0) this.operationSpeed = 20;
        } else this.operationSpeed = 20;

        this.filter = ChemicalHelper.normalizeStack((ItemStack) data.get(TAG_FILTER), this.connectorType);
    }

    private String getRateTooltip() {
        return GAS_RATE_TOOLTIP_FORMATTED.i18n(
                (connectorMode == InsExtMode.EXT ? EXT_ENDING : INS_ENDING).i18n(), getMaxRate(advanced)
        );
    }

    private String getMinMaxTooltip() {
        return GAS_MINMAX_TOOLTIP_FORMATTED.i18n(
                (connectorMode == InsExtMode.EXT ? EXT_ENDING : INS_ENDING).i18n(),
                (connectorMode == InsExtMode.EXT ? LOW_FORMAT : HIGH_FORMAT).i18n());
    }

    @Override
    public void createGui(IEditorGui gui) {
        this.advanced = gui.isAdvanced();
        int maxTransferRate = getMaxRate(this.advanced);
        String[] speeds = Arrays.stream(this.advanced ? Constants.ADVANCED_SPEEDS : Constants.SPEEDS)
                                  .map(s -> String.valueOf(Integer.parseInt(s) * 2)).toArray(String[]::new);
        sideGui(gui);
        colorsGui(gui);
        redstoneGui(gui);
        gui
                .nl()
                .translatableChoices(TAG_MODE, this.connectorMode, InsExtMode.values())
                .translatableChoices(TAG_TYPE, this.connectorType, ChemicalEnums.Type.values());

        gui.nl();
        if (this.connectorMode == InsExtMode.INS) {
            gui.label(PRIORITY_LABEL.i18n()).integer(TAG_PRIORITY, PRIORITY_TOOLTIP.i18n(), this.priority, 36);
        } else {
            gui.choices(TAG_SPEED, SPEED_TOOLTIP.i18n(), Integer.toString(this.operationSpeed), speeds);
        }
        gui.nl();
        gui.label(RATE_LABEL.i18n()).integer(TAG_RATE, getRateTooltip(), this.transferRate, 60, maxTransferRate);
        if (this.connectorMode == InsExtMode.INS) {
            gui.shift(10);
            gui.toggle(TAG_REQUIRE_RATE, REQUIRE_INSERT_RATE_LABEL.i18n(), this.transferRateRequired);
        }
        gui.nl();
        gui
                .label((this.connectorMode == InsExtMode.EXT ? MIN : MAX).i18n())
                .integer(TAG_MIN_MAX, getMinMaxTooltip(), this.minMaxLimit, 48)
                .nl()
                .label(FILTER_LABEL.i18n())
                .ghostSlot(TAG_FILTER, filter);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        this.transferRateRequired = tag.getBoolean(TAG_REQUIRE_RATE);
        this.connectorMode = InsExtMode.values()[tag.getByte(TAG_MODE)];
        this.connectorType = ChemicalEnums.Type.values()[tag.getByte(TAG_TYPE)];

        if (tag.contains(TAG_PRIORITY)) {
            this.priority = tag.getInt(TAG_PRIORITY);
        } else this.priority = null;

        if (tag.contains(TAG_RATE)) {
            this.transferRate = tag.getInt(TAG_RATE);
        } else this.transferRate = null;

        if (tag.contains(TAG_MIN_MAX)) {
            this.minMaxLimit = tag.getInt(TAG_MIN_MAX);
        } else this.minMaxLimit = null;

        this.operationSpeed = tag.getInt(TAG_SPEED);
        if (this.operationSpeed == 0) this.operationSpeed = 20;

        if (tag.contains(TAG_FILTER)) {
            CompoundTag itemTag = tag.getCompound(TAG_FILTER);
            this.filter = ChemicalHelper.normalizeStack(ItemStack.of(itemTag), this.connectorType);
        } else this.filter = ItemStack.EMPTY;
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putBoolean(TAG_REQUIRE_RATE, this.transferRateRequired);
        tag.putByte(TAG_MODE, (byte) this.connectorMode.ordinal());
        tag.putByte(TAG_TYPE, (byte) this.connectorType.ordinal());
        tag.putInt(TAG_SPEED, this.operationSpeed);

        if (this.priority != null) tag.putInt(TAG_PRIORITY, this.priority);
        if (this.transferRate != null) tag.putInt(TAG_RATE, this.transferRate);
        if (this.minMaxLimit != null) tag.putInt(TAG_MIN_MAX, this.minMaxLimit);

        if (!this.filter.isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            this.filter.save(itemTag);
            tag.put(TAG_FILTER, itemTag);
        }
    }

    @Override
    public void readFromJson(JsonObject data) {
        super.readFromJsonInternal(data);
        this.connectorMode = getEnumSafe(data, TAG_MODE, ChemicalHelper::getGasMode);
        this.connectorType = getEnumSafe(data, TAG_TYPE, ChemicalEnums.Type.NAME_MAP::get);
        this.priority = getIntegerSafe(data, TAG_PRIORITY);
        this.transferRate = getIntegerSafe(data, TAG_RATE);
        this.transferRateRequired = getBoolSafe(data, TAG_REQUIRE_RATE);
        this.minMaxLimit = getIntegerSafe(data, TAG_MIN_MAX);
        this.operationSpeed = getIntegerNotNull(data, TAG_SPEED);
        if (this.operationSpeed == 0) this.operationSpeed = 20;
        if (data.has(TAG_FILTER)) {
            this.filter = ChemicalHelper.normalizeStack(JSonTools.jsonToItemStack(data.get(TAG_FILTER).getAsJsonObject()), this.connectorType);
        } else this.filter = ItemStack.EMPTY;
    }

    @Override
    public JsonObject writeToJson() {
        JsonObject data = new JsonObject();
        super.writeToJsonInternal(data);
        data.add(TAG_REQUIRE_RATE, new JsonPrimitive(this.transferRateRequired));
        setEnumSafe(data, TAG_MODE, this.connectorMode);
        setEnumSafe(data, TAG_TYPE, this.connectorType);
        setIntegerSafe(data, TAG_PRIORITY, this.priority);
        setIntegerSafe(data, TAG_RATE, this.transferRate);
        setIntegerSafe(data, TAG_MIN_MAX, this.minMaxLimit);
        setIntegerSafe(data, TAG_SPEED, this.operationSpeed);

        if (!this.filter.isEmpty()) {
            data.add(TAG_FILTER, JSonTools.itemStackToJson(this.filter));
        }
        if (this.operationSpeed == 10 || (this.transferRate != null && this.transferRate > this.getMaxRate(false))) {
            data.add(TAG_ADVANCED_NEEDED, new JsonPrimitive(true));
        }
        return data;
    }

    @Nullable
    @Override
    public IndicatorIcon getIndicatorIcon() {
        return switch (this.connectorMode) {
            case INS -> new IndicatorIcon(XNET_GUI_ELEMENTS, 0, 70, 13, 10);
            case EXT -> new IndicatorIcon(XNET_GUI_ELEMENTS, 13, 70, 13, 10);
        };
    }
}
