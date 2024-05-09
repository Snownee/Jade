package snownee.jade.impl.template;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.TooltipPosition;

public final class TemplateBlockComponentProvider extends TemplateComponentProvider<BlockAccessor> implements IBlockComponentProvider {
	public TemplateBlockComponentProvider(ResourceLocation uid) {
		this(uid, false, true, TooltipPosition.BODY);
	}

	public TemplateBlockComponentProvider(ResourceLocation uid, boolean required, boolean enabledByDefault, int defaultPriority) {
		super(uid, required, enabledByDefault, defaultPriority);
	}
}
