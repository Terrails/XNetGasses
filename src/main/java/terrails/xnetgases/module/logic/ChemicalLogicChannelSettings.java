package terrails.xnetgases.module.logic;

import com.google.gson.JsonObject;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.xnet.channels.IChannelSettings;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import mcjty.rftoolsbase.api.xnet.channels.IControllerContext;
import mcjty.rftoolsbase.api.xnet.gui.IEditorGui;
import mcjty.rftoolsbase.api.xnet.gui.IndicatorIcon;
import mcjty.rftoolsbase.api.xnet.helper.DefaultChannelSettings;
import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import mcjty.xnet.apiimpl.ConnectedBlock;
import mcjty.xnet.apiimpl.logic.ConnectedEntity;
import mcjty.xnet.apiimpl.logic.enums.LogicMode;
import mcjty.xnet.modules.cables.blocks.ConnectorTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static mcjty.xnet.apiimpl.Constants.TAG_COLORS;
import static terrails.xnetgases.Constants.XNET_GUI_ELEMENTS;

public class ChemicalLogicChannelSettings extends DefaultChannelSettings implements IChannelSettings {

    private int colors = 0;
    private List<ConnectedEntity<ChemicalLogicConnectorSettings>> sensors = null;
    private List<ConnectedBlock<ChemicalLogicConnectorSettings>> outputs = null;

    @Override
    public JsonObject writeToJson() {
        return new JsonObject();
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        colors = tag.getInt(TAG_COLORS);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        tag.putInt(TAG_COLORS, colors);
    }

    @Override
    public void tick(int channel, IControllerContext context) {
        updateCache(channel, context);
        Level level = context.getControllerWorld();

        colors = 0;
        for (ConnectedEntity<ChemicalLogicConnectorSettings> entry : sensors) {
            ChemicalLogicConnectorSettings settings = entry.settings();
            int sensorColors = 0;
            BlockPos pos = entry.getBlockPos();
            if (!LevelTools.isLoaded(level, pos)) {
                // If it is not chunkloaded we just use the color settings as we last remembered it
                colors |= settings.getColorMask();
                continue;
            }

            boolean sense = !checkRedstone(level, settings, entry.connectorPos());
            if (sense && !context.matchColor(settings.getColorsMask())) {
                sense = false;
            }

            // If sense is false the sensor is disabled which means the colors from it will also be disabled
            if (sense) {
                BlockEntity te = entry.getConnectedEntity();

                for (ChemicalSensor sensor : settings.getSensors()) {
                    if (sensor.test(te, settings)) {
                        sensorColors |= 1 << sensor.getOutputColor().ordinal();
                    }
                }
            }

            settings.setColorMask(sensorColors);
            colors |= sensorColors;
        }

        for (ConnectedBlock<ChemicalLogicConnectorSettings> entry : outputs) {
            ChemicalLogicConnectorSettings settings = entry.settings();

            BlockPos connectorPos = entry.connectorPos();
            Direction side = entry.sidedConsumer().side();
            if (!LevelTools.isLoaded(level, connectorPos)) {
                continue;
            }

            ConnectorTileEntity connectorTileEntity = entry.getConnectorEntity();
            int powerOut;
            if (checkRedstone(level, settings, connectorPos)) {
                powerOut = 0;
            } else if (!context.matchColor(settings.getColorsMask())) {
                powerOut = 0;
            } else {
                powerOut = settings.getRedstoneOut() == null ? 0 : settings.getRedstoneOut();
            }
            connectorTileEntity.setPowerOut(side, powerOut);
        }
    }

    private void updateCache(int channel, IControllerContext context) {
        if (sensors == null || outputs == null) {
            sensors = new ArrayList<>();
            outputs = new ArrayList<>();
            Level world = context.getControllerWorld();
            Map<SidedConsumer, IConnectorSettings> connectors = context.getConnectors(channel);
            for (Map.Entry<SidedConsumer, IConnectorSettings> entry : connectors.entrySet()) {
                ChemicalLogicConnectorSettings con = (ChemicalLogicConnectorSettings) entry.getValue();
                ConnectedBlock<ChemicalLogicConnectorSettings> connectedBlock;
                connectedBlock = getConnectedBlockInfo(context, entry, world, con);
                if (connectedBlock == null) {
                    continue;
                }
                if (con.getConnectorMode() == LogicMode.SENSOR) {
                    ConnectedEntity<ChemicalLogicConnectorSettings> connectedEntity = getConnectedEntity(connectedBlock, world);
                    if (connectedEntity == null) {
                        continue;
                    }
                    sensors.add(connectedEntity);
                } else {
                    outputs.add(connectedBlock);
                }
            }

            connectors = context.getRoutedConnectors(channel);
            for (Map.Entry<SidedConsumer, IConnectorSettings> entry : connectors.entrySet()) {
                ChemicalLogicConnectorSettings con = (ChemicalLogicConnectorSettings) entry.getValue();
                if (con.getConnectorMode() == LogicMode.OUTPUT) {
                    ConnectedBlock<ChemicalLogicConnectorSettings> connectedBlock;
                    connectedBlock = getConnectedBlockInfo(context, entry, world, con);
                    outputs.add(connectedBlock);
                }
            }
        }
    }

    @Nullable
    private ConnectedEntity<ChemicalLogicConnectorSettings> getConnectedEntity(
            @Nonnull ConnectedBlock<ChemicalLogicConnectorSettings> connectedBlock, @Nonnull Level world
    ) {
        BlockEntity connectedEntity = world.getBlockEntity(connectedBlock.getBlockPos());
        if (connectedEntity == null) {
            return null;
        }
        return new ConnectedEntity<>(connectedBlock, connectedEntity);
    }

    @Nullable
    private ConnectedBlock<ChemicalLogicConnectorSettings> getConnectedBlockInfo(
            IControllerContext context, Map.Entry<SidedConsumer, IConnectorSettings> entry, @Nonnull Level world,
            @Nonnull ChemicalLogicConnectorSettings con
    ) {
        BlockPos connectorPos = context.findConsumerPosition(entry.getKey().consumerId());
        if (connectorPos == null) {
            return null;
        }
        ConnectorTileEntity connectorEntity = (ConnectorTileEntity) world.getBlockEntity(connectorPos);
        if (connectorEntity == null) {
            return null;
        }
        BlockPos connectedBlockPos = connectorPos.relative(entry.getKey().side());
        BlockEntity connectedEntity = world.getBlockEntity(connectedBlockPos);
        if (connectedEntity == null) {
            return new ConnectedBlock<>(entry.getKey(), con, connectorPos, connectedBlockPos, connectorEntity);
        }
        return new ConnectedEntity<>(entry.getKey(), con, connectorPos, connectedBlockPos, connectedEntity, connectorEntity);
    }

    @Override
    public void cleanCache() {
        sensors = null;
        outputs = null;
    }

    @Override
    public int getColors() {
        return colors;
    }

    @Nullable
    @Override
    public IndicatorIcon getIndicatorIcon() {
        return new IndicatorIcon(XNET_GUI_ELEMENTS, 11, 90, 11, 10);
    }

    @Nullable
    @Override
    public String getIndicator() {
        return null;
    }

    @Override
    public boolean isEnabled(String s) {
        return true;
    }

    @Override
    public void createGui(IEditorGui iEditorGui) { }

    @Override
    public void update(Map<String, Object> map) { }
}
