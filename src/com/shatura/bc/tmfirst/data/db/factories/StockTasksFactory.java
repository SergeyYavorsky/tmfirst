/* Made by SNS */

package com.shatura.bc.tmfirst.data.db.factories;

import java.sql.*;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.log4j.Logger;
import ru.sns.db.Utils;
import ru.sns.db.defined.PredefinedConnection;
import ru.sns.k3.dict.db.K3Factory;
import ru.sns.util.colls.idents.AbstractIdentifiable;
import com.shatura.bc.tmfirst.data.*;
import com.shatura.bc.tmfirst.data.Ingredient.TransException;
import com.shatura.bc.tmfirst.data.db.DepartCache;
import com.shatura.bc.tmfirst.logic.ex.LogicException;

public class StockTasksFactory extends K3Factory {

  private static final Logger log = Logger.getLogger(StockTasksFactory.class);

  /**
   * taskTypes[0] - ord
   * taskTypes[1] - dlv
   */
  private final char[] taskTypes = new char[]{'-', '-', '-'};

  private final DepartCache dprtCache;

  public StockTasksFactory(
    PredefinedConnection conn,
    DepartCache dprtCache
  ) {
    super(conn);
    this.dprtCache = dprtCache;
  }

  public char[] getTaskTypes() throws SQLException {
    ensureTaskTypesKnown();
    return taskTypes.clone();
  }

  private static final String GET_TASK_TYPES_CS_DEF_VAL =
    "begin\n" +
    "  :1 := Ord_WhWrk_Srv.TT_REL_ORD;\n" +
    "  :2 := Ord_WhWrk_Srv.TT_REL_DLV;\n" +
    "  :3 := BCD_Generator.TT_REL_LOAD;\n" +
    "end;";
  private static final String GET_TASK_TYPES_CS_DESC =
    "Извлекает значния констант-типов заданий на складскую обработку";

  private void ensureTaskTypesKnown() throws SQLException {
    if ( taskTypes[0] != '-' ) return; // типа, уже загружены

    Utils.lvSQLExecute(
      new PredefinedLVSQLExecutor() {
        public String getPreparedStatementName() { return "GET_TASK_TYPES_CS"; }
        public PreparedStatement prepareStatement() throws SQLException {
          CallableStatement cs = (CallableStatement) super.prepareStatement();
          cs.registerOutParameter(1, Types.VARCHAR);
          cs.registerOutParameter(2, Types.VARCHAR);
          cs.registerOutParameter(3, Types.VARCHAR);
          return cs;
        }
        public String getPreparedStatementDesc() { return GET_TASK_TYPES_CS_DESC; }
        public void executionComplete(PreparedStatement sql) throws SQLException {
          CallableStatement cs = (CallableStatement) sql;
          taskTypes[0] = cs.getString(1).charAt(0);
          taskTypes[1] = cs.getString(2).charAt(0);
          taskTypes[2] = cs.getString(3).charAt(0);
          super.executionComplete(sql);
        }
        public boolean isStatementClosable() { return true; }
      }
    );
  }

  private static boolean string2Bool(String s) {
    switch ( s.charAt(0) ) {
      case 'y': return true;
      case 'n': return false;
      default: throw new Error("string2Bool(" + s + ")");
    }
  }

  private static final String PULL_WH_WRK_TASK_INFO_CS_DEF_VAL =
      "begin\n" +
      "  BCD_Terminals_Srv.getWhWrkTaskInfo(\n" +
      "    :1, --taskType char,\n" +
      "    :2, --taskID number,\n" +
      "    :3, --taskDeleted out char,\n" +
      "    :4, --rlsTime out date,\n" +
      "    :5, --rlsStockID out number,\n" +
      "    :6, --dstStockID out number,\n" +
      "    :7, --whWrkTime out date,\n" +
      "    :8, --scansEnabled out char,\n" +
      "    :9, --isBlocked out char,\n" +
      "    :10  -- doCommit char\n" +
      "  );\n" +
      "end;";
  private static final String PULL_WH_WRK_TASK_INFO_CS_DESC =
    "Извлекает информацию о переданном задании на отгрузку и пытается его заблокировать.";

