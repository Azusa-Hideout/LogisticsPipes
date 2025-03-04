package logisticspipes.modules;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import logisticspipes.interfaces.IPipeServiceProvider;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.connection.LPNeighborTileEntityKt;
import network.rs485.logisticspipes.SatellitePipe;
import network.rs485.logisticspipes.property.StringProperty;
import network.rs485.logisticspipes.property.Property;

public class ModuleSatellite extends LogisticsModule {

	private final SinkReply _sinkReply = new SinkReply(FixedPriority.ItemSink, 0, true, false, 1, 0, null);

	public final StringProperty satellitePipeName = new StringProperty("", "satellitePipeName");
	private final List<Property<?>> properties = ImmutableList.<Property<?>>builder()
		.add(satellitePipeName)
		.build();

	@Nonnull
	@Override
	public String getLPName() {
		throw new RuntimeException("Cannot get LP name for " + this);
	}

	@NotNull
	@Override
	public List<Property<?>> getProperties() {
		return properties;
	}

	@Override
	public SinkReply sinksItem(@Nonnull ItemStack stack, ItemIdentifier item, int bestPriority, int bestCustomPriority,
			boolean allowDefault, boolean includeInTransit, boolean forcePassive) {
		if (bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal()
				&& bestCustomPriority >= _sinkReply.customPriority)) {
			return null;
		}
		final int itemCount = spaceFor(stack, item, includeInTransit);
		if (itemCount > 0) {
			return new SinkReply(_sinkReply, itemCount);
		} else {
			return null;
		}
	}

	private int spaceFor(@Nonnull ItemStack stack, ItemIdentifier item, boolean includeInTransit) {
		final IPipeServiceProvider service = Objects.requireNonNull(_service);
		int count = service.getAvailableAdjacent().inventories().stream()
				.map(neighbor -> LPNeighborTileEntityKt.sneakyInsertion(neighbor).from(getUpgradeManager()))
				.map(LPNeighborTileEntityKt::getInventoryUtil)
				.filter(Objects::nonNull)
				.map(util -> util.roomForItem(stack))
				.reduce(Integer::sum).orElse(0);
		if (includeInTransit) {
			count -= service.countOnRoute(item);
		}
		return count;
	}

	@Override
	public void tick() {}

	@Override
	public boolean hasGenericInterests() {
		return false;
	}

	@Override
	public boolean interestedInAttachedInventory() {
		return false;
		// when we are default we are interested in everything anyway, otherwise we're only interested in our filter.
	}

	@Override
	public boolean interestedInUndamagedID() {
		return false;
	}

	@Override
	public boolean receivePassive() {
		return false;
	}

	@Override
	public void readFromNBT(@NotNull NBTTagCompound tag) {
		super.readFromNBT(tag);

		// FIXME: remove after 1.12
		if (tag.hasKey("satelliteId"))
			satellitePipeName.setValue(tag.getString("satelliteId"));

		if (MainProxy.isServer(getWorld()) && _service != null) {
			((SatellitePipe) _service).ensureAllSatelliteStatus();
		}
	}

}
