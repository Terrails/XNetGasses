package terrails.xnetgases.module.chemical;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import mcjty.rftoolsbase.api.xnet.channels.IControllerContext;
import mcjty.rftoolsbase.api.xnet.gui.IEditorGui;
import mcjty.rftoolsbase.api.xnet.gui.IndicatorIcon;
import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import mcjty.xnet.apiimpl.Constants;
import mcjty.xnet.apiimpl.enums.InsExtMode;
import mcjty.xnet.apiimpl.ConnectedEntity;
import mcjty.xnet.modules.cables.blocks.ConnectorTileEntity;
import mcjty.xnet.setup.Config;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import terrails.xnetgases.helper.BaseChannelSettings;
import terrails.xnetgases.helper.ModuleEnums.ChannelMode;
import terrails.xnetgases.module.chemical.utils.ChemicalHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import static terrails.xnetgases.Constants.TAG_MODE;
import static terrails.xnetgases.Constants.XNET_GUI_ELEMENTS;

public class ChemicalChannelSettings extends BaseChannelSettings {

    public static final String TAG_DELAY = "delay";
    public static final String TAG_DISTRIBUTE_OFFSET = "distribute_offset";

    private ChannelMode channelMode = ChannelMode.DISTRIBUTE;
    private int delay;
    private int roundRobinOffset;

    private List<ConnectedEntity<ChemicalConnectorSettings>> extractors;
    private List<ConnectedEntity<ChemicalConnectorSettings>> consumers;

    public ChemicalChannelSettings() {
        this.delay = 0;
        this.roundRobinOffset = 0;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        this.channelMode = ChannelMode.values()[tag.getByte(TAG_MODE)];
        this.delay = tag.getInt(TAG_DELAY);
        this.roundRobinOffset = tag.getInt(TAG_DISTRIBUTE_OFFSET);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        tag.putByte(TAG_MODE, (byte) this.channelMode.ordinal());
        tag.putInt(TAG_DELAY, this.delay);
        tag.putInt(TAG_DISTRIBUTE_OFFSET, this.roundRobinOffset);
    }

    @Override
    public void readFromJson(JsonObject data) {
        this.channelMode = ChannelMode.byName(data.get(TAG_MODE).getAsString());
        this.delay = data.get(TAG_DELAY).getAsInt();
        this.roundRobinOffset = data.get(TAG_DISTRIBUTE_OFFSET).getAsInt();
    }

    @Override
    public JsonObject writeToJson() {
        JsonObject object = new JsonObject();
        object.add(TAG_MODE, new JsonPrimitive(this.channelMode.name()));
        object.add(TAG_DELAY, new JsonPrimitive(this.delay));
        object.add(TAG_DISTRIBUTE_OFFSET, new JsonPrimitive(this.roundRobinOffset));
        return object;
    }

    @Override
    public void tick(int channel, IControllerContext context) {
        --this.delay;
        if (this.delay <= 0) {
            this.delay = 200 * 6;
        }

        if (this.delay % 10 != 0) {
            return;
        }
        updateCache(channel, context);
        Level world = context.getControllerWorld();
        for (ConnectedEntity<ChemicalConnectorSettings> extractor : this.extractors) {
            ChemicalConnectorSettings settings = extractor.settings();
            if (this.delay % settings.getOperationSpeed() != 0) {
                continue;
            }
            if (!LevelTools.isLoaded(world, extractor.getBlockPos())) {
                continue;
            }
            if (!checkRedstone(settings, extractor.getConnectorEntity(), context)) {
                return;
            }

            BlockEntity be = extractor.getConnectedEntity();
            ChemicalEnums.Type type = settings.getConnectorType();

            IChemicalHandler<?, ?> handler = ChemicalHelper.handler(be, settings.getFacing(), type).orElse(null);
            if (handler == null) {
                continue;
            }

            tickGasHandler(context, settings, handler);
        }
    }

    private void tickGasHandler(IControllerContext context, ChemicalConnectorSettings settings, IChemicalHandler<?,?> handler) {
        if (!context.checkAndConsumeRF(Config.controllerOperationRFT.get())) {
            return;
        }
        ChemicalEnums.Type type = settings.getConnectorType();
        ChemicalStack<?> filter = settings.getMatcher();
        long amount = ChemicalHelper.amountInTank(handler, settings.getFacing(), filter, type);
        // Just skip extractor if there is no chemical in tank
        if (amount <= 0) {
            return;
        }
        long toExtract = settings.getRate();

        Integer count = settings.getMinMaxLimit();
        if (count != null) {
            long canExtract = amount - count;
            if (canExtract <= 0) {
                return;
            }
            toExtract = Math.min(toExtract, canExtract);
        }

        while (true) {
            ChemicalStack<?> extractStack = ChemicalHelper.extract(handler, toExtract, settings.getFacing(), Action.SIMULATE, type);
            if (extractStack.isEmpty() || (filter != null && filter.getType() != extractStack.getType())) {
                return;
            }
            toExtract = extractStack.getAmount();
            long remaining = insertGas(context, extractStack, type);
            toExtract -= remaining;
            if (remaining != toExtract) {
                ChemicalHelper.extract(handler, toExtract, settings.getFacing(), Action.EXECUTE, type);
                return;
            }
        }
    }

