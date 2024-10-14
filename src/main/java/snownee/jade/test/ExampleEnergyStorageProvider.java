package snownee.jade.test;

import java.util.List;

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
		implements IServerExtensionProvider<EnergyView.Data>, IClientExtensionProvider<EnergyView.Data, EnergyView> {
	INSTANCE;

	@Override
	public ResourceLocation getUid() {
		return ExamplePlugin.UID_TEST_ENERGY;
	}

	@Override
	public List<ClientViewGroup<EnergyView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<EnergyView.Data>> groups) {
		return ClientViewGroup.map(groups, data -> EnergyView.read(data, "RF"), (group, clientGroup) -> {
			if (group.id != null) {
				clientGroup.title = Component.literal(group.id);
				clientGroup.messageType = MessageType.DANGER;
			} else {
				clientGroup.messageType = MessageType.INFO;
			}
		});
	}

	@Override
	public List<ViewGroup<EnergyView.Data>> getGroups(Accessor<?> accessor) {
		Level world = accessor.getLevel();
		var cell1 = new ViewGroup<>(List.of(new EnergyView.Data(0, 2000)));
		cell1.id = "1";
		float period = 40;
		cell1.setProgress(((world.getGameTime() % period) + 1) / period);
		var cell2 = new ViewGroup<>(List.of(new EnergyView.Data(1500, 2000), new EnergyView.Data(1500, 2000)));
		period = 100;
		cell2.setProgress(((world.getGameTime() % period) + 1) / period);
		return List.of(cell1, cell2);
	}
}
