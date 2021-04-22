package mcp.mobius.waila.api;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import mcp.mobius.waila.addons.core.PluginCore;
import mcp.mobius.waila.api.impl.WailaRegistrar;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.Constants;

public class RenderableTextComponent extends StringTextComponent {

	public RenderableTextComponent(ResourceLocation id, CompoundNBT data) {
		super(getRenderString(id, data));
	}

	public RenderableTextComponent(RenderableTextComponent... components) {
		this((ITextComponent[]) components);
	}

	public RenderableTextComponent(ITextComponent... components) {
		super(getRenderString(components));
	}

	public List<RenderContainer> getRenderers() {
		List<RenderContainer> renderers = Lists.newArrayList();
		CompoundNBT data = getData();
		if (data.contains("renders")) {
			ListNBT list = data.getList("renders", Constants.NBT.TAG_STRING);
			list.forEach(t -> {
				StringNBT stringTag = (StringNBT) t;
				try {
					CompoundNBT tag = JsonToNBT.getTagFromJson(stringTag.getString());
					ResourceLocation id = new ResourceLocation(tag.getString("id"));
					CompoundNBT dataTag = tag.getCompound("data");
					renderers.add(new RenderContainer(id, dataTag));
				} catch (CommandSyntaxException e) {
					// no-op
				}
			});
		} else {
			ResourceLocation id = new ResourceLocation(data.getString("id"));
			CompoundNBT dataTag = data.getCompound("data");
			renderers.add(new RenderContainer(id, dataTag));
		}

		return renderers;
	}

	private CompoundNBT getData() {
		try {
			return JsonToNBT.getTagFromJson(getString());
		} catch (CommandSyntaxException e) {
			return new CompoundNBT();
		}
	}

	@Override
	public IFormattableTextComponent appendSibling(ITextComponent sibling) {
		throw new UnsupportedOperationException();
	}

	private static String getRenderString(ResourceLocation id, CompoundNBT data) {
		CompoundNBT renderData = new CompoundNBT();
		renderData.putString("id", id.toString());
		renderData.put("data", data);
		return renderData.toString();
	}

	private static String getRenderString(ITextComponent... components) {
		CompoundNBT container = new CompoundNBT();
		ListNBT renderData = new ListNBT();
		for (ITextComponent component : components) {
			if (component instanceof RenderableTextComponent) {
				CompoundNBT data = ((RenderableTextComponent) component).getData();
				if (data.contains("renders")) {
					renderData.addAll(data.getList("renders", Constants.NBT.TAG_STRING));
				} else {
					renderData.add(StringNBT.valueOf(component.getString()));
				}
			} else {
				renderData.add(StringNBT.valueOf(getNormalString(component)));
			}
		}
		container.put("renders", renderData);
		return container.toString();
	}

	private static String getNormalString(ITextComponent component) {
		CompoundNBT data = new CompoundNBT();
		data.putString("text", ITextComponent.Serializer.toJson(component));
		CompoundNBT renderData = new CompoundNBT();
		renderData.putString("id", PluginCore.RENDER_TEXT.toString());
		renderData.put("data", data);
		return renderData.toString();
	}

	public static class RenderContainer {
		private final ResourceLocation id;
		private final CompoundNBT data;
		private final ITooltipRenderer renderer;

		public RenderContainer(ResourceLocation id, CompoundNBT data) {
			this.id = id;
			this.data = data;
			this.renderer = WailaRegistrar.INSTANCE.getTooltipRenderer(id);
		}

		public ResourceLocation getId() {
			return id;
		}

		public CompoundNBT getData() {
			return data;
		}

		public ITooltipRenderer getRenderer() {
			return renderer;
		}
	}
}
