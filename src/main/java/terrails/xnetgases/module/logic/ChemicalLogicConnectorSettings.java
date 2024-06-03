package terrails.xnetgases.module.logic;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.rftoolsbase.api.xnet.gui.IEditorGui;
import mcjty.rftoolsbase.api.xnet.gui.IndicatorIcon;
import mcjty.rftoolsbase.api.xnet.helper.AbstractConnectorSettings;
import mcjty.xnet.apiimpl.EnumStringTranslators;
import mcjty.xnet.apiimpl.logic.enums.LogicMode;
import mcjty.xnet.utils.CastTools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static mcjty.xnet.apiimpl.Constants.TAG_LOGIC_MODE;
import static mcjty.xnet.apiimpl.Constants.TAG_REDSTONE_OUT;
import static mcjty.xnet.apiimpl.Constants.TAG_SENSORS;
import static mcjty.xnet.apiimpl.Constants.TAG_COLORS;
import static mcjty.xnet.utils.I18nConstants.LOGIC_RS_LABEL;
import static mcjty.xnet.utils.I18nConstants.LOGIC_RS_TOOLTIP;
import static terrails.xnetgases.Constants.TAG_MODE;
import static terrails.xnetgases.Constants.TAG_SPEED;
import static terrails.xnetgases.Constants.XNET_GUI_ELEMENTS;


public class ChemicalLogicConnectorSettings extends AbstractConnectorSettings {

    private static final Set<String> TAGS = ImmutableSet.of(TAG_REDSTONE_OUT, TAG_MODE, TAG_RS, TAG_COLOR+"0", TAG_COLOR+"1", TAG_COLOR+"2", TAG_COLOR+"3");

    public static final int SENSORS = 4;

    private LogicMode connectorMode = LogicMode.SENSOR;
    private final List<ChemicalSensor> sensors;

    private int colors;
    private Integer redstoneOut;

    public ChemicalLogicConnectorSettings(@Nonnull Direction side) {
        super(side);
        sensors = new ArrayList<>(SENSORS);
        for (int i = 0; i < SENSORS; i++) sensors.add(new ChemicalSensor(i));
    }

    public List<ChemicalSensor> getSensors() {
        return sensors;
    }

    public void setColorMask(int colors) {
        this.colors = colors;
    }

    public int getColorMask() {
        return colors;
    }

    public Integer getRedstoneOut() {
        return redstoneOut;
    }

    @Override
    public boolean isEnabled(String tag) {
        if (tag.equals(TAG_FACING)) {
            return advanced && connectorMode != LogicMode.OUTPUT;
        }
        if (tag.equals(TAG_SPEED)) {
            return true;
        }
        for (ChemicalSensor sensor : sensors) {
            if (sensor.isEnabled(tag)) {
                return true;
            }
        }

        return TAGS.contains(tag);
    }

    public LogicMode getConnectorMode() {
        return connectorMode;
    }

    @Override
    public void createGui(IEditorGui gui) {
        advanced = gui.isAdvanced();
        sideGui(gui);
        colorsGui(gui);
        redstoneGui(gui);
        gui.nl().translatableChoices(TAG_MODE, connectorMode, LogicMode.values()).nl();

        switch (connectorMode) {
            case SENSOR -> sensors.forEach(sensor -> sensor.createGui(gui));
            case OUTPUT -> gui.label(LOGIC_RS_LABEL.i18n()).integer(TAG_REDSTONE_OUT, LOGIC_RS_TOOLTIP.i18n(), redstoneOut, 40, 15, 0).nl();
        }

    }

    @Override
    public void update(Map<String, Object> data) {
        super.update(data);
        connectorMode = CastTools.safeLogicMode(data.get(TAG_MODE));
        if (connectorMode == LogicMode.SENSOR) {
            sensors.forEach(sensors -> sensors.update(data));
        } else {
            redstoneOut = (Integer) data.get(TAG_REDSTONE_OUT);
        }
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putByte(TAG_MODE, (byte) connectorMode.ordinal());
        tag.putInt(TAG_COLORS, colors);
        sensors.forEach(sensor -> sensor.writeToNBT(tag));
        if (redstoneOut != null) {
            tag.putInt(TAG_REDSTONE_OUT, redstoneOut);
        }
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        connectorMode = LogicMode.values()[tag.getByte(TAG_MODE)];
        colors = tag.getInt(TAG_COLORS);
        sensors.forEach(sensor -> sensor.readFromNBT(tag));
        redstoneOut = tag.getInt(TAG_REDSTONE_OUT);
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
    public JsonObject writeToJson() {
        JsonObject object = new JsonObject();
        super.writeToJsonInternal(object);
        setEnumSafe(object, TAG_MODE, connectorMode);
        JsonArray sensorArray = new JsonArray();
        for (ChemicalSensor sensor : sensors) {
            JsonObject o = new JsonObject();
            sensor.writeToJson(o);
            sensorArray.add(o);
        }
        object.add(TAG_SENSORS, sensorArray);
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
            sensor.readFromJson(o);
            sensors.add(sensor);
        }
    }
}
