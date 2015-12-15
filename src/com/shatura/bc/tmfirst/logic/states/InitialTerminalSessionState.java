package com.shatura.bc.tmfirst.logic.states;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.shatura.bc.tmfirst.TerminalSession;
import com.shatura.bc.tmfirst.logic.UserQuery;
import com.shatura.bc.tmfirst.logic.acts.AbstractAction;
import com.shatura.bc.tmfirst.logic.ex.LogicException;

public final class InitialTerminalSessionState extends AbstractTerminalSessionState {

  private static final Log log = LogFactory.getLog(InitialTerminalSessionState.class);

  public InitialTerminalSessionState(TerminalSession ts) {
    super(ts);
  }

  public String getName() { return "начальное состояние"; }

  @Override
  public UserQuery show() throws java.io.IOException {
    //ts.getIO().eraseScreen();
    return null;
  }

  @Override
  public TerminalSessionState doActionByData(UserQuery answer) throws LogicException {
    return AbstractAction.getGoToMainMenuAction().perform(this);
  }

}
