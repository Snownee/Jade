package snownee.jade.gui.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import snownee.jade.Jade;
import snownee.jade.gui.BaseOptionsScreen;
import snownee.jade.gui.config.value.CycleOptionValue;
import snownee.jade.gui.config.value.InputOptionValue;
import snownee.jade.gui.config.value.OptionValue;
import snownee.jade.gui.config.value.SliderOptionValue;
import snownee.jade.util.ClientProxy;

public class OptionsList extends ContainerObjectSelectionList<OptionsList.Entry> {

	public static final Component OPTION_ON = CommonComponents.OPTION_ON.copy().withStyle(style -> style.withColor(0xFFB9F6CA));
	public static final Component OPTION_OFF = CommonComponents.OPTION_OFF.copy().withStyle(style -> style.withColor(0xFFFF8A80));
	public final Set<OptionsList.Entry> forcePreview = Sets.newIdentityHashSet();
	protected final List<Entry> entries = Lists.newArrayList();
	private final Runnable diskWriter;
	public Title currentTitle;
	public KeyMapping selectedKey;
	private BaseOptionsScreen owner;
	private double targetScroll;
	private Entry defaultParent;
	private int lastActiveIndex;

	public OptionsList(BaseOptionsScreen owner, Minecraft client, int width, int height, int y0, int y1, int entryHeight, Runnable diskWriter) {
		super(client, width, height, y0, y1, entryHeight);
		this.owner = owner;
		this.diskWriter = diskWriter;
		setRenderSelection(false);
	}

	public OptionsList(BaseOptionsScreen owner, Minecraft client, int width, int height, int y0, int y1, int entryHeight) {
		this(owner, client, width, height, y0, y1, entryHeight, null);
	}

	private static void walkChildren(Entry entry, Consumer<Entry> consumer) {
		consumer.accept(entry);
		for (Entry child : entry.children) {
			walkChildren(child, consumer);
		}
	}

	@Override
	public int getRowWidth() {
		return Math.min(width, 300);
	}

	@Override
	protected int getScrollbarPosition() {
		return owner.width - 6;
	}