    private long insertGas(IControllerContext context, ChemicalStack<?> stack, ChemicalEnums.Type type) {
        Level level = context.getControllerWorld();
        if (this.channelMode == ChannelMode.PRIORITY) {
            this.roundRobinOffset = 0;
        }
        long amount = stack.getAmount();
        int currentRoundRobinOffset = this.roundRobinOffset;
        for (int j = 0; j < this.consumers.size(); j++) {
            int i = (j + currentRoundRobinOffset) % this.consumers.size();
            ConnectedEntity<ChemicalConnectorSettings> consumer = this.consumers.get(i);
            ChemicalConnectorSettings settings = consumer.settings();

            if (settings.getConnectorType() != type) {
                continue;
            }

            if (settings.getMatcher() != null && settings.getMatcher().getType() != stack.getType()) {
                continue;
            }

            if (!LevelTools.isLoaded(level, consumer.getBlockPos()) || !checkRedstone(settings, consumer.getConnectorEntity(), context)) {
                continue;
            }

            BlockEntity be = consumer.getConnectedEntity();

            IChemicalHandler<?, ?> handler = ChemicalHelper.handler(be, settings.getFacing(), settings.getConnectorType()).orElse(null);
            if (handler != null) {
                long toInsert = Math.min(settings.getRate(), amount);

                Integer count = settings.getMinMaxLimit();
                if (count != null) {
                    long a = ChemicalHelper.amountInTank(handler, settings.getFacing(), settings.getMatcher(), settings.getConnectorType());
                    long canInsert = count - a;
                    if (canInsert <= 0) {
                        continue;
                    }
                    toInsert = Math.min(toInsert, canInsert);
                }

                if (settings.isTransferRateRequired() && settings.getRate() > toInsert) {
                    continue;
                }

                ChemicalStack<?> copy = stack.copy();
                copy.setAmount(toInsert);

                ChemicalStack<?> remaining = ChemicalHelper.insert(handler, copy, settings.getFacing(), Action.EXECUTE, settings.getConnectorType());
                if (remaining.isEmpty() || (!remaining.isEmpty() && copy.getAmount() != remaining.getAmount())) {
                    this.roundRobinOffset = (this.roundRobinOffset + 1) % this.consumers.size();
                    amount -= (copy.getAmount() - remaining.getAmount());
                    if (amount <= 0) {
                        return 0;
                    }
                }
            }
        }
        return amount;
    }

    @Override
    public void cleanCache() {
        this.extractors = null;
        this.consumers = null;
    }

    private void updateCache(int channel, IControllerContext context) {
        if (this.extractors == null) {
            this.extractors = new ArrayList<>();
            this.consumers = new ArrayList<>();
            Level world = context.getControllerWorld();
            Map<SidedConsumer, IConnectorSettings> connectors = context.getConnectors(channel);
            Iterator<Map.Entry<SidedConsumer, IConnectorSettings>> iterator = connectors.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<SidedConsumer, IConnectorSettings> entry = iterator.next();
                ChemicalConnectorSettings settings = (ChemicalConnectorSettings) entry.getValue();
                ConnectedEntity<ChemicalConnectorSettings> connectedEntity;
                connectedEntity = getConnectedEntityInfo(context, entry, world, settings);
                if (connectedEntity == null) {
                    continue;
                }
                if (settings.getConnectorMode() == InsExtMode.EXT) {
                    this.extractors.add(connectedEntity);
                } else {
                    this.consumers.add(connectedEntity);
                }
            }

            connectors = context.getRoutedConnectors(channel);
            iterator = connectors.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<SidedConsumer, IConnectorSettings> entry = iterator.next();
                ChemicalConnectorSettings settings = (ChemicalConnectorSettings) entry.getValue();
                ConnectedEntity<ChemicalConnectorSettings> connectedEntity;
                connectedEntity = getConnectedEntityInfo(context, entry, world, settings);
                if (connectedEntity == null) {
                    continue;
                }
                if (settings.getConnectorMode() == InsExtMode.INS) {
                    this.consumers.add(connectedEntity);
                }
            }

            this.consumers.sort((o1, o2) -> Integer.compare(o2.settings().getPriority(), o1.settings().getPriority()));
        }
    }

    @javax.annotation.Nullable
    private ConnectedEntity<ChemicalConnectorSettings> getConnectedEntityInfo(
            IControllerContext context, Map.Entry<SidedConsumer, IConnectorSettings> entry, @Nonnull Level world, @Nonnull ChemicalConnectorSettings con
    ) {
        BlockPos connectorPos = context.findConsumerPosition(entry.getKey().consumerId());
        if (connectorPos == null) {
            return null;
        }
        ConnectorTileEntity connectorTileEntity = (ConnectorTileEntity) world.getBlockEntity(connectorPos);
        if (connectorTileEntity == null) {
            return null;
        }
        BlockPos connectedBlockPos = connectorPos.relative(entry.getKey().side());
        BlockEntity connectedEntity = world.getBlockEntity(connectedBlockPos);
        if (connectedEntity == null) {
            return null;
        }

        return new ConnectedEntity<>(entry.getKey(), con, connectorPos, connectedBlockPos, connectedEntity, connectorTileEntity);
    }

    @Nullable
    @Override
    public IndicatorIcon getIndicatorIcon() {
        return new IndicatorIcon(XNET_GUI_ELEMENTS, 0, 90, 11, 10);
    }

    @Override
    public void createGui(IEditorGui gui) {
        gui.nl().translatableChoices(TAG_MODE, this.channelMode, ChannelMode.values());
    }

    @Override
    public boolean isEnabled(String s) {
        return true;
    }

    @Override
    public void update(Map<String, Object> map) {
        this.channelMode = ChemicalHelper.safeChannelMode(map.get(Constants.TAG_MODE));
    }
}
