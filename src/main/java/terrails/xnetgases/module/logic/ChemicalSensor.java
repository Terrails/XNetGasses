package terrails.xnetgases.module.logic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.rftoolsbase.api.xnet.channels.Color;
import mcjty.rftoolsbase.api.xnet.gui.IEditorGui;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;

import terrails.xnetgases.module.ChemicalMatcher;
import terrails.xnetgases.module.logic.enums.SensorMode;
import terrails.xnetgases.module.logic.enums.SensorOperator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

import static mcjty.xnet.apiimpl.Constants.*;
import static mcjty.xnet.utils.I18nConstants.LOGIC_SENSOR_AMOUNT_TOOLTIP;
import static mcjty.xnet.utils.I18nConstants.LOGIC_SENSOR_OUT_COLOR_TOOLTIP;

public class ChemicalSensor {

    public static final Codec<ChemicalSensor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("index").forGetter(s -> s.index),
            SensorMode.CODEC.fieldOf("sensorMode").forGetter(ChemicalSensor::getSensorMode),
            SensorOperator.CODEC.fieldOf("operator").forGetter(ChemicalSensor::getOperator),
            Codec.INT.fieldOf("amount").forGetter(ChemicalSensor::getAmount),
            Color.CODEC.fieldOf("outputColor").forGetter(ChemicalSensor::getOutputColor),
            ItemStack.OPTIONAL_CODEC.fieldOf("filter").forGetter(s -> s.matcher.getStack())
    ).apply(instance, ChemicalSensor::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalSensor> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, s -> s.index,
            SensorMode.STREAM_CODEC, ChemicalSensor::getSensorMode,
            SensorOperator.STREAM_CODEC, ChemicalSensor::getOperator,
            ByteBufCodecs.INT, ChemicalSensor::getAmount,
            Color.STREAM_CODEC, ChemicalSensor::getOutputColor,
            ItemStack.OPTIONAL_STREAM_CODEC, s -> s.matcher.getStack(),
            ChemicalSensor::new
    );

    private final String modeTag;
    private final String operatorTag;
    private final String amountTag;
    private final String colorTag;
    private final String filterTag;

    private final int index;
    private SensorMode sensorMode = SensorMode.OFF;
    private SensorOperator operator = SensorOperator.EQUAL;
    private int amount = 0;
    private Color outputColor = Color.OFF;
    private ChemicalMatcher matcher = ChemicalMatcher.EMPTY;

    public ChemicalSensor(int index) {
        this.index = index;
        String temp = String.format("sensor%s_", index);
        modeTag = temp + TAG_MODE;
        operatorTag = temp + TAG_OPERATOR;
        amountTag = temp + TAG_AMOUNT;
        colorTag = temp + TAG_COLOR;
        filterTag = temp + TAG_FILTER;
    }

    public ChemicalSensor(int index, SensorMode sensorMode, SensorOperator operator, int amount, Color outputColor, ItemStack filter) {
        this(index);
        this.sensorMode = sensorMode;
        this.operator = operator;
        this.amount = amount;
        this.outputColor = outputColor;
        this.matcher = ChemicalMatcher.from(filter);
    }

    public SensorMode getSensorMode() {
        return sensorMode;
    }

    public void setSensorMode(SensorMode sensorMode) {
        this.sensorMode = sensorMode;
    }

    public SensorOperator getOperator() {
        return operator;
    }

    public void setOperator(SensorOperator operator) {
        this.operator = operator;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Color getOutputColor() {
        return outputColor;
    }

    public void setOutputColor(Color outputColor) {
        this.outputColor = outputColor;
    }

    public ChemicalMatcher getMatcher() {
        return matcher;
    }

    public void setMatcher(ChemicalMatcher matcher) {
        this.matcher = matcher;
    }

    public void createGui(IEditorGui gui) {
        gui
                .translatableChoices(modeTag, getSensorMode(), SensorMode.values())
                .translatableChoices(operatorTag, getOperator(), SensorOperator.values())
//                .choices(operatorTag, LOGIC_SENSOR_OPERATOR_TOOLTIP.i18n(), getOperator(), SensorOperator.values())
                .integer(amountTag, LOGIC_SENSOR_AMOUNT_TOOLTIP.i18n(), getAmount(), 46)
                .colors(colorTag, LOGIC_SENSOR_OUT_COLOR_TOOLTIP.i18n(), getOutputColor().getColor(), Color.COLORS)
                .ghostSlot(filterTag, getMatcher().getStack())
                .nl();
    }

    public boolean isEnabled(String tag) {
        if (modeTag.equals(tag)) {
            return true;
        }
        if (operatorTag.equals(tag)) {
            return true;
        }
        if (amountTag.equals(tag)) {
            return true;
        }
        if (colorTag.equals(tag)) {
            return true;
        }
        if (filterTag.equals(tag)) {
            return getSensorMode() != SensorMode.OFF;
        }
        return false;
    }

    public void update(Map<String, Object> data) {
        sensorMode = Optional.ofNullable(data.get(modeTag))
                .map(o -> SensorMode.values()[(int) o])
                .orElse(SensorMode.OFF);

        operator = Optional.ofNullable(data.get(operatorTag))
                .map(o -> SensorOperator.values()[(int) o])
                .orElse(SensorOperator.EQUAL);

        amount = Optional.ofNullable(data.get(amountTag))
                .map(Integer.class::cast)
                .orElse(0);

        outputColor = Optional.ofNullable(data.get(colorTag))
                .map(o -> Color.colorByValue((int) o))
                .orElse(Color.OFF);

        matcher = Optional.ofNullable(data.get(TAG_FILTER))
                .map(ItemStack.class::cast)
                .map(ChemicalMatcher::from)
                .orElse(ChemicalMatcher.EMPTY);
    }

    public boolean test(@Nullable BlockEntity be, @Nonnull Level level, @Nonnull BlockPos pos, ChemicalLogicConnectorSettings settings) {
        if (getSensorMode() == SensorMode.OFF) {
            return false;
        }

        IChemicalHandler handler = Capabilities.CHEMICAL.getCapabilityIfLoaded(level, pos, null, be, settings.getFacing());
        if (handler == null) {
            return false;
        }

        return getOperator().match(getMatcher().amountInTank(handler, settings.getFacing()), getAmount());
    }
}
