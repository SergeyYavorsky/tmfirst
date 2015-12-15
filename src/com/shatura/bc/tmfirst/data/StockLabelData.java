package com.shatura.bc.tmfirst.data;

public interface StockLabelData extends BarCodeData {

  Ingredient getIngredient();
  IngredMod getTIM();
  
  //Depart getDepart();
  int getUniqComp();

  boolean isTypical();

}
