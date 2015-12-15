package com.shatura.bc.tmfirst.data;

public interface RestsInfoRecord extends Comparable<RestsInfoRecord> {

  DictObj getDictObj();
  StoreType getStoreType();

  DictObjStoreTypePair getDictObjStoreTypePair();
  
  int getTotal();
  int getReserv();

}
