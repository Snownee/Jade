package snownee.jade.addon.vanilla;

import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Joiner;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;

public enum NoteBlockProvider implements IBlockComponentProvider {

	INSTANCE;

	private static final String[] PITCH = { "F♯/G♭", "G", "G♯/A♭", "A", "A♯/B♭", "B", "C", "C♯/D♭", "D", "D♯/E♭", "E", "F" };
	private static final ChatFormatting[] OCTAVE = { ChatFormatting.WHITE, ChatFormatting.YELLOW, ChatFormatting.GOLD };

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		int note = state.getValue(NoteBlock.NOTE);
		String pitch = PITCH[note % PITCH.length];
		ChatFormatting octave = OCTAVE[note / PITCH.length];
		NoteBlockInstrument instrument = state.getValue(NoteBlock.INSTRUMENT);
		String key = "jade.instrument." + instrument.getSerializedName();
		String name;
		if (I18n.exists(key)) {
			name = I18n.get(key);
		} else {
			name = Joiner.on(' ').join(Stream.of(instrument.getSerializedName().replace('_', ' ').split(" ")).map(StringUtils::capitalize).toList());
		}
		tooltip.add(new TranslatableComponent("%s %s", name, octave + pitch));
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_NOTE_BLOCK;
	}

}
