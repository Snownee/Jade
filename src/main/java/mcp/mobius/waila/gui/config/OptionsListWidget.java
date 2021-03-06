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

		this.renderBackground(matrixStack);
		int scrollPosX = this.getScrollbarPosition();
		int j = scrollPosX + 6;
		RenderSystem.disableLighting();
		RenderSystem.disableFog();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		this.minecraft.getTextureManager().bindTexture(BACKGROUND_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		int rowLeft = this.getRowLeft();
		int scrollJump = this.y0 + 4 - (int) this.getScrollAmount();

		this.renderList(matrixStack, rowLeft, scrollJump, mouseX, mouseY, delta);
		this.minecraft.getTextureManager().bindTexture(BACKGROUND_LOCATION);
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(519);
		bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
		bufferBuilder.pos(this.x0, this.y0, -100.0D).color(64, 64, 64, 255).tex(0.0F, this.y0 / 32.0F).endVertex();
		bufferBuilder.pos((this.x0 + this.width), this.y0, -100.0D).color(64, 64, 64, 255).tex(this.width / 32.0F, this.y0 / 32.0F).endVertex();
		bufferBuilder.pos((this.x0 + this.width), 0.0D, -100.0D).color(64, 64, 64, 255).tex(this.width / 32.0F, 0.0F).endVertex();
		bufferBuilder.pos(this.x0, 0.0D, -100.0D).color(64, 64, 64, 255).tex(0.0F, 0.0F).endVertex();
		bufferBuilder.pos(this.x0, this.height, -100.0D).color(64, 64, 64, 255).color(64, 64, 64, 255).tex(0.0F, this.height / 32.0F).endVertex();
		bufferBuilder.pos((this.x0 + this.width), this.height, -100.0D).color(64, 64, 64, 255).tex(this.width / 32.0F, this.height / 32.0F).endVertex();
		bufferBuilder.pos((this.x0 + this.width), this.y1, -100.0D).color(64, 64, 64, 255).tex(this.width / 32.0F, this.y1 / 32.0F).endVertex();
		bufferBuilder.pos(this.x0, this.y1, -100.0D).color(64, 64, 64, 255).tex(0.0F, this.y1 / 32.0F).endVertex();
		tessellator.draw();
		RenderSystem.depthFunc(515);
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
		RenderSystem.disableAlphaTest();
		RenderSystem.shadeModel(GL11.GL_SMOOTH);
		RenderSystem.disableTexture();
		bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
		bufferBuilder.pos(this.x0, this.y0 + 4, 0.0D).color(0, 0, 0, 0).tex(0.0f, 1.0f).endVertex();
		bufferBuilder.pos(this.x1, this.y0 + 4, 0.0D).color(0, 0, 0, 0).tex(1.0f, 1.0f).endVertex();
		bufferBuilder.pos(this.x1, this.y0, 0.0D).color(0, 0, 0, 255).tex(1.0f, 0.0f).endVertex();
		bufferBuilder.pos(this.x0, this.y0, 0.0D).color(0, 0, 0, 255).tex(0.0f, 0.0f).endVertex();
		bufferBuilder.pos(this.x0, this.y1, 0.0D).color(0, 0, 0, 255).tex(0.0f, 1.0f).endVertex();
		bufferBuilder.pos(this.x1, this.y1, 0.0D).color(0, 0, 0, 255).tex(1.0f, 1.0f).endVertex();
		bufferBuilder.pos(this.x1, this.y1 - 4, 0.0D).color(0, 0, 0, 0).tex(1.0f, 0.0f).endVertex();
		bufferBuilder.pos(this.x0, this.y1 - 4, 0.0D).color(0, 0, 0, 0).tex(0.0f, 0.0f).endVertex();
		tessellator.draw();
		int int_8 = Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
		if (int_8 > 0) {
			int int_9 = (int) ((float) ((this.y1 - this.y0) * (this.y1 - this.y0)) / (float) this.getMaxPosition());
			int_9 = MathHelper.clamp(int_9, 32, this.y1 - this.y0 - 8);
			int int_10 = (int) this.getScrollAmount() * (this.y1 - this.y0 - int_9) / int_8 + this.y0;
			if (int_10 < this.y0) {
				int_10 = this.y0;
			}

			bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
			bufferBuilder.pos(scrollPosX, this.y1, 0.0D).color(0, 0, 0, 255).tex(0.0f, 1.0f).endVertex();
			bufferBuilder.pos(j, this.y1, 0.0D).color(0, 0, 0, 255).tex(1.0f, 1.0f).endVertex();
			bufferBuilder.pos(j, this.y0, 0.0D).color(0, 0, 0, 255).tex(1.0f, 0.0f).endVertex();
			bufferBuilder.pos(scrollPosX, this.y0, 0.0D).color(0, 0, 0, 255).tex(0.0f, 0.0f).endVertex();
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

		this.renderDecorations(matrixStack, mouseX, mouseY);
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
			this.client = Minecraft.getInstance();
		}

		@Override
		public abstract void render(MatrixStack matrixStack, int index, int rowTop, int rowLeft, int width, int height, int mouseX, int mouseY, boolean hovered, float deltaTime);
	}
}