  private class LoadedStockTask extends AbstractIdentifiable implements StockTask, Loaded {

    private boolean deleted;
    private Timestamp releaseTime;
    private Depart rlsStock;
    private Depart dstStock;
    private Timestamp whWrkTime;
    private boolean scanEnabled;
    private boolean blocked;

    private LoadedStockTask(StockTask.ID id, CallableStatement cs) throws SQLException {
      super(id);
      assignData(cs);
    }

    private final void assignData(CallableStatement cs) throws SQLException {
      log.debug("cs.getString(3)="+cs.getString(3));
      this.deleted = string2Bool(cs.getString(3));
      this.releaseTime = cs.getTimestamp(4);
      int dprtID = cs.getInt(5);
      if ( ! cs.wasNull() ) this.rlsStock = (Depart) dprtCache.getEntity(dprtID);
      dprtID = cs.getInt(6);
      if ( ! cs.wasNull() ) this.dstStock = (Depart) dprtCache.getEntity(dprtID);
      this.whWrkTime = cs.getTimestamp(7);
      this.scanEnabled = string2Bool(cs.getString(8));
      this.blocked = string2Bool(cs.getString(9));
    }

    public void lockAndRefreshInfo(ProcessInLockedTask proc) throws SQLException, LogicException {
      try {
        synchronized ( getConnection() ) {
          LoadedStockTask st = getStockTaskInfo(getID(), false);
          assert st.getID().equals(getID());

          this.deleted = st.deleted;
          this.releaseTime = st.releaseTime;
          this.rlsStock = st.rlsStock;
          this.dstStock = st.dstStock;
          this.whWrkTime = st.whWrkTime;
          this.scanEnabled = st.scanEnabled;
          this.blocked = st.blocked;

          proc.process();
        }
      } finally {
        commit();
      }
    }

    public void commit() throws SQLException {
      getConnection().commit();
    }

    public StockTask.ID getID() { return (StockTask.ID) super.getIdentID(); }

    public boolean isDeleted() { return deleted; }

    public java.util.Date getReleaseTime() { return releaseTime; }

    public Depart getRlsStock() { return rlsStock; }

    public Depart getDstStock() { return dstStock; }

    public java.util.Date getWhWrkTime() { return whWrkTime; }

    public boolean isScanEnabled() { return scanEnabled; }

    public boolean isBlocked() { return blocked; }

    public SortedSet<StockTask.RelInfoRecord> getCurrStat() throws SQLException {
      synchronized ( getConnection() ) {
        if (this.getReleaseTime()==null)
            return  pullTaskStat(getID());
        else
            return pullTaskStatInc(getID());
      }
    }

    public StockTask.RelInfoRecord getCurrStat4Ingred(Ingredient ingred) throws SQLException {
      synchronized ( getConnection() ) {
        if (this.getReleaseTime()==null)
            return pullTaskStat4I(getID(), ingred.getID());
        else
            return pullTaskStat4IInc(getID(), ingred.getID());
      }
    }

    public StockTask.ProcResult processStockLabelData(
      StockLabelData sld, Person scanEmpl, StockTask.ProcResult knownPrevResult
    ) throws SQLException, Ingredient.TransException {
      synchronized ( getConnection() ) {
        return processSKU4LockedTask(this, sld, scanEmpl, knownPrevResult);
      }
    }

    @Override
    public String toString() { return Utils.toString(this); }
  }

