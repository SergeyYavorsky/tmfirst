package com.shatura.bc.tmfirst.logic.states;

import com.shatura.bc.tmfirst.TerminalSession;

public class TimeoutExceededState extends LogoutState {

  //private static final Log log = LogFactory.getLog(ErrorTerminalSessionState.class);

  public TimeoutExceededState(TerminalSession ts) {
    super("�� ������� ����� ������ �� ������. ������ ��� ��������.", ts);
  }

}
