package com.shatura.bc.tmfirst.data.db;

import ru.sns.obj.db.IDNamedEntityCache;
import com.shatura.bc.tmfirst.data.db.factories.DictObjFactory;
import com.shatura.bc.tmfirst.data.db.factories.StoreTypeFactory;

public class StoreTypeCache<StoreType> extends IDNamedEntityCache {

  public StoreTypeCache(StoreTypeFactory fact) {
    super(fact);
  }

}
