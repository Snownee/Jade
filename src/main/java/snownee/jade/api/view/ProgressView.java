package snownee.jade.api.view;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.ui.MessageType;
import snownee.jade.api.ui.ProgressStyle;

public class ProgressView {

	public List<Part> parts = List.of();
	public final ProgressStyle style;
	public final BoxStyle boxStyle;
	public @Nullable Component text;

	public ProgressView(ProgressStyle style, BoxStyle boxStyle) {
		this.style = Objects.requireNonNull(style);
		this.boxStyle = Objects.requireNonNull(boxStyle);
	}

	public ProgressView(ProgressView.Part progress, @Nullable Component text, ProgressStyle style, BoxStyle boxStyle) {
		this(List.of(progress), text, style, boxStyle);
	}

	public ProgressView(List<ProgressView.Part> progress, @Nullable Component text, ProgressStyle style, BoxStyle boxStyle) {
		this(style, boxStyle);
		this.parts = Objects.requireNonNull(progress);
		this.text = text;
	}

	public static ProgressView read(Data data) {
//		ProgressView progressView = new ProgressView(new SlimProgressStyle());
//		progressView.progress = data.progress;
//		return progressView;
		return new ProgressView(JadeUI.progressStyle(), BoxStyle.nestedBox());//TODO
	}

	public record Data(float progress) {
		public static final StreamCodec<ByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.FLOAT,
				Data::progress,
				Data::new);
	}

	public record Part(float progress, @Nullable Element overlay, @Nullable MessageType messageType, int color) {
		public Part(float progress, @Nullable Element overlay, @Nullable MessageType messageType, int color) {
			this.progress = progress;
			this.overlay = overlay;
			this.messageType = messageType;
			this.color = color;
			Preconditions.checkArgument(progress >= 0 && progress <= 1, "Progress must be between 0 and 1, got: %s", progress);
		}

		public static Part of(float progress) {
			return of(progress, MessageType.NORMAL);
		}

		public static Part of(float progress, MessageType messageType) {
			return new Part(progress, null, messageType, -1);
		}

		public static Part of(float progress, Element overlay) {
			return new Part(progress, overlay, null, -1);
		}

		public static Part of(float progress, int color) {
			return new Part(progress, null, null, color);
		}
	}
}
