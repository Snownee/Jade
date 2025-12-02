package snownee.jade.api.view;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import snownee.jade.api.ui.IDisplayHelper;

public class EnergyView {

	public String current;
	public String max;
	public float ratio;
	public @Nullable Component overrideText;

	public EnergyView(String current, String max) {
		this.current = Objects.requireNonNull(current);
		this.max = Objects.requireNonNull(max);
	}

	@Nullable
	public static EnergyView read(Data data, String unit) {
		if (data.capacity <= 0) {
			return null;
		}
		String current = IDisplayHelper.get().humanReadableNumber(data.current, unit, false);
		String max = IDisplayHelper.get().humanReadableNumber(data.capacity, unit, false);
		EnergyView energyView = new EnergyView(current, max);
		energyView.ratio = (float) data.current / data.capacity;
		return energyView;
	}

	public record Data(long current, long capacity) {
		public static final StreamCodec<ByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.LONG,
				Data::current,
				ByteBufCodecs.LONG,
				Data::capacity,
				Data::new);
	}

}
