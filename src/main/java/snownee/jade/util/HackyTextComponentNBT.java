package snownee.jade.util;

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

}
