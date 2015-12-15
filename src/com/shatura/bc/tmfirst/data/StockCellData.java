package com.shatura.bc.tmfirst.data;

import java.sql.SQLException;

public interface StockCellData extends BarCodeData {

  Cell getCell();
  String getCellContent() throws SQLException;

}
