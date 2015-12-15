package com.shatura.bc.tmfirst.logic.ex;

import java.sql.SQLException;
import com.shatura.bc.tmfirst.logic.acts.Action;

public class SQLProblem extends LogicException implements UserShowable, Retrievable {

  public SQLProblem(Action errAction, SQLException cause) {
    super(cause.getMessage(), errAction, cause);
  }

  public String getReadableDescription() {
    //return "Ошибка в БД.\n" + super.toString() + "\nПовторите '" + errAction.getName() + "'";
    return
      "Ош. в БД!\n" + cutShortORAMessage(getMessage()) +
      ( errAction != null ? "\nПовторите '" + errAction.getName() + "'" : "" );
  }

  private static final String ORA_ERR_KEY = "ORA-";

  private static String cutShortORAMessage(String s) {
    //System.out.println("s=" + s);
    int beg = s.indexOf(ORA_ERR_KEY);
    //System.out.println("beg=" + beg);
    if ( beg < 0 ) return s;
    beg += ORA_ERR_KEY.length();
    while ( Character.isDigit(s.charAt(beg)) ) beg++;
    while ( Character.isSpaceChar(s.charAt(beg)) || s.charAt(beg) == ':' ) beg++;
    //System.out.println("beg=" + beg);
    int end = s.indexOf(ORA_ERR_KEY, beg);
    //System.out.println("end=" + end);
    if ( end < 0 ) return s.substring(beg);
    return s.substring(beg, end);
  }


}

