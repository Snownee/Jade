package snownee.jade.gui.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Predicates;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import snownee.jade.Jade;
import snownee.jade.gui.BaseOptionsScreen;
import snownee.jade.gui.config.value.CycleOptionValue;
import snownee.jade.gui.config.value.InputOptionValue;
import snownee.jade.gui.config.value.OptionValue;
import snownee.jade.gui.config.value.SliderOptionValue;
import snownee.jade.util.ClientPlatformProxy;

public class WailaOptionsList extends ContainerObjectSelectionList<WailaOptionsList.Entry> {

	private BaseOptionsScreen owner;
	private final Runnable diskWriter;
	private double targetScroll;
	public int serverFeatures;

	public WailaOptionsList(BaseOptionsScreen owner, Minecraft client, int width, int height, int y0, int y1, int entryHeight, Runnable diskWriter) {
		super(client, width, height, y0, y1, entryHeight);
		this.owner = owner;
		this.diskWriter = diskWriter;
	}

	public WailaOptionsList(BaseOptionsScreen owner, Minecraft client, int width, int height, int y0, int y1, int entryHeight) {
		this(owner, client, width, height, y0, y1, entryHeight, null);
	}

	@Override
	public int getRowWidth() {
		return Math.min(width, 300);
	}

	@Override
	protected int getScrollbarPosition() {
		return width - 6;
	}

	@Override
	public void setScrollAmount(double d) {
		super.setScrollAmount(d);
		targetScroll = getScrollAmount();
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f) {
		double speed = !ClientPlatformProxy.hasFastScroll && Screen.hasControlDown() ? 4.5 : 1.5;
		targetScroll = this.getScrollAmount() - f * (double) this.itemHeight * speed;
		return true;
	}

	@Override
	public boolean isFocused() {
		return owner.getFocused() == this;
	}

	@Override
	protected boolean isSelectedItem(int i) {
		return Objects.equals(this.getSelected(), this.children().get(i));
	}

