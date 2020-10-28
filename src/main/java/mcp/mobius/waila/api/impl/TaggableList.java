package mcp.mobius.waila.api.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import mcp.mobius.waila.api.ITaggableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

public class TaggableList<TAG, VALUE> extends ArrayList<VALUE> implements ITaggableList<TAG, VALUE> {

    protected final Map<TAG, VALUE> tags = Maps.newHashMap();
    private final Function<TAG, VALUE> setProcessor;

    public TaggableList(@Nonnull Function<TAG, VALUE> setProcessor) {
        Preconditions.checkNotNull(setProcessor);

        this.setProcessor = setProcessor;
    }

    @Override
    public void setTag(@Nonnull TAG tag, @Nonnull VALUE value) {
        Preconditions.checkNotNull(tag);
        Preconditions.checkNotNull(value);

        VALUE old = tags.put(tag, value);
        if (old == null)
            add(setProcessor.apply(tag));
    }

    @Override
    public VALUE removeTag(@Nonnull TAG tag) {
        Preconditions.checkNotNull(tag);

        return tags.remove(tag);
    }

    @Nullable
    @Override
    public VALUE getTag(@Nonnull TAG tag) {
        Preconditions.checkNotNull(tag);

        return tags.get(tag);
    }

    @Nonnull
    @Override
    public Map<TAG, VALUE> getTags() {
        return tags;
    }

    @Override
    public void absorb(@Nonnull ITaggableList<TAG, VALUE> other) {
        this.addAll(other);
        tags.putAll(other.getTags());
    }
}
