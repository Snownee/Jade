package snownee.jade.impl.theme;

import java.lang.reflect.Type;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import snownee.jade.api.theme.Theme;
import snownee.jade.util.Color;

public class ThemeSerializer implements JsonDeserializer<Theme>, JsonSerializer<Theme> {

	@Override
	public JsonElement serialize(Theme src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		o.addProperty("id", src.id.toString());
		JsonArray array;
		if (src.backgroundTexture != null) {
			array = new JsonArray();
			array.add(new JsonPrimitive(src.backgroundTexture.toString()));
			for (int i : src.backgroundTextureUV) {
				array.add(i);
			}
			o.add("backgroundImage", array);
		} else {
			if (src.backgroundColor != -1) {
				o.addProperty("backgroundColor", Color.rgb(src.backgroundColor).getHex());
			}
			array = new JsonArray();
			for (int i = 0; i < 4; i++) {
				array.add(Color.rgb(src.borderColor[i]).getHex());
			}
			o.add("borderColor", array);
		}
		o.addProperty("titleColor", Color.rgb(src.titleColor).getHex());
		o.addProperty("normalColor", Color.rgb(src.normalColor).getHex());
		o.addProperty("infoColor", Color.rgb(src.infoColor).getHex());
		o.addProperty("successColor", Color.rgb(src.successColor).getHex());
		o.addProperty("warningColor", Color.rgb(src.warningColor).getHex());
		o.addProperty("dangerColor", Color.rgb(src.dangerColor).getHex());
		o.addProperty("failureColor", Color.rgb(src.failureColor).getHex());
		o.addProperty("boxBorderColor", Color.rgb(src.boxBorderColor).getHex());
		o.addProperty("bottomProgressNormalColor", Color.rgb(src.bottomProgressNormalColor).getHex());
		o.addProperty("bottomProgressFailureColor", Color.rgb(src.bottomProgressFailureColor).getHex());
		if (!src.textShadow)
			o.addProperty("textShadow", false);
		array = new JsonArray();
		for (int i = 0; i < 4; i++) {
			array.add(src.padding[i]);
		}
		o.add("padding", array);
		if (src.squareBorder != null)
			o.addProperty("squareBorder", src.squareBorder);
		if (src.opacity != 0)
			o.addProperty("opacity", src.opacity);
		if (src.bottomProgressOffset != null) {
			array = new JsonArray();
			for (int i = 0; i < 4; i++) {
				array.add(src.bottomProgressOffset[i]);
			}
			o.add("bottomProgressOffset", array);
		}
		if (src.lightColorScheme)
			o.addProperty("lightColorScheme", true);
		return o;
	}

	@Override
	public Theme deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		if (GsonHelper.getAsInt(o, "version", 0) != 0) {
			throw new JsonParseException("Unsupported theme version");
		}
		Theme theme = new Theme();
		if (o.has("backgroundImage")) {
			JsonArray array = o.getAsJsonArray("backgroundImage");
			Preconditions.checkArgument(array.size() == 9, "backgroundImage must have 9 elements");
			theme.backgroundTexture = new ResourceLocation(array.get(0).getAsString());
			theme.backgroundTextureUV = new int[8];
			for (int i = 0; i < 8; i++) {
				theme.backgroundTextureUV[i] = array.get(i + 1).getAsInt();
			}
		} else {
			theme.backgroundColor = readColor(o.get("backgroundColor"), theme.backgroundColor);
			JsonArray array = o.get("borderColor").getAsJsonArray();
			Preconditions.checkArgument(array.size() == 4, "borderColor must have 4 elements");
			for (int i = 0; i < 4; i++) {
				theme.borderColor[i] = readColor(array.get(i), theme.borderColor[i]);
			}
		}
		theme.titleColor = readColor(o.get("titleColor"), theme.titleColor);
		theme.normalColor = readColor(o.get("normalColor"), theme.normalColor);
		theme.infoColor = readColor(o.get("infoColor"), theme.infoColor);
		theme.successColor = readColor(o.get("successColor"), theme.successColor);
		theme.warningColor = readColor(o.get("warningColor"), theme.warningColor);
		theme.dangerColor = readColor(o.get("dangerColor"), theme.dangerColor);
		theme.failureColor = readColor(o.get("failureColor"), theme.failureColor);
		theme.boxBorderColor = readColor(o.get("boxBorderColor"), theme.boxBorderColor);
		theme.bottomProgressNormalColor = readColor(o.get("bottomProgressNormalColor"), theme.bottomProgressNormalColor);
		theme.bottomProgressFailureColor = readColor(o.get("bottomProgressFailureColor"), theme.bottomProgressFailureColor);
		theme.textShadow = GsonHelper.getAsBoolean(o, "textShadow", true);
		if (o.has("padding")) {
			JsonArray array = o.getAsJsonArray("padding");
			Preconditions.checkArgument(array.size() == 4, "padding must have 4 elements");
			for (int i = 0; i < 4; i++) {
				theme.padding[i] = array.get(i).getAsInt();
			}
		}
		if (o.has("squareBorder"))
			theme.squareBorder = o.get("squareBorder").getAsBoolean();
		theme.opacity = GsonHelper.getAsFloat(o, "opacity", 0);
		if (o.has("bottomProgressOffset")) {
			JsonArray array = o.getAsJsonArray("bottomProgressOffset");
			Preconditions.checkArgument(array.size() == 4, "bottomProgressOffset must have 4 elements");
			theme.bottomProgressOffset = new int[4];
			for (int i = 0; i < 4; i++) {
				theme.bottomProgressOffset[i] = array.get(i).getAsInt();
			}
		}
		theme.lightColorScheme = GsonHelper.getAsBoolean(o, "lightColorScheme", false);
		return theme;
	}

	private int readColor(JsonElement el, int fallback) {
		if (el == null || el.isJsonNull())
			return fallback;
		JsonPrimitive e = el.getAsJsonPrimitive();
		if (e.isString()) {
			try {
				return Color.valueOf(e.getAsString()).toInt();
			} catch (Throwable e2) {
				return 0;
			}
		} else {
			return e.getAsInt();
		}
	}

}
