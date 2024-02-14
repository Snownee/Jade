package snownee.jade.gui;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;
import com.mojang.math.Axis;

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
	private final List<TextParticle> pendingParticles = Lists.newArrayList();
	private int creditWidth;
	private boolean hovered;
	private float ticks;
	private byte festival;
	private float nextParticleIn;

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
		} else if (month <= 2 && isLunarNewYear(now)) {
			festival = 99;
		}
		credit = Component.translatable("gui.jade.by", Component.literal("❤").withStyle(ChatFormatting.RED)).withStyle(s -> {
			if (festival != 0 && festival != 1) {
				s = s.withColor(0xF1E3A4);
			}
			return s;
		});
	}

	private static boolean isLunarNewYear(LocalDate now) {
		int year = now.getYear();
		int newYearMonthAndDay = switch (year) {
			case 2025 -> 129;
			case 2026 -> 217;
			case 2027 -> 206;
			case 2028 -> 126;
			case 2029 -> 213;
			case 2030 -> 203;
			case 2031 -> 123;
			case 2032 -> 211;
			case 2033 -> 131;
			case 2034 -> 219;
			case 2035 -> 208;
			case 2036 -> 128;
			case 2037 -> 215;
			case 2038 -> 204;
			case 2039 -> 124;
			case 2040 -> 212;
			case 2041 -> 201;
			case 2042 -> 122;
			case 2043 -> 210;
			default -> 0;
		};
		if (newYearMonthAndDay == 0) {
			return false;
		}
		int newYearMonth = newYearMonthAndDay / 100;
		int newYearDay = newYearMonthAndDay % 100;
		LocalDate newYearDate = LocalDate.of(year, newYearMonth, newYearDay);
		int newYearDayOfYear = newYearDate.getDayOfYear();
		int dayOfYear = now.getDayOfYear();
		return dayOfYear >= newYearDayOfYear - 1 && dayOfYear <= newYearDayOfYear + 2;
	}

	@Override
	protected void init() {
		Objects.requireNonNull(minecraft);
		particles.clear();
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
		Objects.requireNonNull(minecraft).setScreen(parent);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		Objects.requireNonNull(minecraft);
		ticks += partialTicks;
		if (ticks > nextParticleIn) {
			if (festival == 3) {
				nextParticleIn = ticks + 1;
				if (pendingParticles.isEmpty()) {
					festival3populateNew();
				}
				TextParticle particle = pendingParticles.remove(0);
				particle.x = mouseX - 5;
				particle.y = mouseY;
				particles.add(particle);
			} else if (festival == 1) {
				nextParticleIn = ticks + 10 + random.nextFloat() * 10;
				int color = Mth.color(1 - random.nextFloat() * 0.6F, 1, 1);
				color |= (random.nextInt(80) + 40) << 24;
				int x = random.nextIntBetweenInclusive(40, width + 100);
				var particle = new TextParticle("❄", x, -20, -0.3F, 0.5f, color, 2F + random.nextFloat());
				particle.gravity = 0F;
				particles.add(particle);
			}
		}
		renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
		boolean smallUI = minecraft.getWindow().getGuiScale() < 3;
		int left = width / 2 - 105;
		int top = height / 4 - 20;
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(left, top, 0);

		float scale = smallUI ? 2F : 1.5F;
		guiGraphics.pose().scale(scale, scale, scale);
		guiGraphics.drawString(font, ModIdentification.getModName(Jade.MODID).orElse("Jade"), 0, 0, 0xFFFFFF);

		guiGraphics.pose().scale(0.5F, 0.5F, 0.5F);
		titleY.tick(partialTicks);
		String desc2 = I18n.get("gui.jade.configuration.desc2");
		float scaledX, scaledY;
		if (desc2.isEmpty()) {
			guiGraphics.pose().popPose();
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(left, top, 0);
			scaledX = mouseX - left;
			scaledY = mouseY - top;
		} else {
			scaledX = (mouseX - left) / scale * 2;
			scaledY = (mouseY - top) / scale * 2;
		}
		drawFancyTitle(guiGraphics, I18n.get("gui.jade.configuration.desc1"), Math.min(titleY.value, 20F), 20F, scaledX, scaledY);
		if (!desc2.isEmpty()) {
			drawFancyTitle(guiGraphics, desc2, Math.min(titleY.value + 3F, 32F), 32F, scaledX, scaledY);
		}
		guiGraphics.pose().popPose();
		super.render(guiGraphics, mouseX, mouseY, partialTicks);

		int creditX = (int) (width * 0.5F - creditWidth * 0.5F);
		int creditY = (int) (height * 0.9F - 5);
		boolean hover = mouseX >= creditX && mouseX < creditX + creditWidth && mouseY >= creditY && mouseY < creditY + 10;
		if (!hovered && hover) {
			creditHover.target(1);
		} else if (!hover) {
			creditHover.target(0);
		} else if (creditHover.value > 0.5) {
			creditHover.target(0);
			IntList colors = new IntArrayList();
			String text = "❄";
			if (festival == 2) {
				festival = 3;
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
				float x = width * 0.5F + ox;
				float y = creditY + random.nextInt(10);
				float dx = ox * 0.08F;
				float dy = -5 - random.nextFloat() * 3;
				var particle = new TextParticle(text, x, y, dx, dy, color, 0.75F + random.nextFloat() * 0.5F);
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
			if (p.y > height + 20) {
				return true;
			}
			p.render(guiGraphics, font);
			return false;
		});
	}

	private void festival3populateNew() {
		IntList colors = new IntArrayList();
		String text = random.nextBoolean() ? "UwU" : "OwO";
		switch (random.nextInt(7)) {
			case 0 -> {
				colors.add(0xE40303);
				colors.add(0xFF8C00);
				colors.add(0xFFED00);
				colors.add(0x008026);
				colors.add(0x732982);
				colors.add(0x732982);
			}
			case 1 -> {
				colors.add(0x5BCEFA);
				colors.add(0xF5A9B8);
				colors.add(0xFFFFFF);
				colors.add(0xF5A9B8);
				colors.add(0x5BCEFA);
			}
			case 2 -> {
				colors.add(0xD60270);
				colors.add(0xD60270);
				colors.add(0x9B4F96);
				colors.add(0x0038A8);
				colors.add(0x0038A8);
			}
			case 3 -> {
				colors.add(0xFF218C);
				colors.add(0xFF218C);
				colors.add(0xFFD800);
				colors.add(0xFFD800);
				colors.add(0x21B1FF);
				colors.add(0x21B1FF);
			}
			case 4 -> {
				colors.add(0x000000);
				colors.add(0xA3A3A3);
				colors.add(0xFFFFFF);
				colors.add(0x800080);
			}
			case 5 -> {
				colors.add(0xFF76A4);
				colors.add(0xFFFFFF);
				colors.add(0xC011D7);
				colors.add(0x000000);
				colors.add(0x2F3CBE);
			}
			case 6 -> {
				colors.add(0xFCF434);
				colors.add(0xFFFFFF);
				colors.add(0x9C59D1);
				colors.add(0x2C2C2C);
			}
		}
		int ox = random.nextIntBetweenInclusive(-creditWidth / 2, creditWidth / 2);
		float dx = ox * 0.08F;
		float dy = -5 - random.nextFloat() * 3;
		for (int color : colors) {
			for (int i = 0; i < 5; i++) {
				var particle = new TextParticle(text, 0, 0, dx, dy, color, 1);
				pendingParticles.add(particle);
			}
		}
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
		private float gravity = 0.98F;

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
			motionY += gravity * partialTicks;
			if (festival == 99) {
				boolean greaterThanZero = age > 0;
				age -= partialTicks;
				if (greaterThanZero && age <= 0) {
					text = random.nextBoolean() ? "✴" : "✳";
					color = random.nextBoolean() ? 0xFFD427 : 0xF0C415;
					Objects.requireNonNull(minecraft);
					minecraft.getSoundManager().play(SimpleSoundInstance.forUI(random.nextBoolean() ? SoundEvents.FIREWORK_ROCKET_BLAST : SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, 0.7F));
				}
			} else if (festival == 1) {
				age -= partialTicks;
			}
		}

		private void render(GuiGraphics guiGraphics, Font font) {
			if (festival == 99 && age < -4) {
				return;
			}
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(x, y, 0);
			guiGraphics.pose().scale(scale, scale, scale);
			if (festival == 1) {
				guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(age));
			}
			guiGraphics.drawString(font, text, 0, 0, color);
			guiGraphics.pose().popPose();
		}
	}

}
