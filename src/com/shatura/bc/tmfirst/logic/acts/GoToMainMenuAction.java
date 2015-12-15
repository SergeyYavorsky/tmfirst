package com.shatura.bc.tmfirst.logic.acts;

import net.wimpi.telnetd.net.ConnectionData;
import com.shatura.bc.tmfirst.data.Session;
import com.shatura.bc.tmfirst.logic.DBOpers;
import com.shatura.bc.tmfirst.logic.ex.LogicException;
import com.shatura.bc.tmfirst.logic.states.MainMenuSessionState;
import com.shatura.bc.tmfirst.logic.states.TerminalSessionState;

public class GoToMainMenuAction extends AbstractAction {

  public GoToMainMenuAction() {}

  public String getName() { return "регистрация сессии в БД"; }

  public TerminalSessionState perform(TerminalSessionState tss) throws LogicException {
    //System.out.println("идём в главное меню!!!");

    ConnectionData cd = tss.getTerminalSession().getConnectionData();

    Session sess = DBOpers.getDBOpers().registerSession(
      this,
      cd.getHostName(), cd.getHostAddress(), cd.getPort(),
      cd.getSocket().getLocalAddress().getHostName(),
      cd.getSocket().getLocalAddress().getHostAddress(),
      cd.getSocket().getLocalPort()
    );

    tss.getTerminalSession().getContext().setSession(sess);

    return new MainMenuSessionState(tss.getTerminalSession());
  }

}
