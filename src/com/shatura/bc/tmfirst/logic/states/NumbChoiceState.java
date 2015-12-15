package com.shatura.bc.tmfirst.logic.states;

import java.util.Map;
import java.util.SortedMap;
import com.shatura.bc.tmfirst.logic.UserQuery;
import com.shatura.bc.tmfirst.logic.ex.LogicException;

public class NumbChoiceState extends AbstractTerminalSessionState {

  //private static final Log log = LogFactory.getLog(ErrorTerminalSessionState.class);

  protected final TerminalSessionState prevState;
  protected final String header;
  protected final String footer;
  protected final SortedMap<String, TerminalSessionState> choices;

  public NumbChoiceState(TerminalSessionState prevState, String header, String footer, SortedMap<String, TerminalSessionState> choices) {
    super(prevState != null ? prevState.getTerminalSession() : null);
    this.prevState = prevState;
    this.header = header;
    this.footer = footer;
    this.choices = choices;
  }

  @Override
  public UserQuery show() throws java.io.IOException {
    ts.clearStrings();
    if ( header != null ) ts.addString(header);

    int i = 1;
    for ( Map.Entry<String, TerminalSessionState> e : choices.entrySet() ) {
      ts.addString(String.valueOf(i++) + "-" + e.getKey());
    }

    if ( footer != null ) ts.addString(footer);

    ts.toTerminal();

    return new UserQuery.UserNumbChoice(choices.size());
  }

  public String getName() { return NumbChoiceState.class.toString(); }

  @Override
  public final TerminalSessionState doActionByData(UserQuery answer) throws LogicException {
    UserQuery.UserNumbChoice unc = (UserQuery.UserNumbChoice) answer;
    Integer choice = unc.getChoice();
    if ( choice == null ) return prevState;

    int i = 1;
    for ( Map.Entry<String, TerminalSessionState> e : choices.entrySet() ) {
      if ( i++ == choice ) return e.getValue();
    }

    throw new Error("не найдено состояние для choice=" + choice);

  }

}
