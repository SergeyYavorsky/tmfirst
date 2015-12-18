package com.shatura.bc.tmfirst.data.db;

import java.util.SortedSet;
import java.util.SortedMap;
import com.shatura.bc.tmfirst.data.RestsInfoRecord;
import com.shatura.bc.tmfirst.data.StockTask;
import com.shatura.bc.tmfirst.data.DictObjStoreTypePair;

public interface IngredOnStockInfo {

  SortedSet<RestsInfoRecord> getRests();
  SortedSet<String> getCells();
  SortedMap<StockTask.ID, SortedMap<DictObjStoreTypePair, Number>> getActTasksUsages();
  String getSP(); //складская программа

}
