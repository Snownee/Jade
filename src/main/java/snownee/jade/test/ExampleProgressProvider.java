package snownee.jade.test;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ProgressView;
import snownee.jade.api.view.ViewGroup;

public enum ExampleProgressProvider implements IServerExtensionProvider<AbstractFurnaceBlockEntity, CompoundTag>,
		IClientExtensionProvider<CompoundTag, ProgressView> {
	INSTANCE;

	@Override
	public ResourceLocation getUid() {
		return ExamplePlugin.UID_TEST_PROGRESS;
	}

	@Override
	public List<ClientViewGroup<ProgressView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> groups) {
		return ClientViewGroup.map(groups, ProgressView::read, (group, clientGroup) -> {
			var view = clientGroup.views.get(0);
			view.style.color(0xFFCC0000);
			view.text = new TextComponent("Testtttttttttttttttttttttttttttttttt");

			view = clientGroup.views.get(1);
			view.style.color(0xFF00CC00);
			view.text = new TextComponent("Test");
		});
	}

	@Override
	public List<ViewGroup<CompoundTag>> getGroups(ServerPlayer player, ServerLevel world, AbstractFurnaceBlockEntity target, boolean showDetails) {
		float period = 40;
		var progress1 = ProgressView.create(((world.getGameTime() % period) + 1) / period);
		period = 200;
		var progress2 = ProgressView.create(((world.getGameTime() % period) + 1) / period);
		var group = new ViewGroup<>(List.of(progress1, progress2));
		return List.of(group);
	}
}
