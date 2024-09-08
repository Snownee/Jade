package snownee.jade.addon.access;

import java.util.Objects;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;

public class EntityDetailsBodyProvider implements IEntityComponentProvider {
	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!config.get(JadeIds.ACCESS_ENTITY_DETAILS)) {
			return;
		}
		Entity entity = accessor.getEntity();
		int poseId = entity.getPose().id();
		if (entity instanceof TamableAnimal animal && animal.isInSittingPose()) {
			poseId = Pose.SITTING.id();
		} else if (entity instanceof Fox fox) {
			if (fox.isSleeping()) {
				poseId = Pose.SLEEPING.id();
			} else if (fox.isSitting()) {
				poseId = Pose.SITTING.id();
			}
		} else if (entity instanceof Axolotl axolotl && axolotl.isPlayingDead()) {
			poseId = 1000;
		} else if (entity instanceof Armadillo armadillo && armadillo.getState() == Armadillo.ArmadilloState.ROLLING) {
			poseId = 1001;
		}
		if (poseId != Pose.STANDING.id()) {
			String key = "jade.access.entity.pose.%s".formatted(poseId);
			if (I18n.exists(key)) {
				tooltip.add(Component.translatable("jade.access.entity.pose", Component.translatable(key)));
			}
		}
		if (entity instanceof Leashable leashable && leashable.isLeashed()) {
			Entity holder = leashable.getLeashHolder();
			if (holder instanceof LeashFenceKnotEntity knot) {
				BlockState blockState = Objects.requireNonNull(knot.level()).getBlockState(knot.blockPosition());
				tooltip.add(Component.translatable("jade.access.entity.leashed_to", blockState.getBlock().getName()));
			} else if (holder != null) {
				tooltip.add(Component.translatable("jade.access.entity.leashed_to", holder.getName()));
			}
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.ACCESS_ENTITY_DETAILS_BODY;
	}

	@Override
	public boolean isRequired() {
		return true;
	}
}
