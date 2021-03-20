package mcp.mobius.waila.gui;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.gui.config.OptionsListWidget;
import mcp.mobius.waila.gui.config.value.OptionsEntryValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class OptionsScreen extends Screen {

    private final Screen parent;
    private final Runnable saver;
    private final Runnable canceller;
    private OptionsListWidget options;

    public OptionsScreen(Screen parent, ITextComponent title, Runnable saver, Runnable canceller) {
        super(title);

        this.parent = parent;
        this.saver = saver;
        this.canceller = canceller;
    }

    public OptionsScreen(Screen parent, ITextComponent title) {
        this(parent, title, null, null);
    }

    @Override
    public void init(Minecraft client, int width, int height) {
        super.init(client, width, height);

        options = getOptions();
        children.add(options);
        setListener(options);

        if (saver != null && canceller != null) {
            addButton(new Button(width / 2 - 100, height - 25, 100, 20, new TranslationTextComponent("gui.done"), w -> {
                options.save();
                saver.run();
                minecraft.displayGuiScreen(parent);
            }));
            addButton(new Button(width / 2 + 5, height - 25, 100, 20, new TranslationTextComponent("gui.cancel"), w -> {
                canceller.run();
                minecraft.displayGuiScreen(parent);
            }));
        } else {
            addButton(new Button(width / 2 - 50, height - 25, 100, 20, new TranslationTextComponent("gui.done"), w -> {
                options.save();
                minecraft.displayGuiScreen(parent);
            }));
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        options.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, font, title, width / 2, 12, 16777215);
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        if (mouseY < 32 || mouseY > height - 32)
            return;

        OptionsListWidget.Entry entry = options.getSelected();
        if (entry instanceof OptionsEntryValue) {
            OptionsEntryValue<?> value = (OptionsEntryValue<?>) entry;

            if (I18n.hasKey(value.getDescription())) {
                int valueX = value.getX() + 10;
                String title = value.getTitle().getString();
                if (mouseX < valueX || mouseX > valueX + font.getStringWidth(title))
                    return;

                List<IReorderingProcessor> tooltip = Lists.newArrayList(value.getTitle().func_241878_f());
                List<IReorderingProcessor> tooltip2 = font.trimStringToWidth(new TranslationTextComponent(value.getDescription()), 200);
                tooltip.addAll(tooltip2);
                renderTooltip(matrixStack, tooltip, mouseX, mouseY);
            }
        }
    }

    public void renderBackground(MatrixStack matrixStack) {
        this.renderBackground(matrixStack, 0);
    }

    public void renderBackground(MatrixStack matrixStack, int vOffset) {
        if (this.minecraft.world != null) {
            this.fillGradient(matrixStack, 0, 0, this.width, this.height, -1072689136, -804253680);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this, matrixStack));
        } else {
            this.renderDirtBackground(vOffset);
        }
    }

    @Override
    public IGuiEventListener addListener(IGuiEventListener listener) {
        children.add(listener);
        return listener;
    }

    public abstract OptionsListWidget getOptions();

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return options.mouseScrolled(mouseX, mouseY, delta);
    }
}
