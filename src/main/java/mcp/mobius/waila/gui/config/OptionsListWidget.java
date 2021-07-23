package mcp.mobius.waila.gui.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import mcp.mobius.waila.Waila;
import mcp.mobius.waila.gui.OptionsScreen;
import mcp.mobius.waila.gui.config.value.CycleOptionValue;
import mcp.mobius.waila.gui.config.value.InputOptionValue;
import mcp.mobius.waila.gui.config.value.OptionValue;
import mcp.mobius.waila.gui.config.value.SliderOptionValue;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class OptionsListWidget extends AbstractSelectionList<OptionsListWidget.Entry> {

	private final OptionsScreen owner;
	private final Runnable diskWriter;

	public OptionsListWidget(OptionsScreen owner, Minecraft client, int x, int height, int width, int y, int entryHeight, Runnable diskWriter) {
		super(client, x, height, width, y, entryHeight);

		this.owner = owner;
		this.diskWriter = diskWriter;
		setRenderSelection(false);
	}

	public OptionsListWidget(OptionsScreen owner, Minecraft client, int x, int height, int width, int y, int entryHeight) {
		this(owner, client, x, height, width, y, entryHeight, null);
	}

	@Override
	public int getRowWidth() {
		return 250;
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float delta) {
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
		int rowLeft = getRowLeft();
		int scrollJump = y0 + 4 - (int) getScrollAmount();

		renderList(matrixStack, rowLeft, scrollJump, mouseX, mouseY, delta);
		RenderSystem.setShaderTexture(0, BACKGROUND_LOCATION);
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(519);
		bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
		bufferBuilder.vertex(x0, y0, -100.0D).color(64, 64, 64, 255).uv(0.0F, y0 / 32.0F).endVertex();
		bufferBuilder.vertex((x0 + width), y0, -100.0D).color(64, 64, 64, 255).uv(width / 32.0F, y0 / 32.0F).endVertex();
		bufferBuilder.vertex((x0 + width), 0.0D, -100.0D).color(64, 64, 64, 255).uv(width / 32.0F, 0.0F).endVertex();
		bufferBuilder.vertex(x0, 0.0D, -100.0D).color(64, 64, 64, 255).uv(0.0F, 0.0F).endVertex();
		bufferBuilder.vertex(x0, height, -100.0D).color(64, 64, 64, 255).color(64, 64, 64, 255).uv(0.0F, height / 32.0F).endVertex();
		bufferBuilder.vertex((x0 + width), height, -100.0D).color(64, 64, 64, 255).uv(width / 32.0F, height / 32.0F).endVertex();
		bufferBuilder.vertex((x0 + width), y1, -100.0D).color(64, 64, 64, 255).uv(width / 32.0F, y1 / 32.0F).endVertex();
		bufferBuilder.vertex(x0, y1, -100.0D).color(64, 64, 64, 255).uv(0.0F, y1 / 32.0F).endVertex();
		tessellator.end();
		RenderSystem.depthFunc(515);
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
		RenderSystem.disableTexture();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
		bufferBuilder.vertex(x0, y0 + 4, 0.0D).color(0, 0, 0, 0).uv(0.0f, 1.0f).endVertex();
		bufferBuilder.vertex(x1, y0 + 4, 0.0D).color(0, 0, 0, 0).uv(1.0f, 1.0f).endVertex();
		bufferBuilder.vertex(x1, y0, 0.0D).color(0, 0, 0, 255).uv(1.0f, 0.0f).endVertex();
		bufferBuilder.vertex(x0, y0, 0.0D).color(0, 0, 0, 255).uv(0.0f, 0.0f).endVertex();
		bufferBuilder.vertex(x0, y1, 0.0D).color(0, 0, 0, 255).uv(0.0f, 1.0f).endVertex();
		bufferBuilder.vertex(x1, y1, 0.0D).color(0, 0, 0, 255).uv(1.0f, 1.0f).endVertex();
		bufferBuilder.vertex(x1, y1 - 4, 0.0D).color(0, 0, 0, 0).uv(1.0f, 0.0f).endVertex();
		bufferBuilder.vertex(x0, y1 - 4, 0.0D).color(0, 0, 0, 0).uv(0.0f, 0.0f).endVertex();
		tessellator.end();
		int int_8 = Math.max(0, getMaxPosition() - (y1 - y0 - 4));
		if (int_8 > 0) {
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
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}

	public void save() {
		children().stream().filter(e -> e instanceof OptionValue).map(e -> (OptionValue<?>) e).forEach(OptionValue::save);
		if (diskWriter != null)
			diskWriter.run();
	}

	public void add(Entry entry) {
		if (entry instanceof OptionValue) {
			AbstractWidget element = ((OptionValue<?>) entry).getListener();
			if (element != null)
				owner.addWidget(element);
		}
		addEntry(entry);
	}

	public void slider(String optionName, float value, Consumer<Float> setter) {
		slider(optionName, value, setter, 0, 1);
	}

	public void slider(String optionName, float value, Consumer<Float> setter, float min, float max) {
		add(new SliderOptionValue(optionName, value, setter, min, max));
	}

	public <T> void input(String optionName, T value, Consumer<T> setter, Predicate<String> validator) {
		add(new InputOptionValue<>(optionName, value, setter, validator));
	}

	public <T> void input(String optionName, T value, Consumer<T> setter) {
		input(optionName, value, setter, Predicates.alwaysTrue());
	}

	private static final List<Component> boolNames = Arrays.asList(new TranslatableComponent("gui.yes"), new TranslatableComponent("gui.no"));
	private static final List<Boolean> boolValues = Arrays.asList(Boolean.TRUE, Boolean.FALSE);

	public void choices(String optionName, boolean value, BooleanConsumer setter) {
		add(new CycleOptionValue<>(optionName, boolNames, boolValues, value, setter));
	}

	public <T extends Enum<T>> void choices(String optionName, T value, Consumer<T> setter) {
		List<T> values = (List<T>) Arrays.asList(value.getClass().getEnumConstants());
		List<Component> names = Lists.transform(values, v -> Entry.makeTitle(optionName + "_" + v.name().toLowerCase(Locale.ENGLISH)));
		add(new CycleOptionValue<>(optionName, names, values, value, setter));
	}

	public <T> void choices(String optionName, T value, List<T> values, Consumer<T> setter) {
		List<Component> names = Lists.transform(values, v -> new TextComponent(v.toString()));
		add(new CycleOptionValue<>(optionName, names, values, value, setter));
	}

	@Override
	public void updateNarration(NarrationElementOutput output) {
		Entry e = getHovered();
		if (e != null) {
			e.updateNarration(output.nest());
			narrateListElementPosition(output, e);
		} else {
			Entry e1 = getFocused();
			if (e1 != null) {
				e1.updateNarration(output.nest());
				narrateListElementPosition(output, e1);
			}
		}

		output.add(NarratedElementType.USAGE, new TranslatableComponent("narration.component_list.usage"));
	}

	public abstract static class Entry extends AbstractSelectionList.Entry<Entry> implements NarrationSupplier {

		protected final Minecraft client;

		public static Component makeTitle(String key) {
			return new TranslatableComponent(makeKey(key));
		}

		public static String makeKey(String key) {
			return Util.makeDescriptionId("config", new ResourceLocation(Waila.MODID, key));
		}

		public Entry() {
			client = Minecraft.getInstance();
		}

		@Override
		public abstract void render(PoseStack matrixStack, int index, int rowTop, int rowLeft, int width, int height, int mouseX, int mouseY, boolean hovered, float deltaTime);
	}
}
