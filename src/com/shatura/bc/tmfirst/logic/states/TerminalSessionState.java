package com.shatura.bc.tmfirst.logic.states;

import ru.sns.obj.NamedEntity;
import com.shatura.bc.tmfirst.TerminalSession;
import com.shatura.bc.tmfirst.logic.UserQuery;
import com.shatura.bc.tmfirst.logic.ex.LogicException;

public interface TerminalSessionState extends NamedEntity, TerminalSession.Listener {

  public static class Ask {
    public final String labelTxt;
    public final int fldMaxLen;

    protected Ask(String labelTxt, int fldMaxLen) {
      this.labelTxt = labelTxt;
      this.fldMaxLen = fldMaxLen;
    }

    public String toString() {
      return "labelTxt=" + labelTxt + ", fldMaxLen=" + fldMaxLen;
    }
  }

  String getName();

  TerminalSession getTerminalSession();

  UserQuery show() throws java.io.IOException, LogicException;
  TerminalSessionState processData(UserQuery answer) throws LogicException;

}
