package mcp.mobius.waila.overlay;

// TODO
public class DecoratorRenderer {

//    public static void onRenderWorldLast(RenderWorldLastEvent event) {
//        if (RayTracing.INSTANCE.getTarget() == null || RayTracing.INSTANCE.getTargetStack().isEmpty())
//            return;
//
//        double partialTicks = event.getPartialTicks();
//
//        DataAccessorCommon accessor = DataAccessorCommon.INSTANCE;
//        World world = Minecraft.getMinecraft().world;
//        EntityPlayer player = Minecraft.getMinecraft().player;
//        Entity viewEntity = Minecraft.getMinecraft().getRenderViewEntity();
//
//        if (world == null || player == null || viewEntity == null)
//            return;
//
//        accessor.set(world, player, RayTracing.INSTANCE.getTarget(), viewEntity, partialTicks);
//
//        Block block = accessor.getBlock();
//        if (!ModuleRegistrar.instance().hasBlockDecorator(block))
//            return;
//
//        GlStateManager.pushAttrib();
//        GlStateManager.disableTexture2D();
//        GlStateManager.enableBlend();
//        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//        GlStateManager.depthMask(false);
//
//        for (List<IWailaBlockDecorator> decoratorsList : ModuleRegistrar.instance().getBlockDecorators(block).values()) {
//            for (IWailaBlockDecorator decorator : decoratorsList)
//                try {
//                    GlStateManager.pushMatrix();
//                    decorator.decorateBlock(RayTracing.INSTANCE.getTargetStack(), accessor, ConfigHandler.instance());
//                    GlStateManager.popMatrix();
//                } catch (Throwable e) {
//                    GlStateManager.popMatrix();
//                    WailaExceptionHandler.handleErr(e, decorator.getClass().toString(), null);
//                }
//        }
//
//        GlStateManager.enableTexture2D();
//        GlStateManager.popAttrib();
//    }
}