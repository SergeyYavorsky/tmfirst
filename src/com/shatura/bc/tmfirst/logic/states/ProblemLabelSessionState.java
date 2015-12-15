package com.shatura.bc.tmfirst.logic.states;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.shatura.bc.tmfirst.data.StockLabelData;
import com.shatura.bc.tmfirst.data.StockTask;
import com.shatura.bc.tmfirst.logic.UserQuery;
import com.shatura.bc.tmfirst.logic.acts.AbstractAction;
import com.shatura.bc.tmfirst.logic.ex.LogicException;

public class ProblemLabelSessionState extends AbstractTerminalSessionState {

  private static final Log log = LogFactory.getLog(ProblemLabelSessionState.class);

  private final StockTask.ProcResult procRes;

  private final TaskScanState tss;
  private final StockLabelData sld;

  public ProblemLabelSessionState(StockTask.ProcResult procRes, TaskScanState tss, StockLabelData sld) {
    super(tss.getTerminalSession());
    this.procRes = procRes;
    this.tss = tss;
    this.sld = sld;
  }

  public String getName() { return "вопрос при работе с заданием " + tss + ": " + procRes.getMessage(); }


  public StockTask.ProcResult getProblem() { return procRes; }
  public TaskScanState getTaskScanState() { return tss; }

  @Override
  public UserQuery show() throws java.io.IOException {
    ts.warnBell();
    ts.clearStrings();

    String timStr = sld.getTIM() != null ? "\nмод " + sld.getTIM().getID() + " (" + sld.getTIM().getName() + ")" : "";

    ts.addString(
      "¬ " + tss.getStockTaskData().getStockTask().getID() + "\n<" +
      sld.getIngredient().getID() + ">" + sld.getIngredient().getName() + timStr +
      "\nуник:" + /*sld.getDepart().getID() + "~" +*/ sld.getUniqComp()
    );

    ts.addString(procRes.getMessage());
    ts.toTerminal();
    return new UserQuery.YesOrNo();
  }

  @Override
  public final TerminalSessionState doActionByData(UserQuery answer) throws LogicException {

    UserQuery.YesOrNo yna = (UserQuery.YesOrNo) answer;

    if ((this.procRes.getAlias() == 'a') && yna.isYes() ) {
      log.info("извлекаю " + sld);
      return AbstractAction.getProcessScannedCodeAction4StockTask().processValidStockLabelData(this, sld);
    } else {
      log.info("оставл€ю " + sld);
      return tss;
    }
    /*if ( yna.isYes() ) {
      log.info("извлекаю " + sld);
      return AbstractAction.getProcessScannedCodeAction4StockTask().processValidStockLabelData(this, sld);
    } else {
      log.info("оставл€ю " + sld);
      return tss;
    //}     */


  }

}
