package com.shatura.bc.tmfirst.logic.states;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import com.shatura.bc.tmfirst.data.TTNData;
import com.shatura.bc.tmfirst.logic.UserQuery;

public class ShowTTNDataState extends NotifySessionState {

  private static final DateFormat TTN_TIME_FORMAT = new SimpleDateFormat("dd/MM/yy HH:mm");
  //private static final DateFormat TTN_TIME_FORMAT = new SimpleDateFormat("dd/MM/yy");

  //private static final Log log = LogFactory.getLog(ErrorTerminalSessionState.class);

  private final TTNData ttnData;

  public ShowTTNDataState(TerminalSessionState prevState, TTNData ttnData) {
    super(prevState, false);
    this.ttnData = ttnData;
  }

  public String getName() { return "данные ТТН"; }

  @Override
  public UserQuery show() throws java.io.IOException {
    ts.clearStrings();
    ts.addString(
      "ТТН ID:" + ttnData.getTTNID() + "\n" +
      "ном.:" + ttnData.getNumber() + "\n" +
      "от:" + TTN_TIME_FORMAT.format(ttnData.getDate())
    );
    return super.show();
  }

  @Override
  public boolean willReturn2PrevState(UserQuery answer) {
    return true;
  }

}
