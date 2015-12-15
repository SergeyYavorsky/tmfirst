package com.shatura.bc.tmfirst;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import net.wimpi.telnetd.io.BasicTerminalIO;
import net.wimpi.telnetd.io.TerminalIO;
import net.wimpi.telnetd.io.terminal.Terminal;
import net.wimpi.telnetd.io.toolkit.Editfield;
import net.wimpi.telnetd.io.toolkit.Label;
import net.wimpi.telnetd.net.Connection;
import net.wimpi.telnetd.net.ConnectionData;
import net.wimpi.telnetd.net.ConnectionEvent;
import net.wimpi.telnetd.shell.Shell;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.sns.env.ApplicationEnvironmentException;
import com.shatura.bc.tmfirst.env.Environment;
import com.shatura.bc.tmfirst.logic.LogicProcessor;
import com.shatura.bc.tmfirst.logic.SCtx;
import com.shatura.bc.tmfirst.logic.UserQuery;
import com.shatura.bc.tmfirst.logic.states.InitialTerminalSessionState;

public class TerminalSession implements Shell {

  public static interface Listener {

    //void notifyDataEntered(char data);
    void notifyIdle();
    void notifyDisconected(String reason);

  }

  private static final Log log = LogFactory.getLog(TerminalSession.class);

  private Connection conn;
  private BasicTerminalIO io;

  private final List<String> lines;

  private int width;
  private int height;

  private int upLineIndex = 0;
  private String upArrow;
  private String downArrow;

  private Listener lis;
  private final SCtx ctx;

  private TerminalSession() {
    this.lines = new ArrayList<String>(80);
    this.ctx = new SCtx();
  }

  private void genArrows() {
    StringBuilder sb = new StringBuilder(width);
    for ( int i = 0; i < width-1; i++ ) {
      sb.append(' ');
    }
    sb.append('^');
    upArrow = sb.toString();
    sb.deleteCharAt(width-1);
    sb.append('v');
    downArrow = sb.toString();
  }

  public static Shell createShell() {
    return new TerminalSession();
  }

  public void setListener(Listener lis) {
    this.lis = lis;
  }

  public SCtx getContext() { return ctx; }

  private void appendStr(String s) {
    if ( s.equals("\n") ) {
      //lines.add("");
      return;
    }

    if ( s.length() <= width ) {
      lines.add(s);
    } else {
      lines.add(s.substring(0, width));
      appendStr(s.substring(width));
    }
  }

  public void clearStrings() {
    upLineIndex = 0;
    lines.clear();
  }

  private void bell(int times) throws IOException {
    for ( int i = 0; i < times; i ++ ) io.bell();
    io.flush();
  }

  private void rejectedBell() throws IOException {
    bell(1);
  }

  public void warnBell() throws IOException {
    bell(3);
  }

  public void errorBell() throws IOException {
    bell(7);
  }

  public void scrollVert(UserQuery.SrollType st) throws IOException {
    int lines = 0;
    switch ( st ) {
      case SMALL_UP: lines = -1; break;
      case SMALL_DOWN: lines = 1; break;
      case BIG_DOWN: lines = height-2; break;
      case BIG_UP: lines = -height+2; break;
    }
    scrollVert(lines);
  }

  public void scrollVert(int linesCount) throws IOException {
    int newUpLineIndex = upLineIndex + linesCount;

    if ( newUpLineIndex < 0 ) {
      // выше строк нету
      newUpLineIndex = 0;
    } else if ( newUpLineIndex >= lines.size()-1 - height + 3 ) {
      // новая первая строка приведёт к пустым строкам, ибо больше нету
      newUpLineIndex = lines.size()-1 - height + 3;

      if ( newUpLineIndex < 0 ) newUpLineIndex = 0; 
    }

    if ( upLineIndex != newUpLineIndex ) {
      upLineIndex = newUpLineIndex;
      toTerminal();
    }

    //if ( upLineIndex + lines )
  }

