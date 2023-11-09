package snownee.jade.util;

import java.lang.reflect.Type;
import java.util.function.Function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

public class CodecJsonSerializer<T> implements JsonSerializer<T>, JsonDeserializer<T> {

	private final Codec<T> codec;

	public CodecJsonSerializer(Codec<T> codec) {
		this.codec = codec;
	}

	@Override
	public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		return codec.parse(JsonOps.INSTANCE, json).result().orElseThrow(() -> new JsonParseException("Failed to parse json " + json));
	}

	@Override
	public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
		return codec.encodeStart(JsonOps.INSTANCE, src).result().orElseThrow(() -> new JsonParseException("Failed to serialize " + src));
	}
}
