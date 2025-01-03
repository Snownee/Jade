package snownee.jade.addon.vanilla;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.item.ItemStack;
import snownee.jade.JadeClient;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.ITextElement;

public enum PetArmorProvider implements IEntityComponentProvider, StreamServerDataProvider<EntityAccessor, ItemStack> {
	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!(accessor.getEntity() instanceof OwnableEntity)) {
			return;
		}
		Mode mode = IWailaConfig.get().plugin().getEnum(JadeIds.MC_PET_ARMOR);
		if (mode == Mode.OFF) {
			return;
		}
		ItemStack armor = decodeFromData(accessor).orElse(ItemStack.EMPTY);
		if (armor.isEmpty()) {
			return;
		}
		if (mode == Mode.SHOW_DAMAGEABLE && !armor.isDamageableItem()) {
			return;
		}
		IElementHelper helper = IElementHelper.get();
		tooltip.add(helper.smallItem(armor));
		ITextElement text = helper.text(armor.getHoverName());
		if (armor.isDamageableItem()) {
			text.message(JadeClient.format(
					"narration.jade.item_durability",
					armor.getHoverName(),
					armor.getMaxDamage() - armor.getDamageValue()).getString());
		}
		tooltip.append(text);
	}

	@Override
	public boolean shouldRequestData(EntityAccessor accessor) {
		if (!(accessor.getEntity() instanceof OwnableEntity)) {
			return false;
		}
		Mode mode = IWailaConfig.get().plugin().getEnum(JadeIds.MC_PET_ARMOR);
		return mode != Mode.OFF;
	}

	@Override
	public @Nullable ItemStack streamData(EntityAccessor accessor) {
		ItemStack armor = ((Mob) accessor.getEntity()).getBodyArmorItem();
		return armor.isEmpty() ? null : armor;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, ItemStack> streamCodec() {
		return ItemStack.OPTIONAL_STREAM_CODEC;
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_PET_ARMOR;
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	public enum Mode {
		OFF, SHOW_ALL, SHOW_DAMAGEABLE
	}
}
