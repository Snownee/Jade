package snownee.jade.test;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import snownee.jade.api.Accessor;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.MessageType;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;

public enum ExampleFluidStorageProvider implements IServerExtensionProvider<FluidView.Data>, IClientExtensionProvider<FluidView.Data, FluidView> {
	INSTANCE;

	@Override
	public ResourceLocation getUid() {
		return ExamplePlugin.UID_TEST_FLUIDS;
	}

	@Override
	public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<FluidView.Data>> groups) {
		return ClientViewGroup.map(groups, FluidView::readDefault, (group, clientGroup) -> {
			if (group.id != null) {
				clientGroup.title = Component.literal(group.id);
			}
			clientGroup.messageType = MessageType.SUCCESS;
		});
	}

	@Override
	public List<ViewGroup<FluidView.Data>> getGroups(Accessor<?> accessor) {
		var tank1 = new ViewGroup<>(List.of(new FluidView.Data(JadeFluidObject.of(Fluids.LAVA, 1000), 2000)));
		tank1.id = "1";
		var tank2 = new ViewGroup<>(List.of(
				new FluidView.Data(JadeFluidObject.of(Fluids.WATER, 500), 2000),
				new FluidView.Data(JadeFluidObject.empty(), 2000)));
		// tank2.id = "2";
		return List.of(tank1, tank2, tank2, tank2, tank2);
	}
}
