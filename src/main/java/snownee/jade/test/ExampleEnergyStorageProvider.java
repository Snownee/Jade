package snownee.jade.test;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Sheep;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;

public enum ExampleEnergyStorageProvider
		implements IServerExtensionProvider<Sheep, CompoundTag>, IClientExtensionProvider<CompoundTag, EnergyView> {
	INSTANCE;

	@Override
	public ResourceLocation getUid() {
		return ExamplePlugin.UID_TEST_ENERGY;
	}

	@Override
	public List<ClientViewGroup<EnergyView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> groups) {
		return ClientViewGroup.map(groups, tag -> EnergyView.read(tag, "RF"), (group, clientGroup) -> {
			if (group.id != null) {
				clientGroup.title = new TextComponent(group.id);
				clientGroup.bgColor = 0x5511AA11;
			} else {
				clientGroup.progressColor = 0xFFCC1111;
				clientGroup.bgColor = 0x55666666;
			}
		});
	}

	@Override
	public List<ViewGroup<CompoundTag>> getGroups(ServerPlayer player, ServerLevel world, Sheep target, boolean showDetails) {
		var cell1 = new ViewGroup<>(List.of(EnergyView.of(0, 2000)));
		cell1.id = "1";
		float period = 40;
		cell1.setProgress(((world.getGameTime() % period) + 1) / period);
		var cell2 = new ViewGroup<>(List.of(EnergyView.of(1500, 2000), EnergyView.of(1500, 2000)));
		period = 100;
		cell2.setProgress(((world.getGameTime() % period) + 1) / period);
		return List.of(cell1, cell2);
	}
}
