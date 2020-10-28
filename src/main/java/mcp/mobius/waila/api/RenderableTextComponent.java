package mcp.mobius.waila.api;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcp.mobius.waila.api.impl.WailaRegistrar;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class RenderableTextComponent extends StringTextComponent {

    public RenderableTextComponent(ResourceLocation id, CompoundNBT data) {
        super(getRenderString(id, data));
    }

    public RenderableTextComponent(RenderableTextComponent... components) {
        super(getRenderString(components));
    }

    public List<RenderContainer> getRenderers() {
        List<RenderContainer> renderers = Lists.newArrayList();
        CompoundNBT data = getData();
        if (data.contains("renders")) {
            ListNBT list = data.getList("renders", Constants.NBT.TAG_STRING);
            list.forEach(t -> {
                StringNBT stringTag = (StringNBT) t;
                try {
                    CompoundNBT tag = JsonToNBT.getTagFromJson(stringTag.getString());
                    ResourceLocation id = new ResourceLocation(tag.getString("id"));
                    CompoundNBT dataTag = tag.getCompound("data");
                    renderers.add(new RenderContainer(id, dataTag));
                } catch (CommandSyntaxException e) {
                    // no-op
                }
            });
        } else {
            ResourceLocation id = new ResourceLocation(data.getString("id"));
            CompoundNBT dataTag = data.getCompound("data");
            renderers.add(new RenderContainer(id, dataTag));
        }

        return renderers;
    }

    private CompoundNBT getData() {
        try {
            return JsonToNBT.getTagFromJson(getString());
        } catch (CommandSyntaxException e) {
            return new CompoundNBT();
        }
    }

    private static String getRenderString(ResourceLocation id, CompoundNBT data) {
        CompoundNBT renderData = new CompoundNBT();
        renderData.putString("id", id.toString());
        renderData.put("data", data);
        return renderData.toString();
    }

    private static String getRenderString(RenderableTextComponent... components) {
        CompoundNBT container = new CompoundNBT();
        ListNBT renderData = new ListNBT();
        for (RenderableTextComponent component : components)
            renderData.add(StringNBT.valueOf(component.getString()));
        container.put("renders", renderData);
        return container.toString();
    }

    public static class RenderContainer {
        private final ResourceLocation id;
        private final CompoundNBT data;
        private final ITooltipRenderer renderer;

        public RenderContainer(ResourceLocation id, CompoundNBT data) {
            this.id = id;
            this.data = data;
            this.renderer = WailaRegistrar.INSTANCE.getTooltipRenderer(id);
        }

        public ResourceLocation getId() {
            return id;
        }

        public CompoundNBT getData() {
            return data;
        }

        public ITooltipRenderer getRenderer() {
            return renderer;
        }
    }
}
