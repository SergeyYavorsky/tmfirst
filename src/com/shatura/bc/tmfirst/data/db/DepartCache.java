package com.shatura.bc.tmfirst.data.db;

import ru.sns.obj.db.IDNamedEntityCache;
import com.shatura.bc.tmfirst.data.db.factories.DepartFactory;

public class DepartCache<Depart> extends IDNamedEntityCache {

  public DepartCache(DepartFactory fact) {
    super(fact);
  }

}
