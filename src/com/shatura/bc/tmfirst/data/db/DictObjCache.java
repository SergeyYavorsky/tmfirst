package com.shatura.bc.tmfirst.data.db;

import ru.sns.obj.db.IDNamedEntityCache;
import com.shatura.bc.tmfirst.data.db.factories.DepartFactory;
import com.shatura.bc.tmfirst.data.db.factories.DictObjFactory;

public class DictObjCache<DictObj> extends IDNamedEntityCache {

  public DictObjCache(DictObjFactory fact) {
    super(fact);
  }

}
