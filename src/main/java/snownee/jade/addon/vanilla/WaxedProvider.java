package snownee.jade.addon.vanilla;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.animal.coppergolem.CopperGolem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import snownee.jade.addon.access.AccessibilityPlugin;
import snownee.jade.addon.core.ObjectNameProvider;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IJadeProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.impl.ui.CompoundElement;

public class WaxedProvider implements IJadeProvider {
	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_WAXED;
	}

	@Override
	public int getDefaultPriority() {
		return ObjectNameProvider.BlockData.INSTANCE.getDefaultPriority() + 10;
	}

	public static class BlockComponent extends WaxedProvider implements IBlockComponentProvider {
		public static final BlockComponent INSTANCE = new BlockComponent();

		public static Element waxedIcon(Element icon, boolean waxed) {
			if (waxed) {
				return new CompoundElement(icon, JadeUI.item(Items.HONEYCOMB.getDefaultInstance(), 0.5f));
			} else {
				return icon;
			}
		}

		@Override
		public @Nullable Element getIcon(BlockAccessor accessor, IPluginConfig config, Element currentIcon) {
			ItemStack pickedResult = accessor.getPickedResult();
			if (!pickedResult.isEmpty() && accessor.getBlockEntity() instanceof SignBlockEntity sign) {
				return waxedIcon(JadeUI.item(pickedResult), sign.isWaxed());
			}
			return currentIcon;
		}

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			if (IWailaConfig.get().accessibility().getEnableAccessibilityPlugin() &&
					config.get(JadeIds.ACCESS_BLOCK_DETAILS) &&
					accessor.getBlockEntity() instanceof SignBlockEntity sign &&
					sign.isWaxed()) {
				String objectName = tooltip.getString(JadeIds.CORE_OBJECT_NAME);
				AccessibilityPlugin.replaceTitle(tooltip, objectName, "waxed");
			}
		}
	}

	public static class EntityComponent extends WaxedProvider implements IEntityComponentProvider {
		public static final EntityComponent INSTANCE = new EntityComponent();
		private static final Map<WeatheringCopper.WeatherState, Block> COPPER_GOLEM_STATUES = Map.of(
				WeatheringCopper.WeatherState.UNAFFECTED, Blocks.COPPER_GOLEM_STATUE,
				WeatheringCopper.WeatherState.EXPOSED, Blocks.EXPOSED_COPPER_GOLEM_STATUE,
				WeatheringCopper.WeatherState.WEATHERED, Blocks.WEATHERED_COPPER_GOLEM_STATUE,
				WeatheringCopper.WeatherState.OXIDIZED, Blocks.OXIDIZED_COPPER_GOLEM_STATUE
		);

		@Override
		public Element getIcon(EntityAccessor accessor, IPluginConfig config, Element currentIcon) {
			CopperGolem golem = (CopperGolem) accessor.getEntity();
			WeatheringCopper.WeatherState state = golem.getWeatherState();
			Block statueBlock = COPPER_GOLEM_STATUES.get(state);
			boolean waxed = EntityData.INSTANCE.decodeFromData(accessor).isPresent();
			if (waxed) {
				statueBlock = HoneycombItem.getWaxed(statueBlock.defaultBlockState()).orElse(statueBlock.defaultBlockState()).getBlock();
			}
			return BlockComponent.waxedIcon(JadeUI.item(new ItemStack(statueBlock)), waxed);
		}

		@Override
		public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
			if (IWailaConfig.get().accessibility().getEnableAccessibilityPlugin() &&
					config.get(JadeIds.ACCESS_ENTITY_DETAILS)) {
				boolean waxed = EntityData.INSTANCE.decodeFromData(accessor).isPresent();
				if (waxed) {
					String objectName = tooltip.getString(JadeIds.CORE_OBJECT_NAME);
					AccessibilityPlugin.replaceTitle(tooltip, objectName, "waxed");
				}
			}
		}
	}

	public static class EntityData extends WaxedProvider implements StreamServerDataProvider<EntityAccessor, Unit> {
		public static final EntityData INSTANCE = new EntityData();

		@Override
		public @Nullable Unit streamData(EntityAccessor accessor) {
			CopperGolem golem = (CopperGolem) accessor.getEntity();
			return golem.nextWeatheringTick == CopperGolem.IGNORE_WEATHERING_TICK ? Unit.INSTANCE : null;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, Unit> streamCodec() {
			return Unit.STREAM_CODEC.cast();
		}
	}
}
