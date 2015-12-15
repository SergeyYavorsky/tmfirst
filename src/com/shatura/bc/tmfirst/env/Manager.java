package com.shatura.bc.tmfirst.env;

import ru.sns.k3.apps.def.env.CachesFactory;
import com.shatura.bc.tmfirst.data.db.DataCache;
import com.shatura.bc.tmfirst.data.db.IngredientCache;
import com.shatura.bc.tmfirst.data.db.StockTasksCache;
import com.shatura.bc.tmfirst.data.db.IngredModCache;

public class Manager {

  private final Environment env;

  public Manager(Environment env) {
    this.env = env;
  }

  // -------------------------------------

  private final CachesFactory getCachesFactory() {
    return env.getCachesFactory();
  }

  // -------------------------------------

  public DataCache getDataCache() {
    return (DataCache) getCachesFactory().getCache(DataCache.class);
  }

  public IngredientCache getIngredientCache() {
    return (IngredientCache) getCachesFactory().getCache(IngredientCache.class);
  }

  public StockTasksCache getStockTasksCache() {
    return (StockTasksCache) getCachesFactory().getCache(StockTasksCache.class);
  }

  public IngredModCache getIngredModCache() {
    return (IngredModCache) getCachesFactory().getCache(IngredModCache.class);
  }

}

/* end of file*/
