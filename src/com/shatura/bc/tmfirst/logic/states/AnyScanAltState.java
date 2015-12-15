package com.shatura.bc.tmfirst.logic.states;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TreeMap;
import com.shatura.bc.tmfirst.data.StockTask;
import com.shatura.bc.tmfirst.logic.UserQuery;
import com.shatura.bc.tmfirst.logic.acts.AbstractAction;
import com.shatura.bc.tmfirst.logic.ex.LogicException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AnyScanAltState extends NumbChoiceState {

  private static final Log log = LogFactory.getLog(AbstractTerminalSessionState.class);

  public AnyScanAltState(TerminalSessionState prevState) {
    super(
      prevState,
      "Проверка состояния.\n",
      "Выбирайте:",
      new TreeMap<String, TerminalSessionState>()
    );

    /*
    choices.put("поставка", new DlvTaskStatusChecker());
    choices.put("заказ", new OrderTaskStatusChecker());
    */

    choices.put("поставка", new TaskStatusChecker(false));
    choices.put("заказ", new TaskStatusChecker(true));

  }

  private final class TaskStatusChecker extends AbstractTerminalSessionState {

    //private final String taskName;
    private final boolean isOrder;
    private int blockedTaskID = -1;

    private TaskStatusChecker(boolean isOrder) {
      super(AnyScanAltState.this.getTerminalSession());
      //this.taskName = taskName;
      this.isOrder = isOrder;
    }
    
    @Override
    public UserQuery show() throws java.io.IOException {
      ts.clearStrings();
      ts.addString("введите ID\n" + ( isOrder ? "заказа" : "поставки" ) + ":");
      ts.toTerminal();

      if ( blockedTaskID != -1 ) {
        return new UserQuery.UserNumbers(String.valueOf(blockedTaskID), 11);
      }

      return new UserQuery.UserNumbers(true, 11);
    }

    public String getName() { return getClass().toString(); }

    @Override
    public final TerminalSessionState doActionByData(UserQuery answer) throws LogicException {
      UserQuery.UserNumbers un = (UserQuery.UserNumbers) answer;
      
      Integer taskID = un.getEnteredNumber();

      if ( taskID == null ) return AnyScanAltState.this;

      log.debug(taskID);

      return checkTask(taskID);
    }

    protected TerminalSessionState checkTask(int taskID) throws LogicException {
      StockTask st = AbstractAction.getGetStockTaskInfoAction().getStockTask(isOrder, taskID);

      if ( st.isBlocked() ) {
        blockedTaskID = taskID;
        return new ShowMessageNotifySessionState(
          this,
          st.getID().toString() + "\nсейчас заблокировано\nможете повторить",  
          true
        );
      }

      blockedTaskID = -1;

      if ( st.isDeleted() ) {
        return new ShowMessageNotifySessionState(
          AnyScanAltState.this,
          st.getID().toString() + "\nудалено.",  
          false
        );
      }

      if ( st.getReleaseTime() != null ) {
        return new ShowMessageNotifySessionState(
          AnyScanAltState.this,
          st.getID().toString() + "\nотпущено " + DATE_FORMAT.format(st.getReleaseTime()) +
          "\nсклад:\n" + st.getRlsStock(),  
          true
        );
      }

      if ( st.getWhWrkTime() != null ) {
        return new ShowMessageNotifySessionState(
          AnyScanAltState.this,
          st.getID().toString() + "\nна скл.обр. " + DATE_FORMAT.format(st.getWhWrkTime()) +
          "\nсклад:\n" + st.getRlsStock(),  
          false
        );
      }

      return new ShowMessageNotifySessionState(
        AnyScanAltState.this,
        st.getID().toString() + "\nне передано на\nскладскую обработку!",
        true
      );

    }

  }

  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy HH:mm");

  /*
  private class OrderTaskStatusChecker extends AbsStatusChecker {
    private OrderTaskStatusChecker() { super("заказа"); }

    protected boolean isOrder() { return true; }

  }

  private class DlvTaskStatusChecker extends AbsStatusChecker {
    private DlvTaskStatusChecker() { super("поставки"); }

    protected boolean isOrder() { return false; }

  }
  */


}
