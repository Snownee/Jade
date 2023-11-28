package snownee.jade.addon.vanilla;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import snownee.jade.api.Accessor;
import snownee.jade.api.Identifiers;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ItemView;
import snownee.jade.api.view.ViewGroup;

public enum CampfireProvider implements IServerExtensionProvider<Object, ItemStack>, IClientExtensionProvider<ItemStack, ItemView> {

	INSTANCE;

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_CAMPFIRE;
	}

	@Override
	public List<ClientViewGroup<ItemView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<ItemStack>> groups) {
		return ClientViewGroup.map(groups, stack -> {
			String text = null;
			if (stack.getTag() != null && stack.getTag().contains("jade:cooking")) {
				text = IThemeHelper.get().seconds(stack.getTag().getInt("jade:cooking")).getString();
			}
			return new ItemView(stack).amountText(text);
		}, null);
	}

	@Override
	public @Nullable List<ViewGroup<ItemStack>> getGroups(Accessor<?> accessor, Object target) {
		if (target instanceof CampfireBlockEntity campfire) {
			List<ItemStack> list = Lists.newArrayList();
			for (int i = 0; i < campfire.cookingTime.length; i++) {
				ItemStack stack = campfire.getItems().get(i);
				if (stack.isEmpty()) {
					continue;
				}
				stack = stack.copy();
				stack.getOrCreateTag().putInt("jade:cooking", campfire.cookingTime[i] - campfire.cookingProgress[i]);
				list.add(stack);
			}
			return List.of(new ViewGroup<>(list));
		}
		return null;
	}
}
