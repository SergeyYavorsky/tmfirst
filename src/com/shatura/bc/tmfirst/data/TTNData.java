package com.shatura.bc.tmfirst.data;

import java.util.Date;

public interface TTNData extends BarCodeData {
  int getTTNID();
  String getNumber();
  Date getDate();
}
