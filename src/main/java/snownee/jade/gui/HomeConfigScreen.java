package snownee.jade.gui;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import snownee.jade.Jade;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.util.ModIdentification;
import snownee.jade.util.SmoothChasingValue;

public class HomeConfigScreen extends Screen {

	private final RandomSource random = RandomSource.create(42);
	private final Screen parent;
	private final SmoothChasingValue titleY;
	private final SmoothChasingValue creditHover;
	private final String credit;
	private final List<TextParticle> particles = Lists.newArrayList();
	private int creditWidth;
	private boolean hovered;
	private float ticks;
	private byte particleType;

	public HomeConfigScreen(Screen parent) {
		super(Component.translatable("gui.jade.configuration"));
		this.parent = parent;
		titleY = new SmoothChasingValue().start(8).target(32).withSpeed(0.1F);
		creditHover = new SmoothChasingValue();
		credit = "Made with §c❤§r by Snownee";

		Calendar calendar = Calendar.getInstance();
		int month = calendar.get(Calendar.MONTH) + 1;
		int date = calendar.get(Calendar.DATE);
		if (month == 12 && date >= 24 && date <= 26) {
			particleType = 1;
		} else if (month == 6 && date == 28) {
			particleType = 2;
		}
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
		addRenderableWidget(Button.builder(Component.translatable("gui.done"), w -> {
			Jade.CONFIG.save();
			PluginConfig.INSTANCE.save();
			minecraft.setScreen(parent);
		}).bounds(width / 2 - 50, height / 2 + 20, 100, 20).build());
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
			if (particleType == 2) {
				for (int i = 0; i < 11; i++) {
					colors.add(Mth.hsvToRgb(random.nextFloat(), .8F, .9F));
				}
			} else if (particleType == 1) {
				IntList palette = IntList.of(0xD6E4E5, 0xD6E4E5, 0xEFF5F5, 0xEFF5F5, 0x497174, 0xEB6440);
				for (int i = 0; i < 11; i++) {
					colors.add(palette.getInt(random.nextInt(palette.size())));
				}
			} else {
				for (int i = 0; i < 11; i++) {
					colors.add(Mth.color(1 - random.nextFloat() * 0.6F, 1, 1));
				}
			}
			for (int color : colors) {
				int ox = random.nextIntBetweenInclusive(-creditWidth / 2, creditWidth / 2);
				String text = particleType == 2 ? "❤" : "❄";
				particles.add(new TextParticle(text, width * 0.5F + ox, creditY + random.nextInt(10), ox * 0.08F, -5 - random.nextFloat() * 3, color, 0.75F + random.nextFloat() * 0.5F));
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

	private static class TextParticle {
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
		}

		private void render(GuiGraphics guiGraphics, Font font) {
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(x, y, 0);
			guiGraphics.pose().scale(scale, scale, scale);
			guiGraphics.drawString(font, text, 0, 0, color);
			guiGraphics.pose().popPose();
		}
	}

}
