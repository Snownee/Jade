package snownee.jade.util;

import java.util.Collections;
import java.util.List;

import mcp.mobius.waila.api.RenderableTextComponent;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public class HackyRenderableTextComponent extends RenderableTextComponent {

	private final ResourceLocation id;
	private final CompoundNBT data;

	public HackyRenderableTextComponent(ResourceLocation id, CompoundNBT data) {
		super(id, data);
		this.id = id;
		this.data = data;
	}

	@Override
	public List<RenderContainer> getRenderers() {
		return Collections.singletonList(new RenderContainer(id, data));
	}

}
