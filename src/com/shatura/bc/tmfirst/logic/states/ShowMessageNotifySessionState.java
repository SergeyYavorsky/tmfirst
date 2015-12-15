package com.shatura.bc.tmfirst.logic.states;

import com.shatura.bc.tmfirst.TerminalSession;
import com.shatura.bc.tmfirst.logic.UserQuery;

public class ShowMessageNotifySessionState extends NotifySessionState {

  //private static final Log log = LogFactory.getLog(ErrorTerminalSessionState.class);
  private final String message;

  public ShowMessageNotifySessionState(TerminalSessionState nextState, String message, boolean doErrorBell) {
    super(nextState, doErrorBell);
    this.message = message;
  }

  public ShowMessageNotifySessionState(TerminalSession ts, String message, boolean doErrorBell) {
    super(ts, doErrorBell);
    this.message = message;
  }

  public String getName() { return "уведомление"; }

  @Override
  public UserQuery show() throws java.io.IOException {
    if ( myUQ == null ) {
      ts.clearStrings();
      ts.addString(message);
    }
    return super.show();
  }

  @Override
  public boolean willReturn2PrevState(UserQuery answer) {
    return true;
  }

}
