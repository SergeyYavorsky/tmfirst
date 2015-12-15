package com.shatura.bc.tmfirst.logic.acts;

import com.shatura.bc.tmfirst.data.StockTask;
import com.shatura.bc.tmfirst.logic.DBOpers;
import com.shatura.bc.tmfirst.logic.ex.LogicException;

public class GetStockTaskInfoAction extends AbstractAction {

  public String getName() { return "проверка тек.статуса задания на скл.обр."; }

  public StockTask getStockTask(boolean isOrder, int taskID) throws LogicException {
    return DBOpers.getDBOpers().getStockTask(this, isOrder, taskID);
  }

}
