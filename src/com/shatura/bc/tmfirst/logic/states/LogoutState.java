package com.shatura.bc.tmfirst.logic.states;

import com.shatura.bc.tmfirst.TerminalSession;
import com.shatura.bc.tmfirst.logic.UserQuery;

public class LogoutState extends NotifySessionState {

  //private static final Log log = LogFactory.getLog(ErrorTerminalSessionState.class);

  private final String message;

  public LogoutState(String message, TerminalSession ts) {
    super(ts, true);
    this.message = message;
  }

  final public String getName() { return "уведомление об отключении"; }

  @Override
  final public UserQuery show() throws java.io.IOException {
    ts.clearStrings();
    ts.addString(message);
    return super.show();
  }

  @Override
  final public boolean willReturn2PrevState(UserQuery answer) {
    return false;
  }

}
