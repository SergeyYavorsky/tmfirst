package com.shatura.bc.tmfirst.logic.states;

import com.shatura.bc.tmfirst.TerminalSession;
import com.shatura.bc.tmfirst.logic.UserQuery;

public abstract class NotifySessionState extends AbstractTerminalSessionState {

  //private static final Log log = LogFactory.getLog(ErrorTerminalSessionState.class);

  protected final TerminalSessionState prevState;
  private final boolean doErrorBell;

  //private boolean scrollMode = false;
  protected UserQuery.EscKey myUQ;

  public NotifySessionState(TerminalSessionState prevState, boolean doErrorBell) {
    super(prevState != null ? prevState.getTerminalSession() : null);
    this.prevState = prevState;
    this.doErrorBell = doErrorBell;
  }

  public NotifySessionState(TerminalSession ts, boolean doErrorBell) {
    super(ts);
    this.prevState = null;
    this.doErrorBell = doErrorBell;
  }

  @Override
  public UserQuery show() throws java.io.IOException {
    if ( myUQ == null ) {

      //ts.clearStrings(); -- нельзя!

      if ( doErrorBell ) ts.errorBell();

      StringBuilder sb = new StringBuilder();
      if ( prevState == null ) {
        sb.append(
          "Вы отключены!\nнажимайте ESC..."
        );
      }

      //ts.

      ts.addString(sb.toString());

      ts.toTerminal();

      myUQ = new UserQuery.EscKey();

    } else {

      if ( myUQ.isScrollNeeded() ) {
        ts.scrollVert(myUQ.getScroll());
        /*
        int linesCount = myUQ.isScrollUp() ? -1 : 1;
        ts.scrollVert(linesCount);
        */
      }
    }

    return myUQ;
  }

  public abstract boolean willReturn2PrevState(UserQuery answer);

  @Override
  public final TerminalSessionState doActionByData(UserQuery answer)  {

    if ( myUQ.isScrollNeeded() ) {
      //scrollMode = true;
      return this;
    }

    TerminalSessionState nextState = null;
    if ( willReturn2PrevState(answer) ) {
      nextState = prevState;
    }
    ts.setListener(nextState);
    return nextState;
  }

}
