package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.state.properties.NoteBlockInstrument;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import snownee.jade.VanillaPlugin;

public class NoteBlockProvider implements IComponentProvider {

	public static final NoteBlockProvider INSTANCE = new NoteBlockProvider();

	private static final String[] PITCH = { "F♯/G♭", "G", "G♯/A♭", "A", "A♯/B♭", "B", "C", "C♯/D♭", "D", "D♯/E♭", "E", "F" };
	private static final TextFormatting[] OCTAVE = { TextFormatting.WHITE, TextFormatting.YELLOW, TextFormatting.GOLD };

	@Override
	public void append(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.NOTE_BLOCK)) {
			return;
		}
		BlockState state = accessor.getBlockState();
		int note = state.get(NoteBlock.NOTE);
		String pitch = PITCH[note % PITCH.length];
		TextFormatting octave = OCTAVE[note / PITCH.length];
		NoteBlockInstrument instrument = state.get(NoteBlock.INSTRUMENT);
		tooltip.add(new TranslationTextComponent("%s %s", new TranslationTextComponent("jade.instrument." + instrument.getString()), octave + pitch));
	}

}