  public LoadedStockTask getStockTaskInfo(final StockTask.ID id, final boolean doCommit) throws SQLException {
    final LoadedStockTask[] ret = new LoadedStockTask[]{null};

    Utils.lvSQLExecute(
      new PredefinedLVSQLExecutor() {
        public String getPreparedStatementName() { return "PULL_WH_WRK_TASK_INFO_CS"; }
        public PreparedStatement prepareStatement() throws SQLException {
          CallableStatement cs = (CallableStatement) super.prepareStatement();
          log.debug("String.valueOf(id.getType())="+String.valueOf(id.getType()));
          cs.setString(1, String.valueOf(id.getType()));
          log.debug("id.getID()="+id.getID());
          cs.setInt(2, id.getID());

          cs.registerOutParameter(3, Types.VARCHAR);
          cs.registerOutParameter(4, Types.TIMESTAMP);
          cs.registerOutParameter(5, Types.INTEGER);
          cs.registerOutParameter(6, Types.INTEGER);
          cs.registerOutParameter(7, Types.TIMESTAMP);
          cs.registerOutParameter(8, Types.VARCHAR);
          cs.registerOutParameter(9, Types.VARCHAR);

          cs.setString(10, doCommit ? "y" : "n");

          return cs;
        }
        public String getPreparedStatementDesc() { return PULL_WH_WRK_TASK_INFO_CS_DESC; }
        public void executionComplete(PreparedStatement sql) throws SQLException {
          CallableStatement cs = (CallableStatement) sql;
          ret[0] = new LoadedStockTask(id, cs);
          super.executionComplete(sql);
        }
        public boolean isStatementClosable() { return true; }
      }
    );

    return ret[0];
  }

  ////////////////////////////////

  private static class LoadedRelInfoRecord implements StockTask.RelInfoRecord, Loaded {

    private final int uomID;
    private final String uomName;
    private final int dispManQua;
    private final int dispBCDQua;
    private final int qua4Rel;

    private LoadedRelInfoRecord(ResultSet rs) throws SQLException {
      this.uomID = rs.getInt(1);
      this.uomName = rs.getString(2);
      this.dispManQua = rs.getInt(3);
      this.dispBCDQua = rs.getInt(4);
      this.qua4Rel = rs.getInt(5);
    }

    public int compareTo(StockTask.RelInfoRecord o) {
      LoadedRelInfoRecord ot = (LoadedRelInfoRecord) o;
      int ret = this.uomName.compareTo(ot.uomName);
      assert ret != 0 : "Попытка сравнения статистик для одинаковых ЕИ=" + this.uomName;
      return ret;
    }

    public String toString() {
      String ret = uomName + ":" + dispBCDQua + "+" + dispManQua + "~" + qua4Rel;
      if ( dispBCDQua + dispManQua == qua4Rel ) {
        ret = ret + " ВСЕ";
        if ( dispManQua == 0 )
          ret = ret + "!";
        else
          ret = ret + "\nЕщё осталось:\n";
      }
      return ret;
    }

  }

  private static class LoadedLeft2Scan implements StockTask.RelInfoRecord, Loaded {

    private final int ingredID;
    private final int left2Scan;

    private LoadedLeft2Scan(ResultSet rs) throws SQLException {
      this.ingredID = rs.getInt(1);
      this.left2Scan = rs.getInt(2);
    }

    public int compareTo(StockTask.RelInfoRecord o) {
      int ret = this.ingredID + 1000000;
      return ret;
    }

    public String toString() {
      String ret = this.ingredID + " - " + this.left2Scan + "\n";
      //ret += "\nЕщё осталось: test\n";
      return ret;
    }
  }

  private static final String PULL_TASK_STATUS_PS_DEF_VAL =
    "select t.UoM_ID, t.UoM_Name, t.Disp_Man_Qua, t.Disp_BCD_Qua, t.Qua_4_Rel\n" +
    "from (select * from bcd_tm_task_stats \n" +
      "union all \n" +
      "select * from bcd_tm_task2load_stats\n" +
      ") t\n" +
    "where t.task_Type = :1 and t.task_ID = :2\n" +
    "order by UoM_Name";
  private static final String PULL_TASK_STATUS_PS_DESC =
    "Тащит статистику исполнения указанного задания.";