	@Override
	protected void renderSelection(PoseStack poseStack, int i, int j, int k, int l, int m) {
		AbstractSelectionList.fill(poseStack, 0, i - 2, owner.width, i + k + 2, 0x33FFFFFF);
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float delta) {
		super.setScrollAmount((targetScroll + super.getScrollAmount()) / 2);
		Entry entry = getEntryAtPosition(mouseX, mouseY);
		setSelected(entry);

		renderBackground(matrixStack);
		int scrollPosX = getScrollbarPosition();
		int j = scrollPosX + 6;
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();
		RenderSystem.setShaderTexture(0, BACKGROUND_LOCATION);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		//		int rowLeft = getRowLeft();
		//		int scrollJump = y0 + 4 - (int) getScrollAmount();

		renderList(matrixStack, mouseX, mouseY, delta);
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(519);
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferBuilder.vertex(x0, y0, -100.0D).uv(0.0F, y0 / 32.0F).color(64, 64, 64, 255).endVertex();
		bufferBuilder.vertex(x0 + width, y0, -100.0D).uv(width / 32.0F, y0 / 32.0F).color(64, 64, 64, 255).endVertex();
		bufferBuilder.vertex(x0 + width, 0.0D, -100.0D).uv(width / 32.0F, 0.0F).color(64, 64, 64, 255).endVertex();
		bufferBuilder.vertex(x0, 0.0D, -100.0D).uv(0.0F, 0.0F).color(64, 64, 64, 255).endVertex();
		bufferBuilder.vertex(x0, height, -100.0D).uv(0.0F, height / 32.0F).color(64, 64, 64, 255).endVertex();
		bufferBuilder.vertex(x0 + width, height, -100.0D).uv(width / 32.0F, height / 32.0F).color(64, 64, 64, 255).endVertex();
		bufferBuilder.vertex(x0 + width, y1, -100.0D).uv(width / 32.0F, y1 / 32.0F).color(64, 64, 64, 255).endVertex();
		bufferBuilder.vertex(x0, y1, -100.0D).uv(0.0F, y1 / 32.0F).color(64, 64, 64, 255).endVertex();
		BufferUploader.drawWithShader(bufferBuilder.end());
		RenderSystem.depthFunc(515);
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		bufferBuilder.vertex(x0, y0 + 4, 0.0D).color(0, 0, 0, 0).endVertex();
		bufferBuilder.vertex(x1, y0 + 4, 0.0D).color(0, 0, 0, 0).endVertex();
		bufferBuilder.vertex(x1, y0, 0.0D).color(0, 0, 0, 255).endVertex();
		bufferBuilder.vertex(x0, y0, 0.0D).color(0, 0, 0, 255).endVertex();
		bufferBuilder.vertex(x0, y1, 0.0D).color(0, 0, 0, 255).endVertex();
		bufferBuilder.vertex(x1, y1, 0.0D).color(0, 0, 0, 255).endVertex();
		bufferBuilder.vertex(x1, y1 - 4, 0.0D).color(0, 0, 0, 0).endVertex();
		bufferBuilder.vertex(x0, y1 - 4, 0.0D).color(0, 0, 0, 0).endVertex();
		tessellator.end();

		int int_8 = Math.max(0, getMaxPosition() - (y1 - y0 - 4));
		if (int_8 > 0) {
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			int int_9 = (int) ((float) ((y1 - y0) * (y1 - y0)) / (float) getMaxPosition());
			int_9 = Mth.clamp(int_9, 32, y1 - y0 - 8);
			int int_10 = (int) getScrollAmount() * (y1 - y0 - int_9) / int_8 + y0;
			if (int_10 < y0) {
				int_10 = y0;
			}

			bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
			bufferBuilder.vertex(scrollPosX, y1, 0.0D).color(0, 0, 0, 255).uv(0.0f, 1.0f).endVertex();
			bufferBuilder.vertex(j, y1, 0.0D).color(0, 0, 0, 255).uv(1.0f, 1.0f).endVertex();
			bufferBuilder.vertex(j, y0, 0.0D).color(0, 0, 0, 255).uv(1.0f, 0.0f).endVertex();
			bufferBuilder.vertex(scrollPosX, y0, 0.0D).color(0, 0, 0, 255).uv(0.0f, 0.0f).endVertex();
			bufferBuilder.vertex(scrollPosX, (int_10 + int_9), 0.0D).color(128, 128, 128, 255).uv(0.0f, 1.0f).endVertex();
			bufferBuilder.vertex(j, (int_10 + int_9), 0.0D).color(128, 128, 128, 255).uv(1.0f, 1.0f).endVertex();
			bufferBuilder.vertex(j, int_10, 0.0D).color(128, 128, 128, 255).uv(1.0f, 0.0f).endVertex();
			bufferBuilder.vertex(scrollPosX, int_10, 0.0D).color(128, 128, 128, 255).uv(0.0f, 0.0f).endVertex();
			bufferBuilder.vertex(scrollPosX, (int_10 + int_9 - 1), 0.0D).color(192, 192, 192, 255).uv(0.0f, 1.0f).endVertex();
			bufferBuilder.vertex(j - 1, int_10 + int_9 - 1, 0.0D).color(192, 192, 192, 255).uv(1.0f, 1.0f).endVertex();
			bufferBuilder.vertex(j - 1, int_10, 0.0D).color(192, 192, 192, 255).uv(1.0f, 0.0f).endVertex();
			bufferBuilder.vertex(scrollPosX, int_10, 0.0D).color(192, 192, 192, 255).uv(0.0f, 0.0f).endVertex();
			tessellator.end();
		}

		renderDecorations(matrixStack, mouseX, mouseY);
		RenderSystem.disableBlend();
	}

	public void save() {
		children().stream().filter(e -> e instanceof OptionValue).map(e -> (OptionValue<?>) e).forEach(OptionValue::save);
		if (diskWriter != null)
			diskWriter.run();
	}

	public <T extends Entry> T add(T entry) {
		addEntry(entry);
		return entry;
	}

	public MutableComponent title(String string) {
		return add(new Title(string)).getTitle();
	}

	public OptionValue<?> slider(String optionName, float value, Consumer<Float> setter) {
		return slider(optionName, value, setter, 0, 1, FloatUnaryOperator.identity());
	}

	public OptionValue<?> slider(String optionName, float value, Consumer<Float> setter, float min, float max, FloatUnaryOperator aligner) {
		return add(new SliderOptionValue(optionName, value, setter, min, max, aligner));
	}

