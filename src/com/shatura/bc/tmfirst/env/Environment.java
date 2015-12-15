package com.shatura.bc.tmfirst.env;

import java.io.IOException;
import java.nio.charset.Charset;
import net.wimpi.telnetd.TelnetD;
import org.apache.log4j.Logger;
import ru.sns.db.defined.PredefinedConnection;
import ru.sns.db.defined.apps.DatabaseApplEnv;
import ru.sns.env.ApplicationEnvironmentException;
import ru.sns.util.FileMng;
import com.shatura.bc.tmfirst.logic.DBOpers;

public class Environment extends ru.sns.k3.apps.def.env.Environment {

  private static final Logger log = Logger.getLogger(Environment.class);

  private static Environment env;

  public synchronized static Environment get() throws ApplicationEnvironmentException {
    if ( env == null ) {
      try{
        env = new Environment();
      } catch (IOException ex){
        throw new ApplicationEnvironmentException("проблема в процессе иницализации", ex);
      }
    }
    return env;
  }

  // ---------------------------------------------------------------------------

  private Manager dataManager;
  private final Charset terminalCharset;
  private final TelnetD telnetServer;

  private final int terminalColumns;
  private final int terminalRows;

  private Environment() throws ApplicationEnvironmentException, IOException {
    //java.util.Locale.setDefault(java.util.Locale.ENGLISH);

    String name = this.getStringProperty("TerminalCharset");
    this.terminalCharset = Charset.forName(name);

    int intVal;

    try {
      intVal = this.getIntProperty("TerminalColumns");
    } catch ( ApplicationEnvironmentException ex ) {
      log.error("Проблема со свойством TerminalColumns", ex);
      log.error("делаю TerminalColumns=20");
      intVal = 20;
    }
    this.terminalColumns = intVal;

    try {
      intVal = this.getIntProperty("TerminalRows");
    } catch ( ApplicationEnvironmentException ex ) {
      log.error("Проблема со свойством TerminalRows", ex);
      log.error("делаю TerminalRows=16");
      intVal = 16;
    }
    this.terminalRows = intVal;

    name = this.getStringProperty("TelnetConfigProps");
    try{
      this.telnetServer = TelnetD.createTelnetD(FileMng.getPropertiesFromClassPath(name));
    } catch ( Exception ex ) {
      throw new ApplicationEnvironmentException("Телнет сервер что-то скурвился", ex);
    }

    DBOpers.initDBOpers(this);
    DBOpers.getDBOpers().initCaches();

    this.addConnectionsListener(
      new ConnectionsListener() {

        public void notifyConnectionOpened(DatabaseApplEnv dae, PredefinedConnection conn) {
          log.info("notifyConnectionOpened(" + dae + ", " + conn + ") completed");
          DBOpers.getDBOpers().initCaches();
        }

        public void notifyConnectionClosed(DatabaseApplEnv dae, PredefinedConnection conn) {
          log.info("notifyConnectionClosed(" + dae + ", " + conn + ") completed");
        }

      }
    );
  }

  public void refreshCachesFactory(boolean dropConn, String name) {
    dataManager = null;
    super.refreshCachesFactory(dropConn, name);
  }

  public Manager getDataManager() {
    if ( dataManager == null ) dataManager = new Manager(this);
    return dataManager;
  }

  public TelnetD getTelnetD() { return telnetServer; }

  public Charset getTerminalCharset() { return terminalCharset; }

  public int getTerminalColumns() { return terminalColumns; }
  public int getTerminalRows() { return terminalRows; }

  public int getPreloadIngrCacheLimit() {
    String s = null;
    try{
      s = getApplProperties().getProperty("PreloadIngrCacheLimit");
    } catch ( IOException ex ) {
      log.fatal("проблема при извлечении настройки PreloadIngrCacheLimit", ex);
      System.exit(33);
    }

    if ( s == null ) return -1;

    try {
      return Integer.parseInt(s);
    } catch ( NumberFormatException ex ) {
      log.fatal("не число! PreloadIngrCacheLimit=" + s, ex);
      System.exit(33);
      return 0;
    }
  }

  /*
  @Override
  protected synchronized PredefinedConnection createConnection(String dbmsAlias, String connName, String connID) {
    PredefinedConnection pc = super.createConnection(dbmsAlias, connName, connID);
    log.info("createConnection(" + dbmsAlias + ", " + connName + ", " + connID + ") completed");
    return pc;
  }
  */

}

/* end of file*/
