package snownee.jade.mixin;

import java.util.List;
import java.util.Locale;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.ModIdentification;

@Mixin(CreativeModeInventoryScreen.class)
public class CreativeModeInventoryScreenMixin {
	@Inject(
			method = "getTooltipFromContainerItem", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/CreativeModeTabs;tabs()Ljava/util/List;"))
	private void jade$initTabNames(
			ItemStack itemStack,
			CallbackInfoReturnable<List<Component>> cir,
			@Local int i,
			@Share("index") LocalIntRef index) {
		index.set(i);
	}

	@WrapOperation(method = "getTooltipFromContainerItem", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V"))
	private void jade$collectTabNames(
			List<Component> instance,
			int i,
			Object e,
			Operation<Void> original,
			@Share("index") LocalIntRef index,
			@Share("names") LocalRef<List<String>> tabNames) {
		original.call(instance, i, e);
		if (!IWailaConfig.get().general().showItemModNameTooltip()) {
			return;
		}
		index.set(i + 1);
		String tabName = ((Component) e).getString().toLowerCase(Locale.ENGLISH);
		if (tabNames.get() == null) {
			tabNames.set(Lists.newArrayList(tabName));
		} else {
			tabNames.get().add(tabName);
		}
	}

	@WrapMethod(method = "getTooltipFromContainerItem")
	private List<Component> jade$addModName(
			ItemStack itemStack,
			Operation<List<Component>> original,
			@Share("index") LocalIntRef index,
			@Share("names") LocalRef<List<String>> tabNames) {
		List<Component> tooltip = original.call(itemStack);
		int i = index.get();
		if (IWailaConfig.get().general().showItemModNameTooltip() && i > 0 && i < tooltip.size()) {
			String modName = CommonProxy.getModIdFromItem(itemStack);
			modName = ModIdentification.getModFullName(modName).orElse(modName);
			if (tabNames.get() != null) {
				String modNameLower = modName.toLowerCase(Locale.ENGLISH);
				for (String tabName : tabNames.get()) {
					if (tabName.startsWith(modNameLower)) {
						return tooltip;
					}
				}
			}
			tooltip.add(i, Component.literal(modName).withStyle(IWailaConfig.get().formatting().getItemModNameStyle()));
		}
		return tooltip;
	}
}
