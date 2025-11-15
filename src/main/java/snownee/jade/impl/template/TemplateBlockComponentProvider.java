package snownee.jade.impl.template;

import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.TooltipPosition;

public final class TemplateBlockComponentProvider extends TemplateComponentProvider<BlockAccessor> implements IBlockComponentProvider {
	public TemplateBlockComponentProvider(Identifier uid) {
		this(uid, false, true, TooltipPosition.BODY);
	}

	public TemplateBlockComponentProvider(Identifier uid, boolean required, boolean enabledByDefault, int defaultPriority) {
		super(uid, required, enabledByDefault, defaultPriority);
	}
}
