package snownee.jade.api.view;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import snownee.jade.api.ui.ProgressStyle;
import snownee.jade.impl.ui.SlimProgressStyle;

public class ProgressView {

	public ProgressStyle style;
	public float progress;
	@Nullable
	public Component text;

	public ProgressView(ProgressStyle style) {
		this.style = style;
		Objects.requireNonNull(style);
	}

	public static ProgressView read(Data data) {
		ProgressView progressView = new ProgressView(new SlimProgressStyle());
		progressView.progress = data.progress;
		return progressView;
	}

	public record Data(float progress) {
		public static final StreamCodec<ByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.FLOAT,
				Data::progress,
				Data::new);
	}

}
