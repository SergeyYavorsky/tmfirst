package com.shatura.bc.tmfirst.logic.states;

import com.shatura.bc.tmfirst.logic.UserQuery;

public class YesNoSessionState extends AbstractTerminalSessionState {

  //private static final Log log = LogFactory.getLog(ErrorTerminalSessionState.class);

  protected final TerminalSessionState noState;
  protected final TerminalSessionState yesState;
  private final String message;
  private final boolean doWarnBell;
  private final int timeoutInSec;
  private final boolean defNo;

  public YesNoSessionState(String message, TerminalSessionState noState, TerminalSessionState yesState) {
    this(message, noState, yesState, false);
  }

  public YesNoSessionState(String message, TerminalSessionState noState, TerminalSessionState yesState, boolean doWarnBell) {
    this(message, noState, yesState, doWarnBell, 0, false);
  }

  public YesNoSessionState(String message, TerminalSessionState noState, TerminalSessionState yesState, boolean doWarnBell, int timeoutInSec, boolean defNo) {
    super(noState != null ? noState.getTerminalSession() : yesState.getTerminalSession());
    this.message = message;
    this.yesState = yesState;
    this.noState = noState;
    this.doWarnBell = doWarnBell;
    this.timeoutInSec = timeoutInSec;
    this.defNo = defNo;
  }

  public String getName() { return "вопрос"; }

  @Override
  public UserQuery show() throws java.io.IOException {
    if ( doWarnBell ) ts.warnBell();
    ts.clearStrings();
    ts.addString(message);
    ts.toTerminal();
    /** @todo implement timeout!!! */
    return new UserQuery.YesOrNo();
  }

  @Override
  public final TerminalSessionState doActionByData(UserQuery answer) {

    UserQuery.YesOrNo yna = (UserQuery.YesOrNo) answer;
    return yna.isYes() ? yesState : noState;
  }

}
