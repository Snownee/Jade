package snownee.jade.api.ui;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.phys.Vec2;
import snownee.jade.impl.Tooltip;

@ApiStatus.NonExtendable
public interface ITooltipRenderer {

	static final int TOP = 0;
	static final int RIGHT = 1;
	static final int BOTTOM = 2;
	static final int LEFT = 3;

	float getPadding(int i);

	void setPadding(int i, float value);

	Tooltip getTooltip();

	boolean hasIcon();

	IElement getIcon();

	void setIcon(IElement icon);

	Rect2i getPosition();

	Vec2 getSize();

	void setSize(Vec2 size);

	void recalculateSize();

	float getRealScale();

	@Nullable
	Rect2i getRealRect();

	void recalculateRealRect();
}