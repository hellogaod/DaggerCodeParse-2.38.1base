package dagger.internal.codegen.langmodel;

import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import dagger.Reusable;
import dagger.internal.codegen.base.ClearableCache;

import static com.google.common.base.Preconditions.checkNotNull;

/** Extension of {@link Elements} that adds Dagger-specific methods. */
@Reusable
public final class DaggerElements implements ClearableCache {

    private final Elements elements;
    private final Types types;

    public DaggerElements(Elements elements, Types types) {
        this.elements = checkNotNull(elements);
        this.types = checkNotNull(types);
    }

    @Override
    public void clearCache() {

    }
}