	public <T> OptionValue<?> input(String optionName, T value, Consumer<T> setter, Predicate<String> validator) {
		return add(new InputOptionValue<>(this::updateSaveState, optionName, value, setter, validator));
	}

	public <T> OptionValue<?> input(String optionName, T value, Consumer<T> setter) {
		return input(optionName, value, setter, Predicates.alwaysTrue());
	}

	public OptionValue<?> choices(String optionName, boolean value, BooleanConsumer setter) {
		return choices(optionName, value, setter, null);
	}

	public OptionValue<?> choices(String optionName, boolean value, BooleanConsumer setter, @Nullable Consumer<CycleButton.Builder<Boolean>> builderConsumer) {
		CycleButton.Builder<Boolean> builder = CycleButton.onOffBuilder();
		if (builderConsumer != null) {
			builderConsumer.accept(builder);
		}
		return add(new CycleOptionValue<>(optionName, builder, value, setter));
	}

	public <T extends Enum<T>> OptionValue<?> choices(String optionName, T value, Consumer<T> setter) {
		return choices(optionName, value, setter, null);
	}

	public <T extends Enum<T>> OptionValue<?> choices(String optionName, T value, Consumer<T> setter, @Nullable Consumer<CycleButton.Builder<T>> builderConsumer) {
		List<T> values = (List<T>) Arrays.asList(value.getClass().getEnumConstants());
		CycleButton.Builder<T> builder = CycleButton.<T>builder(v -> Entry.makeTitle(optionName + "_" + v.name().toLowerCase(Locale.ENGLISH))).withValues(values);
		if (builderConsumer != null) {
			builderConsumer.accept(builder);
		}
		return add(new CycleOptionValue<>(optionName, builder, value, setter));
	}

	public <T> OptionValue<?> choices(String optionName, T value, List<T> values, Consumer<T> setter) {
		return add(new CycleOptionValue<>(optionName, CycleButton.<T>builder(v -> Component.literal(v.toString())).withValues(values), value, setter));
	}

	public void onClose() {
		clearEntries();
		owner = null;
	}

	public void updateSaveState() {
		for (Entry entry : children()) {
			if (entry instanceof OptionValue<?> value && !value.isValidValue()) {
				owner.saveButton.active = false;
				return;
			}
		}
		owner.saveButton.active = true;
	}

	public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {

		protected final Minecraft client;
		@Nullable
		protected String description;

		public static MutableComponent makeTitle(String key) {
			return Component.translatable(makeKey(key));
		}

		public static String makeKey(String key) {
			return Util.makeDescriptionId("config", new ResourceLocation(Jade.MODID, key));
		}

		public Entry() {
			client = Minecraft.getInstance();
		}

		public AbstractWidget getListener() {
			return null;
		}

		@Override
		public abstract List<? extends AbstractWidget> children();

		@Override
		public List<? extends NarratableEntry> narratables() {
			return children();
		}

		@Override
		public abstract void render(PoseStack matrixStack, int index, int rowTop, int rowLeft, int width, int height, int mouseX, int mouseY, boolean hovered, float deltaTime);

		public void setDisabled(boolean b) {
			if (getListener() != null) {
				getListener().active = !b;
			}
		}

		@Nullable
		public String getDescription() {
			return description;
		}

		public int getTextX(int width) {
			return 0;
		}

		public int getTextWidth() {
			return 0;
		}

	}

	public static class Title extends Entry {

		private final MutableComponent title;
		private int x;

		public Title(String key) {
			title = makeTitle(key);
			key = makeKey(key + "_desc");
			if (I18n.exists(key))
				description = I18n.get(key);
		}

		public MutableComponent getTitle() {
			return title;
		}

		@Override
		public void render(PoseStack matrixStack, int index, int rowTop, int rowLeft, int width, int height, int mouseX, int mouseY, boolean hovered, float deltaTime) {
			x = rowLeft;
			client.font.drawShadow(matrixStack, title, getTextX(width), rowTop + (height / 4) + (client.font.lineHeight / 2), 16777215);
		}

		@Override
		public List<? extends AbstractWidget> children() {
			return Collections.EMPTY_LIST;
		}

		@Override
		public int getTextX(int width) {
			return x + (width - client.font.width(title)) / 2;
		}

		@Override
		public int getTextWidth() {
			return client.font.width(title);
		}

	}

}
