package com.shatura.bc.tmfirst.logic;

import java.io.IOException;
import net.wimpi.telnetd.io.BasicTerminalIO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.shatura.bc.tmfirst.TerminalSession;
import com.shatura.bc.tmfirst.logic.ex.LogicException;
import com.shatura.bc.tmfirst.logic.ex.Retrievable;
import com.shatura.bc.tmfirst.logic.ex.UserShowable;
import com.shatura.bc.tmfirst.logic.states.ErrorTerminalSessionState;
import com.shatura.bc.tmfirst.logic.states.TerminalSessionState;

public class LogicProcessor {

  private static final Log log = LogFactory.getLog(LogicProcessor.class);

  public static void process(TerminalSessionState state) {

    while ( state != null ) {

      log.info("вывожу состояние " + state.getName());

      //TerminalSessionState.Ask ask;
      UserQuery query;

      try{
        query = state.show();
      } catch ( IOException ex ) {
        log.fatal("проблема связи при попытке демонстрации состояния " + state.getName(), ex);
        log.fatal("закрываю сессию");
        return;
      } catch ( LogicException exLog ) {
        log.warn("в процессе показа состояния=" + state + " случилась ошибка", exLog);
        if ( exLog instanceof UserShowable ) {
          state = new ErrorTerminalSessionState(state, null, (UserShowable) exLog);
          continue;
        } else if ( exLog instanceof Retrievable ) {
          log.warn("ошибка сказала, что на исправляемая - состояние не меням");
          continue;
        } else {
          log.fatal("ошибка сказала, что она фатальна...");
          break;
        }
      }
      log.info(
          "состояние " + state.getName() + " успешно показано и оно " +
          ( query != null ? "запрашивает данные так " + query.getClass().getName() : "ничего не запрашивает - транзитное..." )
      );

      TerminalSession ts = state.getTerminalSession();
      if ( query != null ) {
        try {
          if ( ts != null ) ts.setListener(state);
          state.getTerminalSession().queryUser(query);
        } catch ( BasicTerminalIO.ReadCancelled rc ) {
          log.warn("ожидание ввода прервано, предположительно состоянием " + state.getName());
        } catch ( IOException ex ) {
          log.fatal("проблема связи при ожидании ввода данных в состоянии " + state.getName(), ex);
          log.fatal("закрываю сессию");
          return;
        } finally {
          if ( ts != null ) ts.setListener(null);
        }

        log.info("состояние " + state.getName() + " с терминала получило данные");
      }

      try{
        state = state.processData(query);

      } catch ( LogicException ex ) {
        log.warn("в процессе обработки данных=" + query + " состоянием " + state.getName() + " случилась ошибка", ex);
        if ( ex instanceof UserShowable ) {
          state = new ErrorTerminalSessionState(state, ex.getErrAction(), (UserShowable) ex);
        } else if ( ex instanceof Retrievable ) {
          log.warn("ошибка сказала, что она исправляемая - состояние не меням");
        } else {
          log.fatal("ошибка сказала, что она фатальна...");
          state = null;
        }
      }
      log.info("следующим состоянием стало " + (state != null ? state.getName() : "никакое"));
    }

    log.info("процессор закончил работу");
  }

}

