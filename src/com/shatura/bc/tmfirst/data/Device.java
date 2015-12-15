package com.shatura.bc.tmfirst.data;

import ru.sns.util.colls.idents.IntegerIdentifiable;

public interface Device extends IntegerIdentifiable {

  String getSerNum();
  Ingredient getIngredient();

}
