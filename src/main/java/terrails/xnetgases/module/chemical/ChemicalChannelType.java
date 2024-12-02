package terrails.xnetgases.module.chemical;

import com.mojang.serialization.MapCodec;
import mcjty.rftoolsbase.api.xnet.channels.IChannelSettings;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import terrails.xnetgases.I18nConstants;
import terrails.xnetgases.module.ChemicalHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;

public class ChemicalChannelType implements IChannelType {

    public static final IChannelType TYPE = new ChemicalChannelType();

    @Override
    public String getID() {
        return "mekanism.chemical";
    }

    @Override
    public String getName() {
        return I18nConstants.CHANNEL_CHEMICAL.i18n();
    }

    @Override
    public MapCodec<? extends IChannelSettings> getCodec() {
        return ChemicalChannelSettings.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ? extends IChannelSettings> getStreamCodec() {
        return ChemicalChannelSettings.STREAM_CODEC;
    }

    @Override
    public MapCodec<? extends IConnectorSettings> getConnectorCodec() {
        return ChemicalConnectorSettings.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ? extends IConnectorSettings> getConnectorStreamCodec() {
        return ChemicalConnectorSettings.STREAM_CODEC;
    }

    @Override
    public boolean supportsBlock(@NotNull Level level, @NotNull BlockPos pos, @Nullable Direction direction) {
        return ChemicalHelper.blockSupportsChemicals(level, pos, direction);
    }

    @NotNull
    @Override
    public IConnectorSettings createConnector(@NotNull Direction direction) {
        return new ChemicalConnectorSettings(direction);
    }

    @NotNull
    @Override
    public IChannelSettings createChannel() {
        return new ChemicalChannelSettings();
    }
}
