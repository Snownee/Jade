package snownee.jade.api.view;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import snownee.jade.api.IJadeProvider;

public interface IServerExtensionProvider<IN, OUT> extends IJadeProvider {

	@Nullable
	List<ViewGroup<OUT>> getGroups(ServerPlayer player, ServerLevel world, IN target, boolean showDetails);

}
