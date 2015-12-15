package com.shatura.bc.tmfirst.data;

import ru.sns.util.colls.idents.IntegerIdentifiable;

public interface Session extends IntegerIdentifiable {

  Device getDevice();
  Depart getDepart();

}
