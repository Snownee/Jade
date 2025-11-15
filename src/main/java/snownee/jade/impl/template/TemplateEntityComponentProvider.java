package snownee.jade.impl.template;

import net.minecraft.resources.Identifier;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.TooltipPosition;

public final class TemplateEntityComponentProvider extends TemplateComponentProvider<EntityAccessor> implements IEntityComponentProvider {
	public TemplateEntityComponentProvider(Identifier uid) {
		this(uid, false, true, TooltipPosition.BODY);
	}

	public TemplateEntityComponentProvider(Identifier uid, boolean required, boolean enabledByDefault, int defaultPriority) {
		super(uid, required, enabledByDefault, defaultPriority);
	}
}
