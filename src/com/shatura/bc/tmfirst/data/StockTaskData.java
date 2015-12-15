package com.shatura.bc.tmfirst.data;

import ru.sns.util.colls.idents.IntegerIdentifiable;

public interface StockTaskData extends BarCodeData, IntegerIdentifiable {

  StockTask getStockTask();

}
