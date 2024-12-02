package terrails.xnetgases.module.logic;

import com.mojang.serialization.MapCodec;
import mcjty.rftoolsbase.api.xnet.channels.IChannelSettings;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;

import terrails.xnetgases.I18nConstants;
import terrails.xnetgases.module.ChemicalHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class ChemicalLogicChannelType implements IChannelType {

    public static final ChemicalLogicChannelType TYPE = new ChemicalLogicChannelType();

    @Override
    public String getID() {
        return "mekanism.logic";
    }

    @Override
    public String getName() {
        return I18nConstants.CHANNEL_CHEMICAL_LOGIC.i18n();
    }

    @Override
    public MapCodec<? extends IChannelSettings> getCodec() {
        return ChemicalLogicChannelSettings.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ? extends IChannelSettings> getStreamCodec() {
        return ChemicalLogicChannelSettings.STREAM_CODEC;
    }

    @Override
    public MapCodec<? extends IConnectorSettings> getConnectorCodec() {
        return ChemicalLogicConnectorSettings.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ? extends IConnectorSettings> getConnectorStreamCodec() {
        return ChemicalLogicConnectorSettings.STREAM_CODEC;
    }

    @Override
    public boolean supportsBlock(@Nonnull Level level, @Nonnull BlockPos pos, @Nullable Direction direction) {
        return ChemicalHelper.blockSupportsChemicals(level, pos, direction);
    }

    @Nonnull
    @Override
    public IConnectorSettings createConnector(@Nonnull Direction direction) {
        return new ChemicalLogicConnectorSettings(direction);
    }

    @Nonnull
    @Override
    public IChannelSettings createChannel() {
        return new ChemicalLogicChannelSettings();
    }
}
