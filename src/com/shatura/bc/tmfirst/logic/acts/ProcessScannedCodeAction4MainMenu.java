package com.shatura.bc.tmfirst.logic.acts;

import java.sql.SQLException;
import java.util.SortedMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.shatura.bc.tmfirst.data.*;
import com.shatura.bc.tmfirst.data.db.IngredOnStockInfo;
import com.shatura.bc.tmfirst.logic.DBOpers;
import com.shatura.bc.tmfirst.logic.ex.LogicException;
import com.shatura.bc.tmfirst.logic.states.ShowMessageNotifySessionState;
import com.shatura.bc.tmfirst.logic.states.TerminalSessionState;
import com.shatura.bc.tmfirst.logic.states.YesNoSessionState;

public class ProcessScannedCodeAction4MainMenu extends ProcessScannedCodeAbstractAction {

  private static final Log log = LogFactory.getLog(ProcessScannedCodeAction4MainMenu.class);

  public ProcessScannedCodeAction4MainMenu() {}

  @Override
  public String getName() { return "сканирование в главном меню"; }

  @Override
  protected TerminalSessionState processCancel(TerminalSessionState prevState) throws LogicException {
    prevState.getTerminalSession().getContext().setCurrPersonData(null);

    return new YesNoSessionState(
      "Вы хотите завершить работу сканера?\n(потом придется его перезагрузить)",
      prevState,
      new YesNoSessionState(
        "ВЫ ХОРОШО ПОНИМАЕТЕ, ЧТО ДЕЛАЕТЕ???",
        prevState,
        new ShowMessageNotifySessionState(prevState.getTerminalSession(), "Вас предупреждали...\nВы сами этого хотели...\nпока!", true)
      ),
      true
    );
  }

  @Override
  protected TerminalSessionState processValidStockLabelData(TerminalSessionState prevState, StockLabelData sld) throws LogicException {
    Ingredient trI = sld.getIngredient().getTranslatedIngred();
    //Integer timID = sld.

    //SortedSet<RestsInfoRecord> ri = null;
    IngredOnStockInfo iosi = null;

    Session sess = prevState.getTerminalSession().getContext().getSession();

    Depart stock = sess.getDepart();
    if ( stock != null ) {

      iosi = DBOpers.getDBOpers().getIngredOnStockInfo(
        this, stock.getID(), trI.getID(),
        sld.getTIM() != null ? sld.getTIM().getID() : null
      );

      /*
      ri = DBOpers.getDBOpers().getRestsInfo(
          this,
          sess.getID(),
          stock.getID(),
          trI.getID()
      );
      */
    }

    StringBuilder sb = new StringBuilder();
    if ( stock == null ) {
      sb.append("нет склада");
    } else {

      if ( iosi.getRests().size() == 0 ) {
        sb.append("остатков нет\n");
      } else {
        for ( RestsInfoRecord rir : iosi.getRests() ) {
          sb.append(rir).append("\n");
        }
      }

      SortedMap<StockTask.ID, SortedMap<DictObjStoreTypePair, Number>> tasksUsagesMap = iosi.getActTasksUsages();
      //System.out.println("m=" + m);
      if ( tasksUsagesMap.size() > 0 ) {
        for ( Map.Entry<StockTask.ID, SortedMap<DictObjStoreTypePair, Number>> tasksUsagesMapEnt : tasksUsagesMap.entrySet() ) {
          sb.append(tasksUsagesMapEnt.getKey().toString()).append(':'); // + ":\n");

          boolean onlyOne = tasksUsagesMapEnt.getValue().size() == 1;
          if ( onlyOne ) {
            ;//sb.append(' ');
          } else {
            sb.append('\n');
          }

          for ( Map.Entry<DictObjStoreTypePair, Number> resMapEnt : tasksUsagesMapEnt.getValue().entrySet() ) {
            if ( ! onlyOne ) sb.append("  ");
            sb.append(resMapEnt.getKey().toString())
              .append(':')
              .append(resMapEnt.getValue())
              .append('\n');
          }

        }
      }

      if ( iosi.getCells().size() == 0 ) {
        sb.append("вывоз в <ЖК>");
      } else {
        //int i = 0;
        for ( String cell : iosi.getCells() ) {
          sb.append(cell).append('\n');
          //if ( i++ == 8 ) break;
          //System.out.println(cell);
        }
      }

    }

    String timStr = sld.getTIM() != null ? "\nмод " + sld.getTIM().getID() + " (" + sld.getTIM().getName() + ")" : "";

    return new ShowMessageNotifySessionState(
      prevState,
      /*"<" + sld.getIngredient().getID() + ">" + sld.getIngredient().getName() +
      (
        sld.getIngredient().getID() != trI.getID() ?
          " <" + trI.getID() + ">" + trI.getName() : ""
      )*/
      "<" + trI.getID() + ">" + trI.getName()  + timStr + "\n" + sb,
      false
    );
  }

  protected TerminalSessionState processValidStockCellData(TerminalSessionState prevState, StockCellData sld) throws LogicException {
    Session sess = prevState.getTerminalSession().getContext().getSession();
    Depart stock = sess.getDepart();
    try {
      return new ShowMessageNotifySessionState(
            prevState,"Ячейка " + sld.getCell().getName() + "\n" + sld.getCellContent(),
            false
      );
    }
    catch (Exception ex) {
      return new ShowMessageNotifySessionState(
        prevState,ex.toString(), false);
    }
  }
}
