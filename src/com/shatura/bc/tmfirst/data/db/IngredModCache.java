package com.shatura.bc.tmfirst.data.db;

import java.util.Map;
import java.util.WeakHashMap;
import ru.sns.obj.db.IDNamedEntityCache;
import ru.sns.util.colls.idents.IntegerIdentifiable;
import com.shatura.bc.tmfirst.data.db.factories.IngredModFactory;

public class IngredModCache<IngredMod> extends IDNamedEntityCache {

  public IngredModCache(IngredModFactory fact) {
    super(fact, (Map<Integer, IntegerIdentifiable>) new WeakHashMap<Integer, IngredMod>()); //не нравится что-то в варнингах...
    //super(fact, new WeakHashMap<Integer, IntegerIdentifiable>());
  }

}
