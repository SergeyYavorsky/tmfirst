package com.shatura.bc.tmfirst.logic.acts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.shatura.bc.tmfirst.data.*;
import com.shatura.bc.tmfirst.logic.DBOpers;
import com.shatura.bc.tmfirst.logic.UserQuery;
import com.shatura.bc.tmfirst.logic.ex.LogicException;
import com.shatura.bc.tmfirst.logic.states.*;

abstract class ProcessScannedCodeAbstractAction extends AbstractAction {

  private static final Log log = LogFactory.getLog(ProcessScannedCodeAbstractAction.class);

  protected ProcessScannedCodeAbstractAction() {}

  public abstract String getName();

  protected abstract TerminalSessionState processCancel(TerminalSessionState prevState) throws LogicException;

  protected abstract TerminalSessionState processValidStockLabelData(TerminalSessionState prevState, StockLabelData sld) throws LogicException;

  protected abstract TerminalSessionState processValidStockCellData(TerminalSessionState prevState, StockCellData sld) throws LogicException;

  private final TerminalSessionState processAlternative(TerminalSessionState prevState, String altString) throws LogicException {
    //return new AbstractAlternativeState(prevState, altString);
    return new AnyScanAltState(prevState);
  }

  final public TerminalSessionState perform(TerminalSessionState prevState, UserQuery.BarCode bc) throws LogicException {

    if ( bc.isAlternativeSelected() ) {
      String altStr = bc.getAlternativeData();
      log.info("вместо ШК хотят чего-то другого: " + altStr);
      return processAlternative(prevState, altStr);
    }

    String enteredData = bc.getBarCode();

    BarCodeData bcd;// = null;

    if ( enteredData != null && ( !enteredData.trim().equals("") ) ) {
      bcd = DBOpers.getDBOpers().registerBC(
        this, prevState.getTerminalSession().getContext().getSession().getID(), enteredData, true
      );
    } else {

      log.info("bcd пустой");
      return processCancel(prevState);
    }

    log.info("сканировано bcd=" + bcd + "~~~");

    if ( bcd instanceof ErrorData  ) {
      return new ShowMessageNotifySessionState(
        prevState,
        "ошибка:\n" + ((ErrorData) bcd).getErrorText(),
        true
      );
    }

    if ( bcd instanceof PersonData  ) {
      prevState.getTerminalSession().getContext().setCurrPersonData((PersonData) bcd);
      return prevState;
    }

    if ( bcd instanceof TTNData ) {
      return new ShowTTNDataState(prevState, (TTNData) bcd);
    }

    if ( bcd instanceof StockTaskData ) {
     log.info("This is StockTaskData ");

      return processStockTaskData(prevState, (StockTaskData) bcd);
    }

    if ( bcd instanceof StockLabelData ) {
      StockLabelData sld = (StockLabelData) bcd;

      // эта "магия" нужна для того, чтобы вылетело исключение, если у этой ТМЦ ЕИ не сканируется...
      sld.getIngredient().getTranslatedIngred();

      return processValidStockLabelData(prevState, sld);
    }

    if (bcd instanceof StockCellData) {
        StockCellData sld = (StockCellData) bcd;
        return processValidStockCellData(prevState, sld);
        /*return new ShowMessageNotifySessionState(
                prevState,
                "странное:\n" + bcd,
                true
                ); */
    }

    return new ShowMessageNotifySessionState(
      prevState,
      "странное:\n" + bcd,
      true
    );

  }

  private TerminalSessionState processStockTaskData(TerminalSessionState prevState, StockTaskData stockTaskData) throws LogicException {


    /** @todo тут нужно обработать вход в режим сканирования по заказу или вообще заранее */


    StockTask st = stockTaskData.getStockTask();

    String abortMessage = StockTask.Utils.getDenyMessage(st);

    if ( abortMessage == null && prevState.getTerminalSession().getContext().getCurrPersonData() == null ) {
      abortMessage = "\nнет ФИО!";
    }
    log.info(abortMessage);
    if ( abortMessage != null ) {
      return new ShowMessageNotifySessionState(
        //new MainMenuSessionState(prevState.getTerminalSession()), // убрал, т.к. из за ошибки вылетаем из сканирования задания
        prevState,
        st.getID() + "\n" + abortMessage,
        true
      );
    }

    return new YesNoSessionState(
      "сканируем\n" + st.getID() + "?",
      prevState,
      new TaskScanState(prevState.getTerminalSession(), stockTaskData)
    );
  }

}
