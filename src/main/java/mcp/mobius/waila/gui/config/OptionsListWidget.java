package mcp.mobius.waila.gui.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import mcp.mobius.waila.Waila;
import mcp.mobius.waila.gui.OptionsScreen;
import mcp.mobius.waila.gui.config.value.CycleOptionValue;
import mcp.mobius.waila.gui.config.value.InputOptionValue;
import mcp.mobius.waila.gui.config.value.OptionValue;
import mcp.mobius.waila.gui.config.value.SliderOptionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class OptionsListWidget extends AbstractList<OptionsListWidget.Entry> {

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
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
		Entry entry = getEntryAtPosition(mouseX, mouseY);
		setSelected(entry);

		renderBackground(matrixStack);
		int scrollPosX = getScrollbarPosition();
		int j = scrollPosX + 6;
		RenderSystem.disableLighting();
		RenderSystem.disableFog();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		minecraft.getTextureManager().bindTexture(BACKGROUND_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		int rowLeft = getRowLeft();
		int scrollJump = y0 + 4 - (int) getScrollAmount();

		renderList(matrixStack, rowLeft, scrollJump, mouseX, mouseY, delta);
		minecraft.getTextureManager().bindTexture(BACKGROUND_LOCATION);
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(519);
		bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
		bufferBuilder.pos(x0, y0, -100.0D).color(64, 64, 64, 255).tex(0.0F, y0 / 32.0F).endVertex();
		bufferBuilder.pos((x0 + width), y0, -100.0D).color(64, 64, 64, 255).tex(width / 32.0F, y0 / 32.0F).endVertex();
		bufferBuilder.pos((x0 + width), 0.0D, -100.0D).color(64, 64, 64, 255).tex(width / 32.0F, 0.0F).endVertex();
		bufferBuilder.pos(x0, 0.0D, -100.0D).color(64, 64, 64, 255).tex(0.0F, 0.0F).endVertex();
		bufferBuilder.pos(x0, height, -100.0D).color(64, 64, 64, 255).color(64, 64, 64, 255).tex(0.0F, height / 32.0F).endVertex();
		bufferBuilder.pos((x0 + width), height, -100.0D).color(64, 64, 64, 255).tex(width / 32.0F, height / 32.0F).endVertex();
		bufferBuilder.pos((x0 + width), y1, -100.0D).color(64, 64, 64, 255).tex(width / 32.0F, y1 / 32.0F).endVertex();
		bufferBuilder.pos(x0, y1, -100.0D).color(64, 64, 64, 255).tex(0.0F, y1 / 32.0F).endVertex();
		tessellator.draw();
		RenderSystem.depthFunc(515);
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
		RenderSystem.disableAlphaTest();
		RenderSystem.shadeModel(GL11.GL_SMOOTH);
		RenderSystem.disableTexture();
		bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
		bufferBuilder.pos(x0, y0 + 4, 0.0D).color(0, 0, 0, 0).tex(0.0f, 1.0f).endVertex();
		bufferBuilder.pos(x1, y0 + 4, 0.0D).color(0, 0, 0, 0).tex(1.0f, 1.0f).endVertex();
		bufferBuilder.pos(x1, y0, 0.0D).color(0, 0, 0, 255).tex(1.0f, 0.0f).endVertex();
		bufferBuilder.pos(x0, y0, 0.0D).color(0, 0, 0, 255).tex(0.0f, 0.0f).endVertex();
		bufferBuilder.pos(x0, y1, 0.0D).color(0, 0, 0, 255).tex(0.0f, 1.0f).endVertex();
		bufferBuilder.pos(x1, y1, 0.0D).color(0, 0, 0, 255).tex(1.0f, 1.0f).endVertex();
		bufferBuilder.pos(x1, y1 - 4, 0.0D).color(0, 0, 0, 0).tex(1.0f, 0.0f).endVertex();
		bufferBuilder.pos(x0, y1 - 4, 0.0D).color(0, 0, 0, 0).tex(0.0f, 0.0f).endVertex();
		tessellator.draw();
		int int_8 = Math.max(0, getMaxPosition() - (y1 - y0 - 4));
		if (int_8 > 0) {
			int int_9 = (int) ((float) ((y1 - y0) * (y1 - y0)) / (float) getMaxPosition());
			int_9 = MathHelper.clamp(int_9, 32, y1 - y0 - 8);
			int int_10 = (int) getScrollAmount() * (y1 - y0 - int_9) / int_8 + y0;
			if (int_10 < y0) {
				int_10 = y0;
			}

			bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
			bufferBuilder.pos(scrollPosX, y1, 0.0D).color(0, 0, 0, 255).tex(0.0f, 1.0f).endVertex();
			bufferBuilder.pos(j, y1, 0.0D).color(0, 0, 0, 255).tex(1.0f, 1.0f).endVertex();
			bufferBuilder.pos(j, y0, 0.0D).color(0, 0, 0, 255).tex(1.0f, 0.0f).endVertex();
			bufferBuilder.pos(scrollPosX, y0, 0.0D).color(0, 0, 0, 255).tex(0.0f, 0.0f).endVertex();
			bufferBuilder.pos(scrollPosX, (int_10 + int_9), 0.0D).color(128, 128, 128, 255).tex(0.0f, 1.0f).endVertex();
			bufferBuilder.pos(j, (int_10 + int_9), 0.0D).color(128, 128, 128, 255).tex(1.0f, 1.0f).endVertex();
			bufferBuilder.pos(j, int_10, 0.0D).color(128, 128, 128, 255).tex(1.0f, 0.0f).endVertex();
			bufferBuilder.pos(scrollPosX, int_10, 0.0D).color(128, 128, 128, 255).tex(0.0f, 0.0f).endVertex();
			bufferBuilder.pos(scrollPosX, (int_10 + int_9 - 1), 0.0D).color(192, 192, 192, 255).tex(0.0f, 1.0f).endVertex();
			bufferBuilder.pos(j - 1, int_10 + int_9 - 1, 0.0D).color(192, 192, 192, 255).tex(1.0f, 1.0f).endVertex();
			bufferBuilder.pos(j - 1, int_10, 0.0D).color(192, 192, 192, 255).tex(1.0f, 0.0f).endVertex();
			bufferBuilder.pos(scrollPosX, int_10, 0.0D).color(192, 192, 192, 255).tex(0.0f, 0.0f).endVertex();
			tessellator.draw();
		}

		renderDecorations(matrixStack, mouseX, mouseY);
		RenderSystem.enableTexture();
		RenderSystem.shadeModel(GL11.GL_FLAT);
		RenderSystem.enableAlphaTest();
		RenderSystem.disableBlend();
	}

	public void save() {
		getEventListeners().stream().filter(e -> e instanceof OptionValue).map(e -> (OptionValue<?>) e).forEach(OptionValue::save);
		if (diskWriter != null)
			diskWriter.run();
	}

	public void add(Entry entry) {
		if (entry instanceof OptionValue) {
			IGuiEventListener element = ((OptionValue<?>) entry).getListener();
			if (element != null)
				owner.addListener(element);
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

	private static final List<ITextComponent> boolNames = Arrays.asList(new TranslationTextComponent("gui.yes"), new TranslationTextComponent("gui.no"));
	private static final List<Boolean> boolValues = Arrays.asList(Boolean.TRUE, Boolean.FALSE);

	public void choices(String optionName, boolean value, BooleanConsumer setter) {
		add(new CycleOptionValue<>(optionName, boolNames, boolValues, value, setter));
	}

	public <T extends Enum<T>> void choices(String optionName, T value, Consumer<T> setter) {
		List<T> values = (List<T>) Arrays.asList(value.getClass().getEnumConstants());
		List<ITextComponent> names = Lists.transform(values, v -> Entry.makeTitle(optionName + "_" + v.name().toLowerCase(Locale.ENGLISH)));
		add(new CycleOptionValue<>(optionName, names, values, value, setter));
	}

	public <T> void choices(String optionName, T value, List<T> values, Consumer<T> setter) {
		List<ITextComponent> names = Lists.transform(values, v -> new StringTextComponent(v.toString()));
		add(new CycleOptionValue<>(optionName, names, values, value, setter));
	}

	public abstract static class Entry extends AbstractList.AbstractListEntry<Entry> {

		protected final Minecraft client;

		public static ITextComponent makeTitle(String key) {
			return new TranslationTextComponent(makeKey(key));
		}

		public static String makeKey(String key) {
			return Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, key));
		}

		public Entry() {
			client = Minecraft.getInstance();
		}

		@Override
		public abstract void render(MatrixStack matrixStack, int index, int rowTop, int rowLeft, int width, int height, int mouseX, int mouseY, boolean hovered, float deltaTime);
	}
}
