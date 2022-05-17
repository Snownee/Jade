package snownee.jade.addon.vanilla;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class NoteBlockProvider implements IComponentProvider {

	public static final NoteBlockProvider INSTANCE = new NoteBlockProvider();

	private static final String[] PITCH = { "F♯/G♭", "G", "G♯/A♭", "A", "A♯/B♭", "B", "C", "C♯/D♭", "D", "D♯/E♭", "E", "F" };
	private static final ChatFormatting[] OCTAVE = { ChatFormatting.WHITE, ChatFormatting.YELLOW, ChatFormatting.GOLD };

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.NOTE_BLOCK)) {
			return;
		}
		BlockState state = accessor.getBlockState();
		int note = state.getValue(NoteBlock.NOTE);
		String pitch = PITCH[note % PITCH.length];
		ChatFormatting octave = OCTAVE[note / PITCH.length];
		NoteBlockInstrument instrument = state.getValue(NoteBlock.INSTRUMENT);
		tooltip.add(new TranslatableComponent("%s %s", new TranslatableComponent("jade.instrument." + instrument.getSerializedName()), octave + pitch));
	}

}
