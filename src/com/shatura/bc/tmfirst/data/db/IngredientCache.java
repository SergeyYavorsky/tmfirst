package com.shatura.bc.tmfirst.data.db;

import java.sql.SQLException;
import org.apache.log4j.Logger;
import ru.sns.env.ApplicationEnvironmentException;
import ru.sns.obj.db.IDNamedEntityCache;
import com.shatura.bc.tmfirst.data.db.factories.IngredientFactory;
import com.shatura.bc.tmfirst.env.Environment;

public class IngredientCache extends IDNamedEntityCache {

  private static final Logger log = Logger.getLogger(IngredientCache.class);

  public IngredientCache(IngredientFactory fact) throws SQLException {
    super(fact);
    //preloadIngreds();
  }

  public void preloadIngreds() throws SQLException {
    IngredientFactory fact = (IngredientFactory) this.getIDNamedEntityFactory();

    int limit = -1;

    try{
      limit = Environment.get().getPreloadIngrCacheLimit();
    } catch ( ApplicationEnvironmentException ex ) {
      log.fatal("проблема с окружением", ex);
      System.exit(33);
    }

    if ( limit == 0 ) {
      log.info("PreloadIngrCacheLimit = 0: кеш ТМЦ не грузим!");
      return;
    }

    synchronized ( fact.getConnection() ) {
      //System.out.println("preloadIngreds(): fact.getConnection()=" + fact.getConnection());
      if ( loaded.isEmpty() ) {
        loaded.putAll(fact.preloadIngreds(limit));
      }
    }
  }


}
