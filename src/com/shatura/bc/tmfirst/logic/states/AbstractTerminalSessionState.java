package com.shatura.bc.tmfirst.logic.states;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.sns.obj.NamedEntity;
import com.shatura.bc.tmfirst.TerminalSession;
import com.shatura.bc.tmfirst.logic.UserQuery;
import com.shatura.bc.tmfirst.logic.ex.LogicException;

abstract class AbstractTerminalSessionState implements NamedEntity, TerminalSessionState {

  private static final Log log = LogFactory.getLog(AbstractTerminalSessionState.class);

  protected final TerminalSession ts;

  private boolean idleNotified = false;
  private String disconnectReason = null;

  AbstractTerminalSessionState(TerminalSession ts) {
    this.ts = ts;
    log.debug("new state is " + this.getClass().getName());

    /* низзя здесь ставит листенером - не факт, что это состояние станет показываться!
    if ( ts != null ) {
      ts.setListener(this);
      log.debug("listener set");
    } else {
      log.debug("terminal session is null");
    }
    */
  }

  public final TerminalSession getTerminalSession() { return ts; }

  public abstract UserQuery show() throws java.io.IOException, LogicException;

  public abstract TerminalSessionState doActionByData(UserQuery answer) throws LogicException;

  public final TerminalSessionState processData(UserQuery answer) throws LogicException {
    log.debug(
      getName() + '.' +
      "processData(" + answer + "): idleNotified=" + isIdleNotified() + ", disconnectReason=" + getDisconnectReason()
    );

    log.debug("isIdleNotified()=" + isIdleNotified());
    if ( isIdleNotified() ) {
      idleNotified = false;
      return new IdleTimeExceededState(this);
    } else if ( getDisconnectReason() != null ) {
      return new TimeoutExceededState(ts);
    }
    return doActionByData(answer);
  }

  public final void notifyDataEnetered(char data) {
    log.fatal("недопустимый ввод данных: " + data);
  }

  private final synchronized boolean isIdleNotified() { return idleNotified; }
  private final synchronized void setIdleNotified(boolean val) {
    log.warn("setIdleNotified(" + val + ")");
    idleNotified = val;
  }

  private final synchronized String getDisconnectReason() { return disconnectReason; }
  private final synchronized void setDisconnectReason(String r) {
    disconnectReason = r;
  }

  public final void notifyIdle() {
    log.warn(getName() + ": получен сигнал о длительном бездействии");
    setIdleNotified(true);
    if ( ts != null ) ts.interruptWaiting();
  }

  public final void notifyDisconected(String reason) {
    log.warn(getName() + ": получен сигнал об отключении: " + reason);
    setDisconnectReason(reason);
    if ( ts != null ) ts.interruptWaiting();
  }

}

