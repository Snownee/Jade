package snownee.jade.api.view;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import snownee.jade.api.theme.IThemeHelper;
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
		ProgressView view = new ProgressView(JadeUI.progressStyle(), BoxStyle.nestedBox());
		if (data.progress > 0) {
			view.parts = List.of(Part.of(data.progress, data.messageType));
		}
		return view;
	}

	public record Data(float progress, MessageType messageType) {
		public static final StreamCodec<ByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.FLOAT,
				Data::progress,
				MessageType.STREAM_CODEC,
				Data::messageType,
				Data::new);

		public Data(float progress) {
			this(progress, MessageType.INFO);
		}
	}

	public record Part(int id, float progress, @Nullable Element overlay, @Nullable MessageType messageType, int color) {
		public Part(int id, float progress, @Nullable Element overlay, @Nullable MessageType messageType, int color) {
			this.id = id;
			this.progress = progress;
			this.overlay = overlay;
			this.messageType = messageType;
			this.color = color;
			Preconditions.checkArgument(progress >= 0 && progress <= 1, "Progress must be between 0 and 1, got: %s", progress);
		}

		public static Part of(float progress) {
			return of(0, progress);
		}

		public static Part of(int id, float progress) {
			return of(id, progress, MessageType.INFO);
		}

		public static Part of(float progress, MessageType messageType) {
			return of(0, progress, messageType);
		}

		public static Part of(int id, float progress, MessageType messageType) {
			return new Part(id, progress, null, messageType, -1);
		}

		public static Part of(float progress, Element overlay) {
			return new Part(0, progress, overlay, null, -1);
		}

		public static Part of(float progress, int color) {
			return new Part(0, progress, null, null, color);
		}

		public int themeColor() {
			if (color != -1) {
				return color;
			}
			if (messageType != null) {
				return IThemeHelper.get().theme().progressColors.get(messageType);
			}
			return -1;
		}
	}
}
