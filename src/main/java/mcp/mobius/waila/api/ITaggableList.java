package mcp.mobius.waila.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public interface ITaggableList<TAG, VALUE> extends List<VALUE> {

    void setTag(@Nonnull TAG tag, @Nonnull VALUE value);

    VALUE removeTag(@Nonnull TAG tag);

    @Nullable
    VALUE getTag(@Nonnull TAG tag);

    @Nonnull
    Map<TAG, VALUE> getTags();

    void absorb(@Nonnull ITaggableList<TAG, VALUE> other);
}
