package snownee.jade.api.view;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.DynamicOps;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.material.Fluids;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.FluidTextHelper;

public class FluidView {

	public static final Component EMPTY_FLUID = Component.translatable("jade.fluid.empty");

	public IElement overlay;
	public String current;
	public String max;
	public float ratio;
	@Nullable
	public Component fluidName;
	@Nullable
	public Component overrideText;
	/**
	 * Potion contents carried by the fluid, or {@code null} if it has no potion effects.
	 * Extracted from the fluid's {@link DataComponents#POTION_CONTENTS} component;
	 * renderers decide whether and how to display it.
	 */
	@Nullable
	public PotionContents potionContents;

	public FluidView(IElement overlay) {
		this.overlay = overlay;
		Objects.requireNonNull(overlay);
	}

	@Deprecated
	@Nullable
	public static FluidView readDefault(CompoundTag tag) {
		return readDefault(tag, NbtOps.INSTANCE);
	}

	@Nullable
	public static FluidView readDefault(CompoundTag tag, DynamicOps<Tag> ops) {
		long capacity = tag.getLong("capacity");
		if (capacity <= 0) {
			return null;
		}
		JadeFluidObject fluidObject = JadeFluidObject.CODEC.parse(ops, tag.get("fluid")).result().orElse(null);
		if (fluidObject == null) {
			return null;
		}
		long amount = fluidObject.getAmount();
		FluidView fluidView = new FluidView(IElementHelper.get().fluid(fluidObject));
		fluidView.fluidName = CommonProxy.getFluidName(fluidObject);
		// DataComponentPatch.get returns a nullable Optional: null when the patch has no
		// entry for the type at all, otherwise an Optional holding (or explicitly removing) it.
		var potionContents = fluidObject.getComponents().get(DataComponents.POTION_CONTENTS);
		fluidView.potionContents = potionContents != null
				? potionContents.filter(PotionContents::hasEffects).orElse(null)
				: null;
		fluidView.current = FluidTextHelper.getUnicodeMillibuckets(amount, true);
		fluidView.max = FluidTextHelper.getUnicodeMillibuckets(capacity, true);
		fluidView.ratio = (float) ((double) amount / capacity);
		if (fluidObject.getType().isSame(Fluids.EMPTY)) {
			fluidView.overrideText = Component.translatable(
					"jade.fluid",
					EMPTY_FLUID,
					Component.literal(fluidView.max).withStyle(ChatFormatting.GRAY));
		}
		return fluidView;
	}

	@Deprecated
	public static CompoundTag writeDefault(JadeFluidObject fluidObject, long capacity) {
		return writeDefault(fluidObject, capacity, NbtOps.INSTANCE);
	}

	public static CompoundTag writeDefault(JadeFluidObject fluidObject, long capacity, DynamicOps<Tag> ops) {
		CompoundTag tag = new CompoundTag();
		if (capacity <= 0) {
			return tag;
		}
		tag.put("fluid", JadeFluidObject.CODEC.encodeStart(ops, fluidObject).result().orElseThrow());
		tag.putLong("capacity", capacity);
		return tag;
	}

}
