package snownee.jade.util;

import com.google.common.base.Objects;

import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.ITextComponent;

public class HackyTextComponentNBT extends StringNBT {

	private static final String ALTERNATIVE = "<Unsupported Component>";

	private final ITextComponent textComponent;

	public HackyTextComponentNBT(ITextComponent textComponent) {
		super(ALTERNATIVE);
		this.textComponent = textComponent;
	}

	public ITextComponent getTextComponent() {
		return textComponent;
	}

	@Override
	public StringNBT copy() {
		return new HackyTextComponentNBT(textComponent);
	}

	@Override
	public int hashCode() {
		return textComponent.hashCode();
	}

	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != HackyTextComponentNBT.class) {
			return false;
		}
		return Objects.equal(((HackyTextComponentNBT) that).textComponent, textComponent);
	}

}
