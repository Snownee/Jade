package snownee.jade.api.view;

import org.jetbrains.annotations.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import snownee.jade.api.ui.IDisplayHelper;

public class EnergyView {

	public String current;
	public String max;
	public float ratio;
	@Nullable
	public Component overrideText;

	@Nullable
	public static EnergyView read(Data data, String unit) {
		if (data.capacity <= 0) {
			return null;
		}
		EnergyView energyView = new EnergyView();
		energyView.current = IDisplayHelper.get().humanReadableNumber(data.current, unit, false);
		energyView.max = IDisplayHelper.get().humanReadableNumber(data.capacity, unit, false);
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
