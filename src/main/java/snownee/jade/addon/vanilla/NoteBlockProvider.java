package snownee.jade.addon.vanilla;

import java.util.List;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.state.properties.NoteBlockInstrument;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import snownee.jade.JadePlugin;

public class NoteBlockProvider implements IComponentProvider {

	public static final NoteBlockProvider INSTANCE = new NoteBlockProvider();

	private static final String[] PITCH = { "F♯/G♭", "G", "G♯/A♭", "A", "A♯/B♭", "B", "C", "C♯/D♭", "D", "D♯/E♭", "E", "F" };
	private static final TextFormatting[] OCTAVE = { TextFormatting.WHITE, TextFormatting.YELLOW, TextFormatting.GOLD };

	@Override
	public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
		if (!config.get(JadePlugin.NOTE_BLOCK)) {
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
