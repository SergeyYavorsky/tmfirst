package com.shatura.bc.tmfirst.data.db;

import ru.sns.obj.db.IDNamedEntityCache;
import com.shatura.bc.tmfirst.data.db.factories.PersonFactory;

public class PersonCache<Person> extends IDNamedEntityCache {

  public PersonCache(PersonFactory fact) {
    super(fact);
  }

  /*
  private void test(Ingredient ingred) {
  }
  */

}
