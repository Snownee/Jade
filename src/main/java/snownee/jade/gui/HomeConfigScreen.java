package snownee.jade.gui;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import snownee.jade.Jade;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.util.ModIdentification;
import snownee.jade.util.SmoothChasingValue;

public class HomeConfigScreen extends Screen {

	private final RandomSource random = RandomSource.create(42);
	private final Screen parent;
	private final SmoothChasingValue titleY;
	private final SmoothChasingValue creditHover;
	private final Component credit;
	private final List<TextParticle> particles = Lists.newArrayList();
	private int creditWidth;
	private boolean hovered;
	private float ticks;
	private byte festival;

	public HomeConfigScreen(Screen parent) {
		super(Component.translatable("gui.jade.configuration"));
		this.parent = parent;
		titleY = new SmoothChasingValue().start(8).target(32).withSpeed(0.1F);
		creditHover = new SmoothChasingValue();

		LocalDate now = LocalDate.now();
		int month = now.getMonthValue();
		int day = now.getDayOfMonth();
		if (month == 12 && day >= 24 && day <= 26) {
			festival = 1;
		} else if (month == 6 && day == 28) {
			festival = 2;
		} else {
			var newyears = new Int2IntOpenHashMap();
			newyears.put(2024, 210);
			newyears.put(2025, 129);
			newyears.put(2026, 217);
			newyears.put(2027, 206);
			newyears.put(2028, 126);
			newyears.put(2029, 213);
			newyears.put(2030, 203);
			newyears.put(2031, 123);
			newyears.put(2032, 211);
			newyears.put(2033, 131);
			newyears.put(2034, 219);
			newyears.put(2035, 208);
			newyears.put(2036, 128);
			newyears.put(2037, 215);
			newyears.put(2038, 204);
			newyears.put(2039, 124);
			newyears.put(2040, 212);
			newyears.put(2041, 201);
			newyears.put(2042, 122);
			newyears.put(2043, 210);
			int year = now.getYear();
			if (newyears.containsKey(year)) {
				int newyearMonth = newyears.get(year) / 100;
				int newyearDay = newyears.get(year) % 100;
				LocalDate newyearDate = LocalDate.of(year, newyearMonth, newyearDay);
				int newyearDayofyear = newyearDate.getDayOfYear();
				int dayofyear = now.getDayOfYear();
				if (dayofyear >= newyearDayofyear - 1 && dayofyear <= newyearDayofyear + 2) {
					festival = 99;
				}
			}
		}
		credit = Component.translatable("gui.jade.by", Component.literal("❤").withStyle(ChatFormatting.RED)).withStyle(s -> {
			if (festival != 0) {
				s = s.withColor(0xF1E3A4);
			}
			return s;
		});
	}

