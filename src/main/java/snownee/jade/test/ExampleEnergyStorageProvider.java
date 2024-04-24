package snownee.jade.test;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import snownee.jade.api.Accessor;
import snownee.jade.api.ui.MessageType;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;

public enum ExampleEnergyStorageProvider
		implements IServerExtensionProvider<CompoundTag>, IClientExtensionProvider<CompoundTag, EnergyView> {
	INSTANCE;

	@Override
	public ResourceLocation getUid() {
		return ExamplePlugin.UID_TEST_ENERGY;
	}

	@Override
	public List<ClientViewGroup<EnergyView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> groups) {
		return ClientViewGroup.map(groups, tag -> EnergyView.read(tag, "RF"), (group, clientGroup) -> {
			if (group.id != null) {
				clientGroup.title = Component.literal(group.id);
				clientGroup.messageType = MessageType.DANGER;
			} else {
				clientGroup.messageType = MessageType.INFO;
			}
		});
	}

	@Override
	public List<ViewGroup<CompoundTag>> getGroups(Accessor<?> accessor) {
		Level world = accessor.getLevel();
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
