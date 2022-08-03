package snownee.jade.api.ui;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.phys.Vec2;
import snownee.jade.impl.Tooltip;

@ApiStatus.NonExtendable
public interface ITooltipRenderer {

	float getPadding();

	Tooltip getTooltip();

	boolean hasIcon();

	IElement getIcon();

	void setIcon(IElement icon);

	Rect2i getPosition();

	Vec2 getSize();

	Vec2 getContentStart();

	void setContentStart(Vec2 contentStart);
}