	@Override
	public void setScrollAmount(double d) {
		super.setScrollAmount(d);
		targetScroll = getScrollAmount();
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f) {
		double speed = !ClientProxy.hasFastScroll && Screen.hasControlDown() ? 4.5 : 1.5;
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
	protected void renderItem(GuiGraphics guiGraphics, int i, int j, float f, int k, int l, int m, int n, int o) {
		if (isSelectedItem(k) && isMouseOver(i, j)) {
			renderSelection(guiGraphics, m, n, o, -1, -1);
		}
		super.renderItem(guiGraphics, i, j, f, k, l, m, n, o);
	}

	@Override
	protected void renderSelection(GuiGraphics guiGraphics, int i, int j, int k, int l, int m) {
		guiGraphics.fill(x0, i - 2, x1, i + k + 2, 0x33FFFFFF);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		{
			targetScroll = Math.min(targetScroll, getMaxScroll());
			double diff = targetScroll - super.getScrollAmount();
			if (Math.abs(diff) > 0.0003) {
				super.setScrollAmount(super.getScrollAmount() + diff * delta);
			}
		}
		Entry pointAt = getEntryAtPosition(mouseX, mouseY);
		if (pointAt instanceof Title) {
			setSelected(null);
		} else {
			setSelected(pointAt);
		}
		int activeIndex = pointAt != null ? children().indexOf(pointAt) : Mth.clamp((int) getScrollAmount() / itemHeight, 0, getItemCount() - 1);
		if (activeIndex >= 0 && activeIndex != lastActiveIndex) {
			lastActiveIndex = activeIndex;
			Entry entry = getEntry(activeIndex);
			while (entry != null) {
				if (entry instanceof Title) {
					currentTitle = (Title) entry;
					break;
				}
				entry = entry.parent;
			}
		}

		enableScissor(guiGraphics);
		renderBackground(guiGraphics);
		int scrollPosX = getScrollbarPosition();
		int j = scrollPosX + 6;
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();
		RenderSystem.setShaderTexture(0, Screen.BACKGROUND_LOCATION);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		//		int rowLeft = getRowLeft();
		//		int scrollJump = y0 + 4 - (int) getScrollAmount();

		renderList(guiGraphics, mouseX, mouseY, delta);

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

		renderDecorations(guiGraphics, mouseX, mouseY);
		RenderSystem.disableBlend();
		guiGraphics.disableScissor();

		guiGraphics.setColor(0.35f, 0.35f, 0.35f, 1.0f);
		guiGraphics.blit(Screen.BACKGROUND_LOCATION, 0, owner.height - 32, owner.width, 32, owner.width, owner.height, 32, 32);
		guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		guiGraphics.fillGradient(0, owner.height - 32 - 4, owner.width, owner.height - 32, 100, 0x00000000, 0xEE000000);
	}

	public void save() {
		children().stream().filter(e -> e instanceof OptionValue).map(e -> (OptionValue<?>) e).forEach(OptionValue::save);
		if (diskWriter != null)
			diskWriter.run();
	}

	public <T extends Entry> T add(T entry) {
		entries.add(entry);
		if (entry instanceof Title) {
			setDefaultParent(entry);
		} else if (defaultParent != null) {
			entry.parent(defaultParent);
		}
		return entry;
	}

	@Nullable
	public Entry getEntryAt(double x, double y) {
		return getEntryAtPosition(x, y);
	}

	@Override
	public int getRowTop(int i) {
		return super.getRowTop(i);
	}

	@Override
	public int getRowBottom(int i) {
		return super.getRowBottom(i);
	}

	public void setDefaultParent(Entry defaultParent) {
		this.defaultParent = defaultParent;
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
		CycleButton.Builder<Boolean> builder = CycleButton.booleanBuilder(OPTION_ON, OPTION_OFF);
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
		CycleButton.Builder<T> builder = CycleButton.<T>builder(v -> {
			String name = v.name().toLowerCase(Locale.ENGLISH);
			return switch (name) {
				case "on" -> OPTION_ON;
				case "off" -> OPTION_OFF;
				default -> Entry.makeTitle(optionName + "_" + name);
			};
		}).withValues(values);
		if (builderConsumer != null) {
			builderConsumer.accept(builder);
		}
		return add(new CycleOptionValue<>(optionName, builder, value, setter));
	}

	public <T> OptionValue<?> choices(String optionName, T value, List<T> values, Consumer<T> setter) {
		return add(new CycleOptionValue<>(optionName, CycleButton.<T>builder(v -> Component.literal(v.toString())).withValues(values), value, setter));
	}

	public void keybind(KeyMapping keybind) {
		add(new KeybindOptionButton(this, keybind));
	}

	public void removed() {
		forcePreview.clear();
		for (Entry entry : entries) {
			entry.parent = null;
			if (!entry.children.isEmpty()) {
				entry.children.clear();
			}
		}
		clearEntries();
		owner = null;
	}

	public void updateSearch(String search) {
		clearEntries();
		if (search.isBlank()) {
			entries.forEach(this::addEntry);
			return;
		}
		Set<Entry> matches = Sets.newLinkedHashSet();
		String[] keywords = search.split("\\s+");
		for (Entry entry : entries) {
			int bingo = 0;
			for (String keyword : keywords) {
				keyword = keyword.toLowerCase(Locale.ENGLISH);
				for (String message : entry.getMessages()) {
					if (message.contains(keyword)) {
						bingo++;
						break;
					}
				}
			}
			if (bingo == keywords.length) {
				walkChildren(entry, matches::add);
				while (entry.parent != null) {
					entry = entry.parent;
					matches.add(entry);
				}
			}
		}
		for (Entry entry : entries) {
			if (matches.contains(entry)) {
				addEntry(entry);
			}
		}
	}

	public void updateSaveState() {
		for (Entry entry : entries) {
			if (entry instanceof OptionValue<?> value && !value.isValidValue()) {
				owner.saveButton.active = false;
				return;
			}
		}
		owner.saveButton.active = true;
	}

	public void showOnTop(Entry entry) {
		targetScroll = itemHeight * children().indexOf(entry) + 1;
	}

	public void resetMappingAndUpdateButtons() {
		for (Entry entry : entries) {
			if (entry instanceof KeybindOptionButton button) {
				button.refresh(selectedKey);
			}
		}
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (selectedKey != null) {
			Options options = Minecraft.getInstance().options;
			if (i == 256) {
				options.setKey(selectedKey, InputConstants.UNKNOWN);
			} else {
				options.setKey(selectedKey, InputConstants.getKey(i, j));
			}
			selectedKey = null;
			resetMappingAndUpdateButtons();
			return true;
		}
		return super.keyPressed(i, j, k);
	}

	public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {

		protected final Minecraft client;
		private final List<String> messages = Lists.newArrayList();
		@Nullable
		protected String description;
		private Entry parent;
		private List<Entry> children = List.of();

		public Entry() {
			client = Minecraft.getInstance();
		}

		public static MutableComponent makeTitle(String key) {
			return Component.translatable(makeKey(key));
		}

		public static String makeKey(String key) {
			return Util.makeDescriptionId("config", new ResourceLocation(Jade.MODID, key));
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
		public abstract void render(GuiGraphics guiGraphics, int index, int rowTop, int rowLeft, int width, int height, int mouseX, int mouseY, boolean hovered, float deltaTime);

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

		public void parent(Entry parent) {
			this.parent = parent;
			if (parent.children.isEmpty()) {
				parent.children = Lists.newArrayList();
			}
			parent.children.add(this);
		}

		public Entry parent() {
			return parent;
		}

		public final List<String> getMessages() {
			return messages;
		}

		public void addMessage(String message) {
			messages.add(StringUtil.stripColor(message).toLowerCase(Locale.ENGLISH));
		}

		public void addMessageKey(String key) {
			key = makeKey(key + "_extra_msg");
			if (I18n.exists(key)) {
				addMessage(I18n.get(key));
			}
		}
	}

	public static class Title extends Entry {

		private final MutableComponent title;
		private int x;

		public Title(String key) {
			title = makeTitle(key);
			addMessageKey(key);
			addMessage(title.getString());
			key = makeKey(key + "_desc");
			if (I18n.exists(key)) {
				description = I18n.get(key);
				addMessage(description);
			}
		}

		public Title(MutableComponent title) {
			this.title = title;
		}

		public MutableComponent getTitle() {
			return title;
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int rowTop, int rowLeft, int width, int height, int mouseX, int mouseY, boolean hovered, float deltaTime) {
			x = rowLeft;
			guiGraphics.drawString(client.font, title, getTextX(width), rowTop + height - client.font.lineHeight, 16777215);
		}

		@Override
		public List<? extends AbstractWidget> children() {
			return List.of();
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
