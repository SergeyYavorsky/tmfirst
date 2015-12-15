package com.shatura.bc.tmfirst.logic;

import net.wimpi.telnetd.io.TerminalIO;
import com.shatura.bc.tmfirst.TerminalSession;
import com.shatura.bc.tmfirst.logic.ex.InvalidUserNumber;
import com.shatura.bc.tmfirst.logic.ex.LogicException;

public interface UserQuery {

  int REJECTED = 0;
  int ACCEPTED = 1;
  int COMPLETED = 2;

  static enum SrollType {
    NONE,
    SMALL_UP, SMALL_DOWN,
    BIG_UP, BIG_DOWN;

    private static SrollType getByTerminalKey(int key) {
      switch ( key ) {
        case TerminalIO.UP: return SMALL_UP;
        case TerminalIO.DOWN: return SMALL_DOWN;
        case TerminalIO.RIGHT: return BIG_DOWN;
        case TerminalIO.LEFT: return BIG_UP;
        default: return NONE;
      }
    }
  }


  void setTerminalSession(TerminalSession ts);
  int acceptChar(int c);
  boolean isShowAccepted();
  String getPreenteredData();

  static abstract class AbstractUserQuery implements UserQuery {
    private TerminalSession myTS;

    public void setTerminalSession(TerminalSession ts) {
      this.myTS = ts;
    }

    public boolean isShowAccepted() { return false; }
    public String getPreenteredData() { return null; }
  }

  static final class UQData {
    private final char[] data;
    private int dataLen = 0;

    private UQData(int maxLen) { data = new char[maxLen]; }

    private UQData(String initData, int maxLen) {
      //data = initData.toCharArray()
      data = new char[maxLen];
      char[] d = initData.toCharArray();
      System.arraycopy(d, 0, data, 0, d.length);
      dataLen = d.length;
    }

    boolean canAddChar() { return dataLen < data.length; }
    boolean hasData() { return dataLen > 0; } 

    void addChar(char c) { data[dataLen++] = c; }

    boolean removeLastChar() {
      if ( hasData() ) {
        dataLen--;
        return true;
      }
      return false;
    }

    void clean() { dataLen = 0; }

    public int getMaxLen() { return data.length; } 
    public String toString() { return String.valueOf(data, 0, dataLen); }
   
  }

  public static class Answer {
    public static boolean isCancel(int c) { return c == 27; }
    public static boolean isOk(int c) { return c == 9 || c == 32; }
    public static boolean isBackspace(int c) { return c == 8; }
  }

  public static class BarCode extends AbstractUserQuery {

    //private static final int MAX_BARCODE_LEN = 50;

    private final UQData data = new UQData(50); 

    private boolean isAlternative = false;

    public int acceptChar(int intC) {

      if ( Answer.isOk(intC) ) {
        isAlternative = true;
        return COMPLETED;
      }

      if ( ( intC == 10 || intC == 13 ) && data.hasData() ) return COMPLETED;

      if ( Answer.isCancel(intC) ) {
        data.clean();
        return COMPLETED;
      }
      /*
      if ( ! Character.isDigit(intC) ) {
        return REJECTED;
      }  */

      if ( ! data.canAddChar() )
        throw new Error("превышена макс.длина ШК: dataLen=" + data.getMaxLen());

      data.addChar((char) intC);
      /*
      System.out.println("intC=" + intC);
      System.out.println("(char) intC)=" + ((char) intC));
      System.out.println("getBarCode()=" + getBarCode());
      */
      return ACCEPTED;
    }

    private String getDataAsString() { return data.toString(); }

    public String getBarCode() { return getDataAsString(); }

    public boolean isAlternativeSelected() { return isAlternative; }
    public String getAlternativeData() { return getDataAsString(); }

  }

  public static class YesOrNo extends AbstractUserQuery {
    private Boolean answer;

    public int acceptChar(int intC) {
      /*
      if ( intC == 10 || intC == 13 )  {
        answer = Boolean.TRUE;
        return COMPLETED;
      }
      */
      if ( Answer.isOk(intC) )  {
        answer = Boolean.TRUE;
        return COMPLETED;
      }

      if ( Answer.isCancel(intC) ) {
        answer = Boolean.FALSE;
        return COMPLETED;
      }
      return REJECTED;
    }

    public Boolean isYes() { return answer; }
  }

  public static class EscKey extends AbstractUserQuery {

    //private Boolean needScrollUp = null;
    private SrollType scroll = SrollType.NONE; 

    public int acceptChar(int intC) {
      //System.out.println("intC=" + intC);

      scroll = SrollType.getByTerminalKey(intC);

      if ( scroll == SrollType.NONE ) {
        return ( Answer.isCancel(intC) ) ? COMPLETED : REJECTED;
      } else {
        return COMPLETED;
      }
    }

    public SrollType getScroll() { return scroll; }
    public boolean isScrollNeeded() { return scroll != SrollType.NONE; }

    /*
    public boolean isScrollUp() {
      if ( needScrollUp == null ) throw new Error("isScrollUp(), but needScrollUp=null");
      return needScrollUp;
    }
    */

  }

  public static class UserNumbers extends AbstractUserQuery {

    private final boolean showAccepted;
    private final UQData data;

    public UserNumbers(boolean showAccepted, int maxLen) {
      this.showAccepted = showAccepted;
      this.data = new UQData(maxLen);
    }

    public UserNumbers(String initData, int maxLen) {
      this.showAccepted = true;
      this.data = new UQData(initData, maxLen);
    }

    public boolean isShowAccepted() { return showAccepted; }
    public String getPreenteredData() {
      if ( data.hasData() ) return data.toString();
      return null;
    }

    protected boolean isAllowedDigit(int d) {
      return true;
    }

    public int acceptChar(int intC) {

      //System.out.println("intC=" + intC);

      if ( Character.isDigit(intC) && isAllowedDigit(intC) ) {
        if ( ! data.canAddChar() ) return REJECTED;
        data.addChar((char) intC);
        return ACCEPTED;
      }

      if ( Answer.isBackspace(intC) ) {
        return data.removeLastChar() ? ACCEPTED : REJECTED; 
      }

      if ( Answer.isCancel(intC) ) {
        data.clean();
        return COMPLETED; 
      }

      if ( Answer.isOk(intC) && data.hasData() ) {
        return COMPLETED;
      }

      return REJECTED;
    }

    public Integer getEnteredNumber() throws LogicException {
      if ( data.hasData() ) {
        try {
          return Integer.parseInt(data.toString());
        } catch ( NumberFormatException ex ) {
          throw new InvalidUserNumber(data.toString(), ex);
        }
      }
      return null;
    }

  }

  public static class UserNumbChoice extends UserNumbers {
    private final int maxChoices;

    public UserNumbChoice(int maxChoices) {
      super(true, 1);
      if ( maxChoices < 2 || maxChoices > 9 )
        throw new Error("недопустимое количество вариантов выбора = " + maxChoices);
      this.maxChoices = maxChoices;
    }

    protected boolean isAllowedDigit(int d) {
      /*
      boolean r = d >= (int) '1' && d < ( (int) '1' + maxChoices );
      System.out.println("r=" + r);
      return r;
      */
      return d >= (int) '1' && d < ( (int) '1' + maxChoices );
    }

    public Integer getChoice() throws LogicException {
      return getEnteredNumber();      
    }

  }


}

