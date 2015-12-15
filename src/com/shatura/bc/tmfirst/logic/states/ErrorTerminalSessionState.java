package com.shatura.bc.tmfirst.logic.states;

import com.shatura.bc.tmfirst.logic.UserQuery;
import com.shatura.bc.tmfirst.logic.acts.Action;
import com.shatura.bc.tmfirst.logic.ex.Retrievable;
import com.shatura.bc.tmfirst.logic.ex.UserShowable;

public class ErrorTerminalSessionState extends NotifySessionState {

  //private static final Log log = LogFactory.getLog(ErrorTerminalSessionState.class);

  private final Action errorAction;
  private final UserShowable cause;

  public ErrorTerminalSessionState(TerminalSessionState prevState, Action errorAction, UserShowable cause) {
    super(prevState, true);
    this.errorAction = errorAction;
    this.cause = cause;
  }

  public String getName() { return "уведомление об ошибке"; }

  @Override
  public UserQuery show() throws java.io.IOException {
    ts.clearStrings();
    ts.addString(
      //"в процессе '" + errorAction.getName() + "' сл.ош.:\n"  +
      "ошибка:\n"  +
      cause.getReadableDescription()
    );
    return super.show();
  }

  @Override
  public boolean willReturn2PrevState(UserQuery answer) {
    return cause instanceof Retrievable;
  }

}
