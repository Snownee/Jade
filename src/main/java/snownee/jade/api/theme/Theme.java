package snownee.jade.api.theme;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.Identifiers;

public class Theme {

	public static final Theme DARK = new Theme();

	static {
		DARK.id = Identifiers.JADE("dark");
		DARK.backgroundColor = 0x131313;
	}

	public ResourceLocation id;
	public int backgroundColor = -1;
	public final int[] borderColor = { 0xFF383838, 0xFF383838, 0xFF242424, 0xFF242424 };
	public int titleColor = 0xFFFFFFFF;
	public int normalColor = 0xFFA0A0A0;
	public int infoColor = 0xFFFFFFFF;
	public int successColor = 0xFF55FF55;
	public int warningColor = 0xFFFFF3CD;
	public int dangerColor = 0xFFFF5555;
	public int failureColor = 0xFFAA0000;
	public int boxBorderColor = 0xFF808080;
	public boolean textShadow = true;
	public ResourceLocation backgroundTexture;
	public int[] backgroundTextureUV;
	public final int[] padding = new int[] { 4, 3, 1, 4 };
	public Boolean squareBorder;
	public float opacity;
	public int[] bottomProgressOffset;
	public int bottomProgressNormalColor = 0xFFFFFFFF;
	public int bottomProgressFailureColor = 0xFFFF4444;
	public boolean lightColorScheme;

}
