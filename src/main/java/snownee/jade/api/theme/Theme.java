package snownee.jade.api.theme;

import java.util.Arrays;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSpriteManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.BoxElement;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
import snownee.jade.impl.Tooltip;
import snownee.jade.impl.ui.BoxElementImpl;

public class Theme {

	public static final ResourceLocation DEFAULT_THEME_ID = JadeIds.JADE("dark");
	public ResourceLocation id;
	public String styleName;
	public BoxStyle tooltipStyle;
	public BoxStyle nestedBoxStyle;
	public BoxStyle viewGroupStyle;
	public TextSetting text;
	public float changeOpacity;
	public boolean lightColorScheme;
	public ResourceLocation iconSlotSprite;
	public int iconSlotInflation;
	public BoxElement iconSlotSpriteCache;

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public Theme(
			String styleName,
			BoxStyle tooltipStyle,
			BoxStyle nestedBoxStyle,
			BoxStyle viewGroupStyle,
			TextSetting text,
			float changeOpacity,
			boolean lightColorScheme,
			Optional<ResourceLocation> iconSlotSprite,
			int iconSlotInflation) {
		this.styleName = styleName;
		this.tooltipStyle = tooltipStyle;
		this.nestedBoxStyle = nestedBoxStyle;
		this.viewGroupStyle = viewGroupStyle;
		this.text = text;
		this.changeOpacity = changeOpacity;
		this.lightColorScheme = lightColorScheme;
		this.iconSlotSprite = iconSlotSprite.orElse(null);
		this.iconSlotInflation = iconSlotInflation;
	}

	public ResourceLocation mainId() {
		if (id.getPath().contains("/")) {
			return id.withPath(id.getPath().substring(0, id.getPath().indexOf('/')));
		} else {
			return id;
		}
	}

	public String styleId() {
		if (id.getPath().contains("/")) {
			return id.getPath().substring(id.getPath().indexOf('/') + 1);
		} else {
			return "";
		}
	}

	public @Nullable Element modifyIcon(@Nullable Element icon) {
		if (icon == null) {
			return null;
		}

		IWailaConfig.Overlay overlay = IWailaConfig.get().overlay();
		if (!overlay.shouldShowIcon() || overlay.getIconMode() == IWailaConfig.IconMode.INLINE) {
			return null;
		}

		if (iconSlotSprite != null) {
			if (iconSlotSpriteCache == null) {
				GuiSpriteManager guiSprites = Minecraft.getInstance().getGuiSprites();
				TextureAtlasSprite textureAtlasSprite = guiSprites.getSprite(iconSlotSprite);
				GuiSpriteScaling scaling = guiSprites.getSpriteScaling(textureAtlasSprite);
				int[] padding = new int[4];
				Arrays.fill(padding, iconSlotInflation);
				if (scaling instanceof GuiSpriteScaling.NineSlice nineSlice) {
					GuiSpriteScaling.NineSlice.Border border = nineSlice.border();
					padding[0] += border.top();
					padding[1] += border.right();
					padding[2] += border.bottom();
					padding[3] += border.left();
				}
				iconSlotSpriteCache = new BoxElementImpl(new Tooltip(), BoxStyle.getSprite(iconSlotSprite, padding), null);
			}
			ITooltip tooltip1 = iconSlotSpriteCache.getTooltip();
			tooltip1.clear();
			tooltip1.add(icon);
			icon = iconSlotSpriteCache;
		}
		return icon.tag(JadeIds.CORE_ROOT_ICON);
	}
}