	@Override
	protected void init() {
		Objects.requireNonNull(minecraft);
		creditWidth = font.width(credit);
		addRenderableWidget(Button.builder(Component.translatable("gui.jade.jade_settings"), w -> {
			minecraft.setScreen(new WailaConfigScreen(HomeConfigScreen.this));
		}).bounds(width / 2 - 105, height / 2 - 10, 100, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("gui.jade.plugin_settings"), w -> {
			minecraft.setScreen(new PluginsConfigScreen(HomeConfigScreen.this));
		}).bounds(width / 2 + 5, height / 2 - 10, 100, 20).build());
		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, w -> {
			onClose();
		}).bounds(width / 2 - 50, height / 2 + 20, 100, 20).build());
	}

	@Override
	public void onClose() {
		Jade.CONFIG.save();
		PluginConfig.INSTANCE.save();
		WailaClientRegistration.instance().reloadBlocklists();
		Objects.requireNonNull(minecraft).setScreen(parent);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
		Objects.requireNonNull(minecraft);
		ticks += partialTicks;
		renderBackground(guiGraphics);
		boolean smallUI = minecraft.getWindow().getGuiScale() < 3;
		int left = width / 2 - 105;
		int top = height / 4 - 20;
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(left, top, 0);

		float scale = smallUI ? 2F : 1.5F;
		guiGraphics.pose().scale(scale, scale, scale);
		guiGraphics.drawString(font, ModIdentification.getModName(Jade.MODID), 0, 0, 0xFFFFFF);

		guiGraphics.pose().scale(0.5F, 0.5F, 0.5F);
		titleY.tick(partialTicks);
		String desc2 = I18n.get("gui.jade.configuration.desc2");
		float scaledX, scaledY;
		if (desc2.isEmpty()) {
			guiGraphics.pose().popPose();
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(left, top, 0);
			scaledX = x - left;
			scaledY = y - top;
		} else {
			scaledX = (x - left) / scale * 2;
			scaledY = (y - top) / scale * 2;
		}
		drawFancyTitle(guiGraphics, I18n.get("gui.jade.configuration.desc1"), Math.min(titleY.value, 20F), 20F, scaledX, scaledY);
		if (!desc2.isEmpty()) {
			drawFancyTitle(guiGraphics, desc2, titleY.value, 32F, scaledX, scaledY);
		}
		guiGraphics.pose().popPose();
		super.render(guiGraphics, x, y, partialTicks);

		int creditX = (int) (width * 0.5F - creditWidth * 0.5F);
		int creditY = (int) (height * 0.9F - 5);
		boolean hover = x >= creditX && x < creditX + creditWidth && y >= creditY && y < creditY + 10;
		if (!hovered && hover) {
			creditHover.target(1);
		} else if (!hover) {
			creditHover.target(0);
		} else if (creditHover.value > 0.5) {
			creditHover.target(0);
			IntList colors = new IntArrayList();
			String text = "❄";
			if (festival == 2) {
				for (int i = 0; i < 11; i++) {
					colors.add(Mth.hsvToRgb(random.nextFloat(), .8F, .9F));
				}
				text = "❤";
			} else if (festival == 1) {
				IntList palette = IntList.of(0xD6E4E5, 0xD6E4E5, 0xEFF5F5, 0xEFF5F5, 0x497174, 0xEB6440);
				for (int i = 0; i < 11; i++) {
					colors.add(palette.getInt(random.nextInt(palette.size())));
				}
			} else if (festival == 99) {
				for (int i = 0; i < 11; i++) {
					colors.add(random.nextBoolean() ? 0xA80000 : 0xC01800);
				}
				text = "✐";
			} else {
				for (int i = 0; i < 11; i++) {
					colors.add(Mth.color(1 - random.nextFloat() * 0.6F, 1, 1));
				}
			}
			for (int color : colors) {
				int ox = random.nextIntBetweenInclusive(-creditWidth / 2, creditWidth / 2);
				var particle = new TextParticle(text, width * 0.5F + ox, creditY + random.nextInt(10), ox * 0.08F, -5 - random.nextFloat() * 3, color, 0.75F + random.nextFloat() * 0.5F);
				particles.add(particle);
				if (festival == 99) {
					particle.age = 8 + random.nextFloat() * 5;
				}
			}
		}
		creditHover.tick(partialTicks);
		creditHover.value = Math.min(0.6F, creditHover.value);
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(width * 0.5F, creditY, 0);
		scale = 1 + creditHover.value * 0.2F;
		guiGraphics.pose().scale(scale, scale, scale);
		guiGraphics.pose().translate(creditWidth * -0.5F, 0, 0);
		guiGraphics.drawString(font, credit, 0, 0, 0x55FFFFFF);
		guiGraphics.pose().popPose();
		hovered = hover;

		particles.removeIf(p -> {
			p.tick(partialTicks);
			if (p.y > height) {
				return true;
			}
			p.render(guiGraphics, font);
			return false;
		});
	}

	private void drawFancyTitle(GuiGraphics guiGraphics, String text, float y, float expectY, float mouseX, float mouseY) {
		float distY = Math.abs(y - expectY);
		if (distY >= 9) {
			return;
		}
		int color = IWailaConfig.IConfigOverlay.applyAlpha(0xAAAAAA, 1 - distY / 10F);
		((JadeFont) font).jade$setGlint((ticks - y / 5F) % 90 / 45 * width, mouseX);
		((JadeFont) font).jade$setGlintStrength(1, 1 - Mth.clamp(Math.abs(mouseY - y) / 20F, 0, 1));
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0, y, 0);
		guiGraphics.drawString(font, text, 0, 0, color);
		guiGraphics.pose().popPose();
		((JadeFont) font).jade$setGlint(Float.NaN, Float.NaN);
	}

	private class TextParticle {
		private float age;
		private String text;
		private float x;
		private float y;
		private float motionX;
		private float motionY;
		private int color;
		private float scale;

		public TextParticle(String text, float x, float y, float motionX, float motionY, int color, float scale) {
			this.text = text;
			this.x = x;
			this.y = y;
			this.motionX = motionX;
			this.motionY = motionY;
			this.color = color;
			this.scale = scale;
		}

		private void tick(float partialTicks) {
			x += motionX * partialTicks;
			y += motionY * partialTicks;
			motionY += 0.98F * partialTicks;
			if (festival == 99) {
				boolean geaterThanZero = age > 0;
				age -= partialTicks;
				if (geaterThanZero && age <= 0) {
					text = random.nextBoolean() ? "✴" : "✳";
					color = random.nextBoolean() ? 0xFFD427 : 0xF0C415;
					Objects.requireNonNull(minecraft);
					minecraft.getSoundManager().play(SimpleSoundInstance.forUI(random.nextBoolean() ? SoundEvents.FIREWORK_ROCKET_BLAST : SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, 0.7F));
				}
			}
		}

		private void render(GuiGraphics guiGraphics, Font font) {
			if (festival == 99 && age < -4) {
				return;
			}
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(x, y, 0);
			guiGraphics.pose().scale(scale, scale, scale);
			guiGraphics.drawString(font, text, 0, 0, color);
			guiGraphics.pose().popPose();
		}
	}

}
