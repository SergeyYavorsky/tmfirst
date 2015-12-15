package com.shatura.bc.tmfirst.logic;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import com.shatura.bc.tmfirst.data.BarCodeData;
import com.shatura.bc.tmfirst.data.Session;
import com.shatura.bc.tmfirst.data.StockTask;
import com.shatura.bc.tmfirst.data.db.IngredOnStockInfo;
import com.shatura.bc.tmfirst.data.db.StockTasksCache;
import com.shatura.bc.tmfirst.env.Environment;
import com.shatura.bc.tmfirst.logic.acts.Action;
import com.shatura.bc.tmfirst.logic.ex.SQLProblem;

public class DBOpers {

  private static DBOpers ops;

  public static void initDBOpers(Environment env) {
    ops = new DBOpers(env);
  }

  public static DBOpers getDBOpers() { return ops; }

  private final Environment env;

  private DBOpers(Environment env) {
    this.env = env;
  }

  public void initCaches() {

    new Thread(new Runnable() {
      public void run() {
        try{
          env.getDataManager().getIngredientCache().preloadIngreds();
        } catch ( SQLException ex ) {
          log.fatal("Проблема инициализации кешей", ex);
          System.exit(33);
        }
      }
    }, "инициализатор кешей").start();
  }

  public void dropDBConnect() {
    Connection conn = env.getConnection().getConnection();
    if ( conn != null ) {
      synchronized ( conn ) {
        try {
          env.refreshAllCachesFactories(true);
        } catch ( Exception ex1 ) {
          log.warn(this, ex1);
        }
      }
    }
  }

  public void refreshDBConnect() throws SQLException {
    dropDBConnect();
    initCaches();
  }

  /**
   * @return Object[]{Integer(sessionID), String(dbName)}
   */
  public Session registerSession(
    Action inAction,
    String tmName, String tmAddress, int tmPort,
    String sName, String sAddress, int sPort
  ) throws SQLProblem {
    try{
      return env.getDataManager().getDataCache().registerSession(
        tmName, tmAddress, tmPort, sName, sAddress, sPort
      );
    } catch ( SQLException ex ) {
      throw new DBSQLProblem(inAction, ex);
    }
  }

  public BarCodeData registerBC(Action inAction, int sessionID, String scannedBarCode, boolean doCommit) throws SQLProblem {
    try {
      return env.getDataManager().getDataCache().registerBC(sessionID, scannedBarCode, doCommit);
    } catch ( SQLException ex ) {
      throw new DBSQLProblem(inAction, ex);
    }
  }

  /*
  public SortedSet<RestsInfoRecord> getRestsInfo(Action inAction, int sessionID, int stockID, int ingredID) throws SQLProblem {
    try {
      return env.getDataManager().getDataCache().getRestsInfo(stockID, ingredID);
    } catch ( SQLException ex ) {
      throw new DBSQLProblem(inAction, ex);
    }
  }
  */

  public IngredOnStockInfo getIngredOnStockInfo(Action inAction, int stockID, final int ingredID, final Integer timID) throws SQLProblem {

    try {
      return env.getDataManager().getDataCache().getIngredOnStockInfo(stockID, ingredID, timID);
    } catch ( SQLException ex ) {
      throw new DBSQLProblem(inAction, ex);
    }

  }


  public StockTask getStockTask(Action inAction, boolean isOrder, int taskID) throws SQLProblem {
    try {
      StockTasksCache stc = env.getDataManager().getStockTasksCache();
      StockTask.ID id = isOrder ? stc.getStockTaskIDForOrd(taskID) : stc.getStockTaskIDForDlv(taskID);
      return env.getDataManager().getStockTasksCache().getStockTask(id, true);
    } catch ( SQLException ex ) {
      throw new DBSQLProblem(inAction, ex);
    }
  }

  private static final Logger log = Logger.getLogger(DBOpers.class);

  public class DBSQLProblem extends SQLProblem {

    public DBSQLProblem(Action inAction, SQLException ex) {
      super(inAction, ex);
      synchronized ( env.getConnection().getConnection() ) {
        try {
          env.refreshAllCachesFactories(true);
        } catch ( Exception ex1 ) {
          log.warn(this, ex1);
        }
      }
    }

    public DBSQLProblem(SQLException ex) {
      this(null, ex);
    }

  }


}
