package com.shatura.bc.tmfirst.logic.states;

import com.shatura.bc.tmfirst.logic.UserQuery;

public class IdleTimeExceededState extends NotifySessionState {

  //private static final Log log = LogFactory.getLog(ErrorTerminalSessionState.class);

  public IdleTimeExceededState(TerminalSessionState prevState) {
    super(prevState, true);
  }

  public String getName() { return "предупреджение о бездействии"; }

  @Override
  public UserQuery show() throws java.io.IOException {
    ts.clearStrings();
    ts.addString(
      "Сервер предупреждает Вас о долгом бездействии..."
    );
    return super.show();
  }

  @Override
  public boolean willReturn2PrevState(UserQuery answer) {
    return true;
  }

}