  private static final String PULL_TASK_STATUS2_PS_DEF_VAL =
    "select ingred_id, qua_4_rel\n" +
      "from (select task_type, task_id, ingred_id," +
      "disp_bcd_qua, disp_man_qua, qua_4_rel\n" +
      " from BCD_TM_Task_Stats_Det " +
      "union all " +
      "select \n" +
      "task_type, task_id, ingred_id,  \n" +
      "sum(disp_bcd_qua) disp_bcd_qua, sum(disp_man_qua) disp_man_qua, sum(qua_4_rel) qua_4_rel\n" +
      "from BCD_TM_Task2load_Stats_Det t group by task_type, task_id, ingred_id) t\n" +
      "where t.task_Type = :1 and t.task_ID = :2 \n" +
      "and disp_bcd_qua < qua_4_rel\n" +
      "and rownum <=5";
  private static final String PULL_TASK_STATUS2_PS_DESC =
    "Сколько ещё грузить указанного задания.";

  private SortedSet<StockTask.RelInfoRecord> pullTaskStat(final StockTask.ID id) throws SQLException {
    final TreeSet<StockTask.RelInfoRecord> ret = new TreeSet<StockTask.RelInfoRecord>();

    Utils.lvSQLExecute(
      new PredefinedLVSQLExecutor() {
        public String getPreparedStatementName() {
            return "PULL_TASK_STATUS_PS";
        }
        public String getPreparedStatementDesc() { return PULL_TASK_STATUS_PS_DESC; }
        public PreparedStatement prepareStatement() throws SQLException {
          PreparedStatement ps = super.prepareStatement();
          ps.setString(1, String.valueOf(id.getType()));
          ps.setInt(2, id.getID());
          return ps;
        }

        protected String getProcessName() { return "тест"; }
        public boolean processResultSet(ResultSet rs) throws SQLException {
          ret.add(new LoadedRelInfoRecord(rs));
          return false;
        }
      }
    );
    Utils.lvSQLExecute(
      new PredefinedLVSQLExecutor() {
        public String getPreparedStatementName() {
          return "PULL_TASK_STATUS2_PS";
        }
        public String getPreparedStatementDesc() { return PULL_TASK_STATUS2_PS_DESC; }
        public PreparedStatement prepareStatement() throws SQLException {
          PreparedStatement ps = super.prepareStatement();
          ps.setString(1, String.valueOf(id.getType()));
          ps.setInt(2, id.getID());
          return ps;
        }

        protected String getProcessName() { return "тест"; }
        public boolean processResultSet(ResultSet rs) throws SQLException {
          ret.add(new LoadedLeft2Scan(rs));
          return true;
        }
      }
    );
    return ret;
  }

    private static final String PULL_TASK_STATUS_INC_PS_DEF_VAL =
            "select UoM_ID, UoM_Name, (Qua_4_Rel - Disp_BCD_Qua) as Disp_Man_Qua, Disp_BCD_Qua, Qua_4_Rel\n" +
                    "from (\n" +
                    "select \n" +
                    "  v.id_units UoM_ID, \n" +
                    "  u.name_short UoM_Name, \n" +
                    "  (select count(*) from Pact_RW_Lines_BCD b where b.pact_id = p.id) Disp_BCD_Qua, \n" +
                    "  sum(v.tovcol) Qua_4_Rel\n" +
                    "from (select 'П' task_type, id, ttn_id from ord_deliveries od union all select 'З' task_type, id, fin_ttn_id from orders) od\n" +
                    "join pact p on p.id_inttn = od.ttn_id\n" +
                    "join pact_tv v on v.id_act_n = p.id\n" +
                    "join units u on u.id = v.id_units\n" +
                    "where od.task_type = :1 and od.id = :2\n" +
                    "group by p.id, v.id_units, u.name_short)";

    private SortedSet<StockTask.RelInfoRecord> pullTaskStatInc(final StockTask.ID id) throws SQLException {
        final TreeSet<StockTask.RelInfoRecord> ret = new TreeSet<StockTask.RelInfoRecord>();

        Utils.lvSQLExecute(
                new PredefinedLVSQLExecutor() {
                    public String getPreparedStatementName() {
                        return "PULL_TASK_STATUS_INC_PS";
                    }
                    public String getPreparedStatementDesc() { return PULL_TASK_STATUS_PS_DESC; }
                    public PreparedStatement prepareStatement() throws SQLException {
                        PreparedStatement ps = super.prepareStatement();
                        ps.setString(1, String.valueOf(id.getType()));
                        ps.setInt(2, id.getID());
                        return ps;
                    }

                    protected String getProcessName() { return "тест"; }
                    public boolean processResultSet(ResultSet rs) throws SQLException {
                        ret.add(new LoadedRelInfoRecord(rs));
                        return true;
                    }
                }
        );

        return ret;
    }
  ///////////////////////////////////////////////////
  private static class LoadedRelInfo4IngredRecord implements StockTask.RelInfoRecord, Loaded {

