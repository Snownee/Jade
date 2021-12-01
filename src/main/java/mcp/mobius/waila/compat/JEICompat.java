//package mcp.mobius.waila.compat;
//
//import com.mojang.blaze3d.platform.InputConstants;
//
//import mcp.mobius.waila.api.Accessor;
//import mcp.mobius.waila.impl.ObjectDataCenter;
//import mezz.jei.api.IModPlugin;
//import mezz.jei.api.JeiPlugin;
//import mezz.jei.api.recipe.IFocus;
//import mezz.jei.api.recipe.IRecipeManager;
//import mezz.jei.api.runtime.IJeiRuntime;
//import mezz.jei.api.runtime.IRecipesGui;
//import net.minecraft.client.KeyMapping;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.item.ItemStack;
//import net.minecraftforge.client.event.InputEvent;
//import net.minecraftforge.client.settings.KeyConflictContext;
//import net.minecraftforge.client.settings.KeyModifier;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.fmlclient.registry.ClientRegistry;
//import snownee.jade.Jade;
//
//@JeiPlugin
//public class JEICompat implements IModPlugin {
//
//	public static final ResourceLocation ID = new ResourceLocation(Jade.MODID, "main");
//	public static KeyMapping showRecipes;
//	public static KeyMapping showUses;
//
//	public JEICompat() {
//		if (showRecipes == null) {
//			showRecipes = new KeyMapping("key.waila.show_recipes", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(323), Jade.NAME);
//			showUses = new KeyMapping("key.waila.show_uses", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(324), Jade.NAME);
//			ClientRegistry.registerKeyBinding(showRecipes);
//			ClientRegistry.registerKeyBinding(showUses);
//		}
//		MinecraftForge.EVENT_BUS.addListener(this::onKeyPressed);
//	}
//
//	@Override
//	public ResourceLocation getPluginUid() {
//		return ID;
//	}
//
//	private IJeiRuntime runtime;
//
//	@Override
//	public void onRuntimeAvailable(IJeiRuntime runtime) {
//		this.runtime = runtime;
//	}
//
//	private void onKeyPressed(InputEvent.KeyInputEvent event) {
//		if (runtime == null || showRecipes == null || showUses == null)
//			return;
//		if (event.getAction() != 1)
//			return;
//		if (!showRecipes.isDown() && !showUses.isDown())
//			return;
//		Accessor<?> accessor = ObjectDataCenter.get();
//		if (accessor == null)
//			return;
//		ItemStack stack = accessor.getPickedResult();
//		if (stack.isEmpty())
//			return;
//
//		IRecipesGui gui = runtime.getRecipesGui();
//		IRecipeManager manager = runtime.getRecipeManager();
//
//		gui.show(manager.createFocus(showUses.isDown() ? IFocus.Mode.INPUT : IFocus.Mode.OUTPUT, stack));
//	}
//}
