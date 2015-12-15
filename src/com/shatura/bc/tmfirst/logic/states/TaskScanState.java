package com.shatura.bc.tmfirst.logic.states;

import java.sql.SQLException;
import java.util.SortedSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.shatura.bc.tmfirst.TerminalSession;
import com.shatura.bc.tmfirst.data.Ingredient;
import com.shatura.bc.tmfirst.data.StockLabelData;
import com.shatura.bc.tmfirst.data.StockTask;
import com.shatura.bc.tmfirst.data.StockTaskData;
import com.shatura.bc.tmfirst.logic.DBOpers;
import com.shatura.bc.tmfirst.logic.UserQuery;
import com.shatura.bc.tmfirst.logic.acts.AbstractAction;
import com.shatura.bc.tmfirst.logic.ex.LogicException;

public class TaskScanState extends AbstractTerminalSessionState {

  private static final Log log = LogFactory.getLog(TaskScanState.class);

  private final StockTaskData std;
  private StockLabelData lastLabelData;

  public TaskScanState(TerminalSession ts, StockTaskData std) {
    super(ts);
    this.std = std;
  }

  public StockTaskData getStockTaskData() { return std; }

  public String getName() { return "сканирование задания " + std; }

  public void setLastLabelData(StockLabelData lastLabelData) {
    this.lastLabelData = lastLabelData;
  }

  @Override
  public UserQuery show() throws java.io.IOException, LogicException {
    ts.clearStrings();
    ts.addString("скан:" + std.getStockTask().getID());
    ts.addString(ts.getContext().getCurrPersonData().getPerson().getName());

    SortedSet<StockTask.RelInfoRecord> stat = null;
    StockTask.RelInfoRecord ingrStat = null;
    try {
      if ( lastLabelData != null ) {
        // Если translatedIngred не проверить в Action, то здась вылетит исключение о неправильной ЕИ
        // и сильно обидит LogicProcessor (зациклится), посему, именно в Action нужно её понюхать...
        ingrStat = std.getStockTask().getCurrStat4Ingred(lastLabelData.getIngredient().getTranslatedIngred());
      } else {
        stat = std.getStockTask().getCurrStat();
      }
    } catch ( SQLException ex ) {
      throw DBOpers.getDBOpers().new DBSQLProblem(ex);
    }

    if ( stat != null ) {
      for ( StockTask.RelInfoRecord rec : stat ) {
        ts.addString(rec.toString());
      }
    }

    if ( ingrStat != null ) {
      Ingredient ingr = lastLabelData.getIngredient().getTranslatedIngred();

      String timStr =
        lastLabelData.getTIM() != null ?
          "\nмод " + lastLabelData.getTIM().getID() + " (" + lastLabelData.getTIM().getName() + ")" : "";

      ts.addString("<" + ingr.getID() + ">" + ingr.getName() + timStr + ":");

      //lastLabelData.TIMID...
      ts.addString(ingrStat.toString());
    }

    ts.toTerminal();

    return new UserQuery.BarCode();
  }

  @Override
  public TerminalSessionState doActionByData(UserQuery answer) throws LogicException {
    UserQuery.BarCode bc = (UserQuery.BarCode) answer;
    return AbstractAction.getProcessScannedCodeAction4StockTask().perform(this, bc);
  }

}
