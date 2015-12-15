package com.shatura.bc.tmfirst.logic.states;

import com.shatura.bc.tmfirst.TerminalSession;
import com.shatura.bc.tmfirst.data.Depart;
import com.shatura.bc.tmfirst.data.Device;
import com.shatura.bc.tmfirst.data.PersonData;
import com.shatura.bc.tmfirst.data.Session;
import com.shatura.bc.tmfirst.logic.UserQuery;
import com.shatura.bc.tmfirst.logic.acts.AbstractAction;
import com.shatura.bc.tmfirst.logic.ex.LogicException;

public class MainMenuSessionState extends AbstractTerminalSessionState {

  public MainMenuSessionState(TerminalSession ts) {
    super(ts);
  }

  public String getName() { return "������� ����"; }

  @Override
  public UserQuery show() throws java.io.IOException {
    ts.clearStrings();
    //String dbName = ts.getContext().getDBName();
    //if ( dbName != null ) ts.addString("������� ����. ��:" + dbName);

    Session s = ts.getContext().getSession();

    Depart stock = s.getDepart();
    String stockName = stock != null ? String.valueOf(stock.getID()) : null;

    Device dev = s.getDevice();

    ts.addString(
      "�:" + ( dev != null ? dev.getID() : "" ) + " " +
      "�����:" + (stockName != null ? stockName : "")
    );

    PersonData pd = ts.getContext().getCurrPersonData();
    if ( pd != null ) {
      ts.addString(pd.getPerson().getName());
    }

    ts.addString("����������:");

    ts.toTerminal();

    return new UserQuery.BarCode();
  }

  @Override
  public TerminalSessionState doActionByData(UserQuery answer) throws LogicException {
    UserQuery.BarCode bc = (UserQuery.BarCode) answer;
    return AbstractAction.getProcessScannedCodeAction4MainMenu().perform(this, bc);
  }

}
