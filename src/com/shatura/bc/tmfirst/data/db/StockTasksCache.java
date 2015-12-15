package com.shatura.bc.tmfirst.data.db;

import java.sql.SQLException;
import ru.sns.k3.dict.objects.K3Cache;
import com.shatura.bc.tmfirst.data.StockTask;
import com.shatura.bc.tmfirst.data.db.factories.StockTasksFactory;

public class StockTasksCache implements K3Cache {

  private final StockTasksFactory fact;

  public StockTasksCache(StockTasksFactory fact) {
    this.fact = fact;
  }

  public void dropCache() {
  }

  public StockTask.ID getStockTaskIDForOrd(int ordID) throws SQLException {
    char[] types = fact.getTaskTypes();
    return new StockTaskID(types[0], ordID);
  }

  public StockTask.ID getStockTaskIDForDlv(int dlvID) throws SQLException {
    char[] types = fact.getTaskTypes();
    return new StockTaskID(types[1], dlvID);
  }

  public StockTask.ID getStockTaskIDForLoad(int loadID) throws SQLException {
    char[] types = fact.getTaskTypes();
    return new StockTaskID(types[2], loadID);
  }

  public StockTask getStockTask(StockTask.ID id, boolean doCommit) throws SQLException {
    synchronized ( fact.getConnection() ) {
      return fact.getStockTaskInfo(id, doCommit);
    }
  }

  public StockTask.ID getStockTaskID(char type, int id) throws SQLException {
    char[] types = fact.getTaskTypes();
    if ( type != types[0] && type != types[1] ) {
      throw new SQLException("неизвестный тип задания type=" + type);
    }
    return new StockTaskID(type, id); 
  }


  private static final class StockTaskID implements StockTask.ID {
    private final char type;
    private final int id;

    private StockTaskID(char type, int id) {
      this.type = type;
      this.id = id;
    }

    public char getType() { return type; }
    public int getID() { return id; }

    @Override
    public int hashCode() { return id; }

    @Override
    public boolean equals(Object o) {
      if ( o instanceof StockTaskID ) {
        StockTaskID ot = (StockTaskID) o;
        return this.id == ot.id && this.type == ot.type;
      }
      return false;
    }

    public int compareTo(StockTask.ID ot) {
      int ret = this.type - ot.getType();
      if ( ret == 0 ) {
        return this.id - ot.getID();
      } else {
        return ret;
      }
    }

    @Override
    public String toString() { return String.valueOf(type) + '-' + id; }
  }

}