  public void toTerminal() throws IOException {
    io.eraseScreen();
    io.homeCursor();
    io.flush();

    boolean first = true;
    int lastLineLen = 0;

    int currPrintedLineNum = 0;

    if ( upLineIndex > 0 ) {
      //io.write("  ^^^");
      io.write(upArrow);
      lastLineLen = width;
      currPrintedLineNum++;
      first = false;
    }

    int txtLine2Print = 0;
    for ( String s : lines ) {

      if ( txtLine2Print < upLineIndex ) {
        txtLine2Print++;
        continue;
      }

      if ( first ) {
        first = false;
      } else {
        io.write(BasicTerminalIO.CRLF);
      }

      if ( s != null ) {
        io.write(s);
        txtLine2Print++; currPrintedLineNum++;
        lastLineLen = s.length();
      }

      if ( currPrintedLineNum + 1 == height && txtLine2Print < lines.size() ) {
        io.write(BasicTerminalIO.CRLF);
        //io.write("  vvv");
        io.write(downArrow);
        lastLineLen = width;
        currPrintedLineNum++;
        break;
      }

    }

    // нельзя курсор ставить сразу после строки шириной в экран - всё погано уезжает влево
    if ( lastLineLen >= width ) {
      if ( lines.size() >= height ) {
        io.homeCursor();
      } else {
        io.write(BasicTerminalIO.CRLF);
      }
    }

    io.flush();
  }

  public void addString(String s) {
    for ( StringTokenizer st = new StringTokenizer(s, "\n", true); st.hasMoreTokens(); ) {
      String cs = st.nextToken();
      appendStr(cs);
    }
  }

  public ConnectionData getConnectionData() { return conn.getConnectionData(); }

  public Environment getApplicationEnvironment() {
    try {
      return Environment.get();
    } catch ( ApplicationEnvironmentException ex ) {
      log.fatal("не могу настройки тиснуть", ex);
      System.exit(1);
      return null;
    }
  }

  private static final String CHARSET_EPILOG = "cs:";
  private static final String CHARSET_PROLOG = ":";

  private Charset extractCharsetFromLogin(String loginStr) {
    String charsetName = null;
    int csBegPos = loginStr.indexOf(CHARSET_EPILOG);
    if ( csBegPos >= 0 ) {
      csBegPos += CHARSET_EPILOG.length();
      int csEndPos = loginStr.indexOf(CHARSET_PROLOG, csBegPos);
      charsetName = csEndPos >= 0 ? loginStr.substring(csBegPos, csEndPos-1) : loginStr.substring(csBegPos);
    }
    log.info("extracted charsetName=" + charsetName);

    if ( charsetName == null ) {
      return getApplicationEnvironment().getTerminalCharset();
    } else {
      try {
        return Charset.forName(charsetName);
      } catch ( Throwable tr ) {
        log.error("Проблема с получением charset=" + charsetName + " - не буду его использовать", tr);
        return null;
      }
    }
  }

  public String getStringFromUser() {
    try {
      Editfield ef = new Editfield(io, "login:", 10);
      ef.run();

      return ef.getValue();
    } catch ( Exception ex ) {
      //????
      return null;
    }
  }

  public void run(Connection terminalConnection) {

    conn = terminalConnection;
    io = conn.getTerminalIO();

    ConnectionData cd = conn.getConnectionData();

    //this.width = cd.getTerminalColumns(); // кажет 80
    //this.height = cd.getTerminalRows(); // кажет 25
    //System.out.println("getTerminalColumns()=" + this.width);
    //System.out.println("cd.getTerminalRows()=" + this.height);

    this.width = getApplicationEnvironment().getTerminalColumns();
    this.height = getApplicationEnvironment().getTerminalRows();
    genArrows();

    conn.addConnectionListener(this);

    try {

      io.eraseScreen(); io.homeCursor(); io.flush();

      Label lab = new Label(io, "login: ");
      lab.draw();

      Editfield ef = new Editfield(io, "login:", 10);
      ef.run();

      String login = ef.getValue();
      io.setCharset(extractCharsetFromLogin(login));

      io.write(BasicTerminalIO.CRLF);

      lab = new Label(io, "Password: ");
      lab.draw();

      ef = new Editfield(io, "password:", 10);
      ef.run();
      ef.setPasswordField(true);
      //System.out.println("password=" + m_IO.decodeString(ef.getValue()));

      io.eraseScreen(); io.homeCursor(); io.flush();

      io.bell(); io.bell(); io.bell(); io.flush();

      io.setCancellableMode(true);
      io.setNoStripCRSeqMode(true);

      LogicProcessor.process(new InitialTerminalSessionState(this));

    } catch ( Exception ex ) {
      disconnect("проблема в основном цикле ожидания ввода " + ex.getMessage(), false);
      log.error("run()", ex);
    }
    if ( lis != null ) {
      lis.notifyDisconected("что-то из цикла вышли совсем...");
    }
  }

  public void interruptWaiting() {
    conn.interrupt();
  }

