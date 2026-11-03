package snownee.jade.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.util.ModIdentification;

@Mixin(CreativeModeInventoryScreen.class)
public class CreativeModeInventoryScreenMixin {
	@Unique
	private static final String TOOLTIP_LAMBDA = "lambda$new$0(Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/client/gui/components/Tooltip;";

	@WrapOperation(
			method = TOOLTIP_LAMBDA,
			at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V"))
	private static void jade$collectTabNames(
			List<Component> list,
			int index,
			Object element,
			Operation<Void> original,
			@Share("index") LocalIntRef insertionIndex,
			@Share("names") LocalRef<@Nullable List<String>> tabNames) {
		original.call(list, index, element);
		if (!(element instanceof Component component) || !IWailaConfig.get().general().showItemModNameTooltip()) {
			return;
		}
		insertionIndex.set(index + 1);
		String tabName = component.getString().toLowerCase(Locale.ENGLISH);
		List<String> tabNamesValue = tabNames.get();
		if (tabNamesValue == null) {
			tabNames.set(Lists.newArrayList(tabName));
		} else {
			tabNamesValue.add(tabName);
		}
	}

	@WrapOperation(
			method = TOOLTIP_LAMBDA,
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/components/Tooltip;createForItem(Ljava/util/List;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/gui/components/Tooltip;"))
	private static Tooltip jade$addModName(
			List<Component> lines,
			ItemStack item,
			Operation<Tooltip> original,
			@Share("index") LocalIntRef insertionIndex,
			@Share("names") LocalRef<@Nullable List<String>> tabNames) {
		if (!IWailaConfig.get().general().showItemModNameTooltip() || item.isEmpty() || lines.size() <= 1) {
			return original.call(lines, item);
		}
		int index = Math.max(insertionIndex.get(), 1);
		if (index >= lines.size()) {
			return original.call(lines, item);
		}
		String modName = ModIdentification.getModId(item);
		modName = ModIdentification.getModFullName(modName).orElse(modName);
		List<String> tabNamesValue = tabNames.get();
		if (tabNamesValue != null) {
			String modNameLower = modName.toLowerCase(Locale.ENGLISH);
			for (String tabName : tabNamesValue) {
				if (tabName.startsWith(modNameLower)) {
					return original.call(lines, item);
				}
			}
		}
		List<Component> newLines = new ArrayList<>(lines);
		newLines.add(index, Component.literal(modName).withStyle(IWailaConfig.get().formatting().getItemModNameStyle()));
		return original.call(newLines, item);
	}
}