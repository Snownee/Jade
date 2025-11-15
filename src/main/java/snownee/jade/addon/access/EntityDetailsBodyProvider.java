package snownee.jade.addon.access;

import java.util.List;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import snownee.jade.JadeClient;
import snownee.jade.addon.core.DistanceProvider;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.ui.TextElement;

public class EntityDetailsBodyProvider implements IEntityComponentProvider {
	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!config.get(JadeIds.ACCESS_ENTITY_DETAILS)) {
			return;
		}
		Entity entity = accessor.getEntity();
		int poseId = getPoseId(entity);
		if (poseId != Pose.STANDING.id()) {
			String key = "jade.access.entity.pose.%s".formatted(poseId);
			if (I18n.exists(key)) {
				tooltip.add(Component.translatable("jade.access.entity.pose", Component.translatable(key)));
			}
		}
		int passengers = entity.getPassengers().size();
		if (passengers > 0) {
			tooltip.add(JadeClient.format("jade.access.entity.passengers", passengers));
		}
		if (entity instanceof Leashable leashable) {
			if (leashable.isLeashed()) {
				Entity holder = leashable.getLeashHolder();
				if (holder instanceof LeashFenceKnotEntity knot) {
					TextElement text = DistanceProvider.xyz(knot.blockPosition());
					tooltip.add(JadeUI
							.text(Component.translatable("jade.access.entity.leashed_to", text.getString()))
							.narration(Component.translatable("jade.access.entity.leashed_to", text.getString())));
				} else if (holder != null) {
					tooltip.add(Component.translatable("jade.access.entity.leashed_to", holder.getName()));
				}
			}
			List<Leashable> leashables = Leashable.leashableLeashedTo(entity);
			if (!leashables.isEmpty()) {
				tooltip.add(Component.translatable(
						"jade.access.entity.is_leashing",
						ComponentUtils.formatList(
								leashables,
								Component.literal(ComponentUtils.DEFAULT_SEPARATOR_TEXT),
								$ -> ((Entity) $).getDisplayName())));
			}
		}
	}

	private static int getPoseId(Entity entity) {
		int poseId = entity.getPose().id();
		switch (entity) {
			case TamableAnimal animal when animal.isInSittingPose() -> poseId = Pose.SITTING.id();
			case Panda panda when panda.isSitting() -> poseId = Pose.SITTING.id();
			case Camel camel when camel.isCamelSitting() -> poseId = Pose.SITTING.id();
			case Fox fox -> {
				if (fox.isSleeping()) {
					poseId = Pose.SLEEPING.id();
				} else if (fox.isSitting()) {
					poseId = Pose.SITTING.id();
				}
			}
			case Axolotl axolotl when axolotl.isPlayingDead() -> poseId = 1000;
			case Armadillo armadillo when armadillo.getState() == Armadillo.ArmadilloState.ROLLING -> poseId = 1001;
			default -> {
			}
		}
		return poseId;
	}

	@Override
	public Identifier getUid() {
		return JadeIds.ACCESS_ENTITY_DETAILS_BODY;
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public int getDefaultPriority() {
		return 3333;
	}
}