    private final int dispManQua;
    private final int dispBCDQua;
    private final int qua4Rel;

    private LoadedRelInfo4IngredRecord(ResultSet rs) throws SQLException {
      this.dispBCDQua = rs.getInt(1);
      this.dispManQua = rs.getInt(2);
      this.qua4Rel = rs.getInt(3);
    }

    public int compareTo(StockTask.RelInfoRecord o) {
      return 0;
    }

    public String toString() {
      String ret = "" + dispBCDQua + "+" + dispManQua + "~" + qua4Rel;
      if ( dispBCDQua + dispManQua == qua4Rel ) {
        ret = ret + " ВСЕ";
        if ( dispManQua == 0 ) ret = ret + "!";
      }
      return ret;
    }
  }

  private static final String PULL_TASK_STATUS_4I_PS_DEF_VAL =
    "select Disp_BCD_Qua, Disp_Man_Qua, Qua_4_Rel\n" +
    "from (select task_type, task_id, ingred_id," +
      "disp_bcd_qua, disp_man_qua, qua_4_rel\n" +
      " from BCD_TM_Task_Stats_Det " +
      "union all " +
      "select \n" +
      "task_type, task_id, ingred_id,  \n" +
      "sum(disp_bcd_qua) disp_bcd_qua, sum(disp_man_qua) disp_man_qua, sum(qua_4_rel) qua_4_rel\n" +
      "from BCD_TM_Task2load_Stats_Det t group by task_type, task_id, ingred_id) sd\n" +
    "where\n" +
    "  sd.Task_Type = :1 and\n" +
    "  sd.Task_ID = :2 and\n" +
    "  sd.Ingred_ID = :3";
  private static final String PULL_TASK_STATUS_4I_PS_DESC =
    "Для указанной ТМЦ";

  private StockTask.RelInfoRecord pullTaskStat4I(final StockTask.ID id, final int ingredID) throws SQLException {
    final StockTask.RelInfoRecord[] ret = new StockTask.RelInfoRecord[]{null};

    Utils.lvSQLExecute(
      new PredefinedLVSQLExecutor() {
        public String getPreparedStatementName() { return "PULL_TASK_STATUS_4I_PS"; }
        public String getPreparedStatementDesc() { return PULL_TASK_STATUS_4I_PS_DESC; }
        public PreparedStatement prepareStatement() throws SQLException {
          PreparedStatement ps = super.prepareStatement();
          ps.setString(1, String.valueOf(id.getType()));
          ps.setInt(2, id.getID());
          ps.setInt(3, ingredID);
          return ps;
        }

        protected String getProcessName() { return "тест"; }
        public boolean processResultSet(ResultSet rs) throws SQLException {
          //ret.add(new LoadedRelInfoRecord(rs));
          assert ret[0] == null;
          ret[0] = new LoadedRelInfo4IngredRecord(rs);
          return true;
        }
      }
    );

    return ret[0];
  }

    private static final String PULL_TASK_STATUS_4I_INC_PS_DEF_VAL =
            "select nvl(prl.amount,0) Disp_BCD_Qua, nvl(v.amount,0)-nvl(prl.amount,0) Disp_Man_Qua, nvl(v.amount,0) Qua_4_Rel\n" +
                    "from \n" +
                    "  (select 'П' task_type, id, ttn_id from ord_deliveries od union all select 'З' task_type, id, fin_ttn_id from orders) od\n" +
                    "  join pact p on p.id_inttn = od.ttn_id\n" +
                    "  join (select v.id_act_n, v.id_tovcod, sum(v.tovcol) amount from pact_tv v group by v.id_act_n, v.id_tovcod) v on v.id_act_n = p.id\n" +
                    "  left join (select b.pact_id, b.ingred_id, count(*) amount from pact_rw_lines_bcd b group by b.pact_id, b.ingred_id) prl on prl.pact_id = p.id and prl.ingred_id = v.id_tovcod\n" +
                    "where\n" +
                    "  od.task_type = :1 and\n" +
                    "  od.ID = :2 and\n" +
                    "  v.id_tovcod = :3";

