package com.shatura.bc.tmfirst.logic.acts;

import java.sql.SQLException;

import com.shatura.bc.tmfirst.data.StockCellData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.shatura.bc.tmfirst.data.StockLabelData;
import com.shatura.bc.tmfirst.data.StockTask;
import com.shatura.bc.tmfirst.data.StockTaskData;
import com.shatura.bc.tmfirst.logic.DBOpers;
import com.shatura.bc.tmfirst.logic.ex.LogicException;
import com.shatura.bc.tmfirst.logic.ex.ProgramError;
import com.shatura.bc.tmfirst.logic.states.*;

public class ProcessScannedCodeAction4StockTask extends ProcessScannedCodeAbstractAction {

  private static final Log log = LogFactory.getLog(ProcessScannedCodeAction4StockTask.class);

  public ProcessScannedCodeAction4StockTask() {}

  @Override
  public String getName() { return "сканирование в рамках задани€ на скл.обр."; }

  @Override
  protected TerminalSessionState processCancel(TerminalSessionState prevState) throws LogicException {
    TaskScanState tss = (TaskScanState) prevState;
    StockTaskData std = tss.getStockTaskData();
    tss.setLastLabelData(null);  // сбрасываем последнюю этикетку, чтобы показать статистику по заданию
    String s = "«акончить с " + std.getStockTask().getID() + "?";
    return new YesNoSessionState(s, prevState, new MainMenuSessionState(prevState.getTerminalSession()));
  }

  //processStockTaskData

  private TerminalSessionState validateTask(StockTask st, TerminalSessionState prevState) throws SQLException {
    if ( st.isBlocked() ) {
      return new ShowMessageNotifySessionState(
        prevState, st.getID() + " блокировано\nповторите!", true
      );
    }

    String abortMessage = StockTask.Utils.getDenyMessage(st);

    if ( abortMessage != null ) {
      return new ShowMessageNotifySessionState(
        //new MainMenuSessionState(prevState.getTerminalSession()), // например, скан.запрещ. - пусть пользователь сам выходит из режима
        prevState,
        st.getID() + "\n" + abortMessage,
        true
      );
    }
    return null;
  }

  @Override
  public TerminalSessionState processValidStockLabelData(final TerminalSessionState prevState, final StockLabelData sld) throws LogicException {

    TaskScanState tempState;
    StockTask.ProcResult tempRes = null;

    if ( prevState instanceof TaskScanState ) {
      tempState = (TaskScanState) prevState;
    } else if ( prevState instanceof ProblemLabelSessionState ) {
      ProblemLabelSessionState plss = (ProblemLabelSessionState) prevState;
      tempState = plss.getTaskScanState();
      tempRes = plss.getProblem();
    } else throw new ProgramError("illegal type of prevState", this);

    final TaskScanState tss = tempState;
    final StockTask st = tss.getStockTaskData().getStockTask();
    final StockTask.ProcResult prevKnownProblem = tempRes;

    tss.setLastLabelData(sld); // устанавливаем крайнюю отсканированную этикетку

    final TerminalSessionState[] retState = new TerminalSessionState[]{prevState};
    try {
      st.lockAndRefreshInfo(new StockTask.ProcessInLockedTask() {
        public void process() throws SQLException, LogicException {

          TerminalSessionState errorState = validateTask(st, prevState);
          if ( errorState != null ) {
            retState[0] = errorState;
            return;
          }

          StockTask.ProcResult pr = st.processStockLabelData(
            sld,
            prevState.getTerminalSession().getContext().getCurrPersonData().getPerson(),
            prevKnownProblem
          );

          if ( pr == null || pr == StockTask.ProcResult.SUCCESS ) {
            retState[0] = tss;
            return;
          }

          log.info("StockTask.ProcResult=" + pr);

          if ( prevKnownProblem != null )
            throw new ProgramError("prevKnownProblem=" + prevKnownProblem, ProcessScannedCodeAction4StockTask.this);

          retState[0] = new ProblemLabelSessionState(pr, tss, sld);
        }
      });

      return retState[0];

    } catch ( SQLException ex ) {
      throw DBOpers.getDBOpers().new DBSQLProblem(ex);
    }
  }

  protected TerminalSessionState processValidStockCellData(TerminalSessionState prevState, StockCellData sld) throws LogicException {
      return new ShowMessageNotifySessionState(
              prevState,"это не работает\n",
              false
      );
  }
}
