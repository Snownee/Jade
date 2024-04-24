package snownee.jade.addon.vanilla;

import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Joiner;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum NoteBlockProvider implements IBlockComponentProvider {

	INSTANCE;

	private static final String[] PITCH = {"F♯/G♭", "G", "G♯/A♭", "A", "A♯/B♭", "B", "C", "C♯/D♭", "D", "D♯/E♭", "E", "F"};
	private static final ChatFormatting[] OCTAVE = {ChatFormatting.WHITE, ChatFormatting.YELLOW, ChatFormatting.GOLD};
	private static final ChatFormatting[] OCTAVE_LIGHT = {ChatFormatting.DARK_PURPLE, ChatFormatting.DARK_BLUE, ChatFormatting.BLUE};

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		NoteBlockInstrument instrument = state.getValue(NoteBlock.INSTRUMENT);
		String key = "jade.instrument." + instrument.getSerializedName();
		String name;
		if (I18n.exists(key)) {
			name = I18n.get(key);
		} else {
			name = Joiner.on(' ').join(Stream.of(instrument.getSerializedName().replace('_', ' ').split(" "))
					.map(StringUtils::capitalize)
					.toList());
		}
		if (instrument.isTunable()) {
			int note = state.getValue(NoteBlock.NOTE);
			String pitch = PITCH[note % PITCH.length];
			ChatFormatting octave = (IThemeHelper.get().isLightColorScheme() ? OCTAVE_LIGHT : OCTAVE)[note / PITCH.length];
			tooltip.add(Component.literal("%s %s".formatted(name, octave + pitch)));
		} else {
			tooltip.add(Component.literal(name));
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_NOTE_BLOCK;
	}

}