    private StockTask.RelInfoRecord pullTaskStat4IInc(final StockTask.ID id, final int ingredID) throws SQLException {
        final StockTask.RelInfoRecord[] ret = new StockTask.RelInfoRecord[]{null};

        Utils.lvSQLExecute(
                new PredefinedLVSQLExecutor() {
                    public String getPreparedStatementName() { return "PULL_TASK_STATUS_4I_INC_PS"; }
                    public String getPreparedStatementDesc() { return PULL_TASK_STATUS_4I_PS_DESC; }
                    public PreparedStatement prepareStatement() throws SQLException {
                        PreparedStatement ps = super.prepareStatement();
                        ps.setString(1, String.valueOf(id.getType()));
                        ps.setInt(2, id.getID());
                        ps.setInt(3, ingredID);
                        return ps;
                    }

                    protected String getProcessName() { return "тест"; }
                    public boolean processResultSet(ResultSet rs) throws SQLException {
                        //ret.add(new LoadedRelInfoRecord(rs));
                        assert ret[0] == null;
                        ret[0] = new LoadedRelInfo4IngredRecord(rs);
                        return true;
                    }
                }
        );

        return ret[0];
    }
  ///////////////////////////////////////////////////

  private static final String PROC_STOCK_LABEL_CS_DEF_VAL =
    "begin\n" +
    "  :1 := BCD_Terminals_Srv.processSKU4LockedTask(\n" +
    "    :2, --taskType char,\n" +
    "    :3, --taskID number,\n" +
    "    :4, --sessScanID number,\n" +
    "    :5, --translatedIngredID number,\n" +
    "    :6, --emplID number,\n" +
    "    :7  --knownPrevResult char\n" +
    "  );\n" +
    "end;";
  private static final String PROC_STOCK_LABEL_CS_DESC =
    "Регистрирует BCD";

  private StockTask.ProcResult processSKU4LockedTask(
    final StockTask task,
    final StockLabelData sld,
    final Person scanEmpl,
    final StockTask.ProcResult knownPrevResult
  ) throws SQLException, TransException {
    final char[] ret = new char[]{'-'};
    final Ingredient tri = sld.getIngredient().getTranslatedIngred();

    Utils.lvSQLExecute(
      new PredefinedLVSQLExecutor() {
        public String getPreparedStatementName() { return "PROC_STOCK_LABEL_CS"; }
        public PreparedStatement prepareStatement() throws SQLException {
          CallableStatement cs = (CallableStatement) super.prepareStatement();
          StockTask.ID id = task.getID();
          cs.registerOutParameter(1, Types.VARCHAR);

          cs.setString(2, String.valueOf(id.getType()));
          cs.setInt(3, id.getID());
          cs.setInt(4, sld.getID());
          cs.setInt(5, tri.getID());
          cs.setInt(6, scanEmpl.getID());

          if ( knownPrevResult != null ) {
            cs.setString(7, String.valueOf(knownPrevResult.getAlias()));
          } else {
            cs.setNull(7, Types.VARCHAR);
          }
          return cs;
        }
        public String getPreparedStatementDesc() { return PROC_STOCK_LABEL_CS_DESC; }
        public void executionComplete(PreparedStatement sql) throws SQLException {
          CallableStatement cs = (CallableStatement) sql;
          String s = cs.getString(1);
          if ( s != null && s.trim().length() > 0 ) {
            ret[0] = s.charAt(0);
          } else {
            ret[0] = 's';
          }
          super.executionComplete(sql);
        }
      }
    );

    return StockTask.ProcResult.getProcResult(ret[0]);
  }

}

/* end of file*/

