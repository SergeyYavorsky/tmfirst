package com.shatura.bc.tmfirst.data.db;

import com.shatura.bc.tmfirst.data.db.factories.CellFactory;
import ru.sns.obj.db.IDNamedEntityCache;

public class CellCache<Cell> extends IDNamedEntityCache {

  public CellCache(CellFactory fact) {
    super(fact);
  }

  /*
  private void test(Ingredient ingred) {
  }
  */

}
