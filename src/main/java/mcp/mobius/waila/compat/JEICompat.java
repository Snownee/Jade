package mcp.mobius.waila.compat;

import mcp.mobius.waila.api.impl.DataAccessor;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import snownee.jade.Jade;

@JeiPlugin
public class JEICompat implements IModPlugin {

	public static final ResourceLocation ID = new ResourceLocation(Jade.MODID, "main");
	public static KeyBinding showRecipes;
	public static KeyBinding showUses;

	public JEICompat() {
		if (showRecipes == null) {
			showRecipes = new KeyBinding("key.waila.show_recipes", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputMappings.Type.KEYSYM.getOrMakeInput(323), Jade.NAME);
			showUses = new KeyBinding("key.waila.show_uses", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputMappings.Type.KEYSYM.getOrMakeInput(324), Jade.NAME);
			ClientRegistry.registerKeyBinding(showRecipes.getKeyBinding());
			ClientRegistry.registerKeyBinding(showUses.getKeyBinding());
		}
		MinecraftForge.EVENT_BUS.addListener(this::onKeyPressed);
	}

	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}

	private IJeiRuntime runtime;

	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime) {
		this.runtime = runtime;
	}

	private void onKeyPressed(InputEvent.KeyInputEvent event) {
		if (runtime == null || showRecipes == null || showUses == null)
			return;
		if (event.getAction() != 1)
			return;
		if (!showRecipes.isKeyDown() && !showUses.isKeyDown())
			return;
		ItemStack stack = DataAccessor.INSTANCE.getStack();
		if (stack.isEmpty())
			return;

		IRecipesGui gui = runtime.getRecipesGui();
		IRecipeManager manager = runtime.getRecipeManager();

		gui.show(manager.createFocus(showUses.isKeyDown() ? IFocus.Mode.INPUT : IFocus.Mode.OUTPUT, stack));
	}
}