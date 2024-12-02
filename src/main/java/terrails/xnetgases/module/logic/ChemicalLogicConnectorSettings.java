package terrails.xnetgases.module.logic;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.JSonTools;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.gui.IEditorGui;
import mcjty.rftoolsbase.api.xnet.gui.IndicatorIcon;
import mcjty.rftoolsbase.api.xnet.helper.AbstractConnectorSettings;
import mcjty.rftoolsbase.api.xnet.helper.BaseStringTranslators;
import mcjty.xnet.apiimpl.EnumStringTranslators;
import mcjty.xnet.apiimpl.logic.RSOutput;
import mcjty.xnet.apiimpl.logic.enums.LogicFilter;
import mcjty.xnet.apiimpl.logic.enums.LogicMode;

import terrails.xnetgases.module.ChemicalMatcher;
import terrails.xnetgases.module.logic.enums.SensorMode;
import terrails.xnetgases.module.logic.enums.SensorOperator;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static mcjty.xnet.apiimpl.Constants.*;
import static terrails.xnetgases.Constants.CHEMICAL_LOGIC_FEATURES;
import static terrails.xnetgases.Constants.XNET_GUI_ELEMENTS;

public class ChemicalLogicConnectorSettings extends AbstractConnectorSettings {

    public static final MapCodec<ChemicalLogicConnectorSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BaseSettings.CODEC.fieldOf("base").forGetter(s -> s.settings),
            Direction.CODEC.fieldOf("side").forGetter(ChemicalLogicConnectorSettings::getSide),
            LogicMode.CODEC.fieldOf("mode").forGetter(ChemicalLogicConnectorSettings::getConnectorMode),
            RSOutput.CODEC.fieldOf("output").forGetter(ChemicalLogicConnectorSettings::getOutput),
            Codec.list(ChemicalSensor.CODEC).fieldOf("sensors").forGetter(ChemicalLogicConnectorSettings::getSensors)
    ).apply(instance, ChemicalLogicConnectorSettings::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalLogicConnectorSettings> STREAM_CODEC = StreamCodec.composite(
            BaseSettings.STREAM_CODEC, s -> s.settings,
            Direction.STREAM_CODEC, ChemicalLogicConnectorSettings::getSide,
            LogicMode.STREAM_CODEC, ChemicalLogicConnectorSettings::getConnectorMode,
            RSOutput.STREAM_CODEC, ChemicalLogicConnectorSettings::getOutput,
            ChemicalSensor.STREAM_CODEC.apply(ByteBufCodecs.list()), ChemicalLogicConnectorSettings::getSensors,
            ChemicalLogicConnectorSettings::new
    );

    private static final int MAX_SENSORS = 4;

    private final List<ChemicalSensor> sensors;
    private LogicMode connectorMode = LogicMode.SENSOR;
    private RSOutput output = null;
    private int colorMask;

    public ChemicalLogicConnectorSettings(@Nonnull BaseSettings base, @Nonnull Direction direction, LogicMode connectorMode, RSOutput output, List<ChemicalSensor> sensors) {
        super(base, direction);
        this.connectorMode = connectorMode;
        this.output = output;
        this.sensors = sensors;
    }

    public ChemicalLogicConnectorSettings(@Nonnull Direction direction) {
        super(DEFAULT_SETTINGS, direction);
        this.sensors = IntStream.range(0, MAX_SENSORS).mapToObj(ChemicalSensor::new).collect(Collectors.toCollection(ArrayList::new));
        this.output = new RSOutput(this.advanced);
    }

    @Override
    public IChannelType getType() {
        return ChemicalLogicChannelType.TYPE;
    }

    public List<ChemicalSensor> getSensors() {
        return sensors;
    }

    public LogicMode getConnectorMode() {
        return connectorMode;
    }

    public RSOutput getOutput() {
        return output;
    }

    public int getColorMask() {
        return colorMask;
    }

    public void setColorMask(int colors) {
        this.colorMask = colors;
    }

    @Override
    public void createGui(IEditorGui gui) {
        advanced = gui.isAdvanced();
        sideGui(gui);
        colorsGui(gui);
        redstoneGui(gui);
        gui.nl().translatableChoices(TAG_LOGIC_MODE, connectorMode, LogicMode.values()).nl();
        if (getConnectorMode() == LogicMode.SENSOR) {
            sensors.forEach(s -> s.createGui(gui));
        } else {
            output.createGui(gui);
        }
    }

    @Override
    public boolean isEnabled(String tag) {
        if (tag.equals(TAG_FACING)) {
            return advanced && getConnectorMode() != LogicMode.OUTPUT;
        }

        for (ChemicalSensor sensor : sensors) {
            if (sensor.isEnabled(tag)) {
                return true;
            }
        }

        if (output.isEnabled(tag)) {
            return true;
        }

        return CHEMICAL_LOGIC_FEATURES.contains(tag);
    }

    @Override
    public void update(Map<String, Object> data) {
        super.update(data);
        connectorMode = Optional.ofNullable(data.get(TAG_LOGIC_MODE))
                .map(o -> LogicMode.values()[(int) o])
                .orElse(LogicMode.SENSOR);

        if (connectorMode == LogicMode.SENSOR) {
            sensors.forEach(sensors -> sensors.update(data));
        } else {
            output.update(data);
        }
    }

    @Nullable
    @Override
    public IndicatorIcon getIndicatorIcon() {
        return switch (connectorMode) {
            case SENSOR -> new IndicatorIcon(XNET_GUI_ELEMENTS, 26, 70, 13, 10);
            case OUTPUT -> new IndicatorIcon(XNET_GUI_ELEMENTS, 39, 70, 13, 10);
        };
    }

    @Nullable
    @Override
    public String getIndicator() {
        return null;
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        output.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        output.readFromNBT(tag);
    }

    @Override
    public JsonObject writeToJson() {
        JsonObject object = new JsonObject();
        super.writeToJsonInternal(object);
        setEnumSafe(object, TAG_LOGIC_MODE, connectorMode);

        JsonArray sensorArray = new JsonArray();
        for (ChemicalSensor sensor : sensors) {
            JsonObject o = new JsonObject();
            setEnumSafe(o, TAG_SENSOR_MODE, sensor.getSensorMode());
            setEnumSafe(o, TAG_OUTPUT_COLOR, sensor.getOutputColor());
            setEnumSafe(o, TAG_OPERATOR, sensor.getOperator());
            setIntegerSafe(o, TAG_AMOUNT, sensor.getAmount());
            if (!sensor.getMatcher().isEmpty()) {
                o.add(TAG_FILTER, JSonTools.itemStackToJson(sensor.getMatcher().getStack()));
            }
            sensorArray.add(o);
        }
        object.add(TAG_SENSORS, sensorArray);

        if (!output.getLogicFilter().equals(LogicFilter.DIRECT)) {
            object.add(TAG_ADVANCED_NEEDED, new JsonPrimitive(true));
        }

        JsonObject outputJSON = new JsonObject();
        setEnumSafe(outputJSON, TAG_RS_FILTER, output.getLogicFilter());
        setEnumSafe(outputJSON, TAG_RS_CHANNEL_1, output.getInputChannel1());
        setEnumSafe(outputJSON, TAG_RS_CHANNEL_2, output.getInputChannel2());
        outputJSON.addProperty(TAG_RS_COUNTING_HOLDER, output.getCountingHolder());
        outputJSON.addProperty(TAG_RS_TICKS_HOLDER, output.getTicksHolder());
        outputJSON.addProperty(TAG_REDSTONE_OUT, output.getRedstoneOut());
        object.add(TAG_OUTPUT, outputJSON);

        return object;
    }

    @Override
    public void readFromJson(JsonObject object) {
        super.readFromJsonInternal(object);
        connectorMode = getEnumSafe(object, TAG_LOGIC_MODE, EnumStringTranslators::getLogicMode);

        JsonArray sensorArray = object.get(TAG_SENSORS).getAsJsonArray();
        sensors.clear();
        for (JsonElement oe : sensorArray) {
            JsonObject o = oe.getAsJsonObject();
            ChemicalSensor sensor = new ChemicalSensor(sensors.size());
            sensor.setSensorMode(getEnumSafe(o, TAG_SENSOR_MODE, SensorMode::byName));
            sensor.setOutputColor(getEnumSafe(o, TAG_OUTPUT_COLOR, BaseStringTranslators::getColor));
            sensor.setOperator(getEnumSafe(o, TAG_OPERATOR, SensorOperator::byName));
            sensor.setAmount(getIntegerNotNull(o, TAG_AMOUNT));
            if (o.has(TAG_FILTER)) {
                sensor.setMatcher(ChemicalMatcher.from(JSonTools.jsonToItemStack(o.get(TAG_FILTER).getAsJsonObject())));
            } else {
                sensor.setMatcher(ChemicalMatcher.EMPTY);
            }
            sensors.add(sensor);
        }

        JsonObject outputJSON = object.getAsJsonObject(TAG_OUTPUT);
        output = new RSOutput(advanced);
        output.setLogicFilter(getEnumSafe(outputJSON, TAG_RS_FILTER, EnumStringTranslators::getLogicFilter));
        output.setInputChannel1(getEnumSafe(outputJSON, TAG_RS_CHANNEL_1, BaseStringTranslators::getColor));
        output.setInputChannel2(getEnumSafe(outputJSON, TAG_RS_CHANNEL_2, BaseStringTranslators::getColor));
        output.setCountingHolder(getIntegerNotNull(outputJSON, TAG_RS_COUNTING_HOLDER));
        output.setTicksHolder(getIntegerNotNull(outputJSON, TAG_RS_TICKS_HOLDER));
        output.setRedstoneOut(getIntegerNotNull(outputJSON, TAG_REDSTONE_OUT));
    }
}
