package com.shatura.bc.tmfirst.data.db;

import java.sql.SQLException;
import ru.sns.k3.dict.objects.K3Cache;
import com.shatura.bc.tmfirst.data.BarCodeData;
import com.shatura.bc.tmfirst.data.Session;
import com.shatura.bc.tmfirst.data.db.factories.DataFactory;

public class DataCache implements K3Cache {

  private final DataFactory fact;

  public DataCache(DataFactory fact) {
    this.fact = fact;
  }

  /**
   *
   * @param tmName String
   * @param tmAddress String
   * @param tmPort int
   * @param sName String
   * @param sAddress String
   * @param sPort int
   * @return Object[]{Integer(sessionID), String(dbName)}
   * @throws SQLException
   */
  public Session registerSession(
    String tmName, String tmAddress, int tmPort,
    String sName, String sAddress, int sPort
  ) throws SQLException {
    //fact.getPr
    synchronized ( fact.getConnection() ) {
      return fact.registerSession(tmName, tmAddress, tmPort,  sName, sAddress, sPort);
    }
  }

  public BarCodeData registerBC(final int sessionID, final String scannedBarCode, boolean doCommit) throws SQLException {
    synchronized ( fact.getConnection() ) {
      return fact.registerBC(sessionID, scannedBarCode, doCommit);
    }
  }

  /*
  public SortedSet<RestsInfoRecord> getRestsInfo(int stockID, int ingredID) throws SQLException {
    synchronized ( fact.getConnection() ) {
      return fact.getRestsInfo(stockID, ingredID);
    }
  }
  */

  public IngredOnStockInfo getIngredOnStockInfo(int stockID, final int ingredID, final Integer timID) throws SQLException {
    synchronized ( fact.getConnection() ) {
      return fact.getIngredOnStockInfo(stockID, ingredID, timID);
    }
  }

  public void dropCache() {
    //ingredsMap.clear();
    //depsMap.clear();
  }

}
