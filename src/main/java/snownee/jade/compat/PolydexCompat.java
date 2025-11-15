package snownee.jade.compat;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class PolydexCompat implements RecipeLookupPlugin {
	@Override
	public RecipeLookupResult lookup(ItemStack itemStack, @Nullable Identifier specialId, boolean uses) {
		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		if (connection == null) {
			return RecipeLookupResult.FAIL;
		}
		CommandDispatcher<ClientSuggestionProvider> commands = connection.getCommands();
		if (!(commands.getRoot().getChild("polydex") instanceof LiteralCommandNode)) {
			return RecipeLookupResult.FAIL;
		}
		Identifier id = specialId != null ? specialId : itemStack.getItemHolder().unwrapKey().orElseThrow().identifier();
		return new RecipeLookupResult(
				"polydex",
				specialId != null ? 10f : 0.1f,
				(s, results) -> connection.sendCommand("polydex %s %s".formatted(uses ? "entry_usage" : "entry_result", id)));
	}
}