  public void queryUser(UserQuery uq) throws IOException {
    try {
      uq.setTerminalSession(this);
      io.setRawReadAllowed(true);

      boolean completed = false;
      if ( uq.isShowAccepted() && uq.getPreenteredData() != null ) {
        io.write(uq.getPreenteredData());
      }

      int a1 = Integer.MAX_VALUE;
      int a2 = Integer.MAX_VALUE;
      do {

        int a;
        boolean isCode = false;

        if ( a1 != Integer.MAX_VALUE ) {
          a = a1;
          a1 = Integer.MAX_VALUE;
        } else if ( a2 != Integer.MAX_VALUE ) {
          a = a2;
          a2 = Integer.MAX_VALUE;
        } else {
          a = io.read();
          //System.out.println("read a=" + a + ", io.getAvailableCount()=" + io.getAvailableCount());

          if ( a == Terminal.ESC && io.getAvailableCount() >= 2 ) {
            a1 = io.read();
            //System.out.println("read a1=" + a1 + ", io.getAvailableCount()=" + io.getAvailableCount());
            a2 = io.read();
            //System.out.println("read a2=" + a2 + ", io.getAvailableCount()=" + io.getAvailableCount());

            if ( a1 == Terminal.LSB ) {
              switch ( a2 ) {
                case Terminal.A: a = TerminalIO.UP; break;
                case Terminal.B: a = TerminalIO.DOWN; break;
                case Terminal.C: a = TerminalIO.RIGHT; break;
                case Terminal.D: a = TerminalIO.LEFT; break;
                default: a = TerminalIO.UNRECOGNIZED; break;
              }
              a1 = a2 = Integer.MAX_VALUE;
              isCode = true;
            } else if ( a1 == 79 /*Fx-key?*/ ) { 
              // игнорируем все F-клавиши
              a = TerminalIO.UNRECOGNIZED;
              a1 = a2 = Integer.MAX_VALUE;
              isCode = true;
            }
          }
        }

        char c = isCode ? (char) a : io.decodeChar(a);
        //System.out.println("a=" + a + ", c=" + c + ", (int) c=" + ((int) c) + ", isCode=" + isCode);

        int answer = uq.acceptChar(c);
        switch ( answer ) {

          case UserQuery.REJECTED:
            this.rejectedBell();
            io.readAvailable();
            break;

          case UserQuery.COMPLETED:
            completed = true;
            break;

          case UserQuery.ACCEPTED:
            if ( uq.isShowAccepted() ) {

              //System.out.println("c=" + c);

              io.write(c);

              if ( UserQuery.Answer.isBackspace(c) ) {
                //System.out.println("стираю");
                io.write(' ');
                io.moveLeft(1);
              }

            }
            break;

          default:
            throw new Error("недопустимый ответ: answer=" + answer);

        }

      } while ( !completed );

    } finally {
      io.readAvailable();
      io.setRawReadAllowed(false);
    }

  }


  private void showMessage(final String m) throws IOException {
    this.errorBell();
    io.eraseScreen(); io.homeCursor(); io.flush();
    io.write(m);
    io.flush();
  }

  @SuppressWarnings({"EmptyCatchBlock"})
  private void disconnect(String reason, boolean doClose) {

    try{
      if ( lis != null ) {
        lis.notifyDisconected(reason);
        lis = null;
      } else {
        showMessage(reason + BasicTerminalIO.CRLF + "Вы отключены от сервера.");
      }
    } catch ( IOException ex ) {
      log.error("disconnect(" + reason + ")", ex);
    }

    if ( doClose ) {
      try { Thread.sleep(4000); } catch ( InterruptedException ex ) {}
      conn.close();
    }
  }

  public void connectionIdle(ConnectionEvent ce) {
    try{
      if ( lis != null ) {
        lis.notifyIdle();
      } else {
        showMessage("долго думаем!!!!");
      }
    } catch ( IOException ex ) {
      disconnect("внезапный обрыв связи", false);
    }
  }


  public void connectionTimedOut(ConnectionEvent ce) {
    disconnect("Превышено время бездействия.", true);
  }


  public void connectionLogoutRequest(ConnectionEvent ce) {
    disconnect("Принят запрос на отключение.", false);
  }

  public void connectionSentBreak(ConnectionEvent ce) {
    disconnect("Ошибка связи.", false);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder(lines.size() * width);
    for ( String s : lines ) {
      if ( s != null ) {
        sb.append(s);
      }
      sb.append("\n");
    }
    return sb.toString();
  }

}
