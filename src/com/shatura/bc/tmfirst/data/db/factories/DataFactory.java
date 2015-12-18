/* Made by SNS */

package com.shatura.bc.tmfirst.data.db.factories;

import java.sql.*;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import ru.sns.db.Utils;
import ru.sns.db.defined.PredefinedConnection;
import ru.sns.k3.dict.db.K3Factory;
import ru.sns.util.colls.idents.AbstractIntegerIdentifiable;
import com.shatura.bc.tmfirst.data.*;
import com.shatura.bc.tmfirst.data.db.*;
import java.util.Iterator;

public class DataFactory extends K3Factory {

  private static final Logger log = Logger.getLogger(DataFactory.class);

  private final IngredientCache ingredCache;
  private final DictObjCache dicObjCache;
  private final StoreTypeCache storeTypeCache;
  private final IngredModCache ingredModCache;
  private final DeviceCache devCache;
  private final DepartCache depCache;
  private final PersonCache personCache;
  private final CellCache cellCache;
  private final StockTasksCache stockTasksCache;

  private String prefixOrd;
  private String prefixStockLabel;
  private String prefixTTN;
  private String prefixDlv;
  private String prefixPerson;
  private String prefixStockLabelTP;
  private String prefixTask2Load;

  public DataFactory(
    PredefinedConnection conn,
    IngredientCache ingredCache,
    IngredModCache ingredModCache,
    DeviceCache devCache,
    DepartCache depCache, PersonCache personCache,
    StockTasksCache stockTasksCache,
    DictObjCache dicObjCache,
    StoreTypeCache storeTypeCache,
    CellCache cellCache
  ) {
    super(conn);
    this.ingredCache = ingredCache;
    this.ingredModCache = ingredModCache;
    this.devCache = devCache;
    this.depCache = depCache;
    this.personCache = personCache;
    this.stockTasksCache = stockTasksCache;

    this.dicObjCache = dicObjCache;
    this.storeTypeCache = storeTypeCache;
    this.cellCache = cellCache;
  }

  private static class LoadedSession extends AbstractIntegerIdentifiable implements Session, Loaded {

    private final Device dev;
    private final Depart stock;

    private LoadedSession(CallableStatement cs, DeviceCache devCache, DepartCache depCache) throws SQLException {
      super(cs.getInt(7));

      int id = cs.getInt(8);
      this.dev = cs.wasNull() ? null : (Device) devCache.getEntity(id);

      id = cs.getInt(9);
      this.stock = cs.wasNull() ? null : (Depart) depCache.getEntity(id);
    }

    public Device getDevice() { return dev; }
    public Depart getDepart() { return stock; }

  }

  private static final String REG_SEESSION_CS_DEF_VAL =
      "begin\n" +
      "  BCD_Terminals_Srv.registerSession(\n" +
      "    :1, --tmName varchar2,\n" +
      "    :2, --tmAddress varchar2,\n" +
      "    :3, --tmPort number,\n" +
      "    :4, --sName varchar2,\n" +
      "    :5, --sAddress varchar2,\n" +
      "    :6, --sPort number,\n" +
      "    --\n" +
      "    :7, --sessionID out number,\n" +
      "    :8, --deviceID out number,\n" +
      "    :9  --stockID out number\n" +
      "  );\n" +
      "end;";
  private static final String REG_SEESSION_CS_DESC =
    "Регистрирует терминальную сессию и возвращает присвоенный ей ID и название узла РБД, к которой присоединились.";
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
    final String tmName, final String tmAddress, final int tmPort,
    final String sName, final String sAddress, final int sPort
  ) throws SQLException {
    final Session[] ret = new Session[]{null};

    Utils.lvSQLExecute(
      new PredefinedLVSQLExecutor() {
        public String getPreparedStatementName() { return "REG_SEESSION_CS"; }
        public PreparedStatement prepareStatement() throws SQLException {
          CallableStatement cs = (CallableStatement) super.prepareStatement();
          cs.setString(1, tmName);
          cs.setString(2, tmAddress);
          cs.setInt(3, tmPort);

          cs.setString(4, sName);
          cs.setString(5, sAddress);
          cs.setInt(6, sPort);

          cs.registerOutParameter(7, Types.INTEGER);
          cs.registerOutParameter(8, Types.INTEGER);
          cs.registerOutParameter(9, Types.INTEGER);
          return cs;
        }
        public String getPreparedStatementDesc() { return REG_SEESSION_CS_DESC; }
        public void executionComplete(PreparedStatement sql) throws SQLException {
          CallableStatement cs = (CallableStatement) sql;
          ret[0] = new LoadedSession(cs, devCache, depCache);
          super.executionComplete(sql);
        }
      }
    );

    return ret[0];
  }

  private static final String GET_BCD_TYPES_CS_DEF_VAL =
    "begin\n" +
    "  :1 := BCD_Generator.BCD_PREFIX_ORD_ID;\n" +
    "  :2 := BCD_Generator.BCD_PREFIX_STOCK_LABEL;\n" +
    "  :3 := BCD_Generator.BCD_PREFIX_TTN_ID;\n" +
    "  :4 := BCD_Generator.BCD_PREFIX_DLV_ID;\n" +
    "  :5 := BCD_Generator.BCD_PREFIX_PERSON_ID;\n" +
    "  :6 := BCD_Generator.BCD_PREFIX_STOCK_LABEL_TP;\n" +
    "  :7 := BCD_Generator.BCD_PREFIX_TASK2LOAD_ID;\n" +
    "end;";
  private static final String GET_BCD_TYPES_CS_DESC =
    "Узнаёт типы штрих-кодов";
  private void ensurePrefixesKnown() throws SQLException {
    if ( prefixOrd != null ) return; // типа, уже загружены

    Utils.lvSQLExecute(
      new PredefinedLVSQLExecutor() {
        public String getPreparedStatementName() { return "GET_BCD_TYPES_CS"; }
        public PreparedStatement prepareStatement() throws SQLException {
          CallableStatement cs = (CallableStatement) super.prepareStatement();
          cs.registerOutParameter(1, Types.VARCHAR);
          cs.registerOutParameter(2, Types.VARCHAR);
          cs.registerOutParameter(3, Types.VARCHAR);
          cs.registerOutParameter(4, Types.VARCHAR);
          cs.registerOutParameter(5, Types.VARCHAR);
          cs.registerOutParameter(6, Types.VARCHAR);
          cs.registerOutParameter(7, Types.VARCHAR);
          return cs;
        }
        public String getPreparedStatementDesc() { return GET_BCD_TYPES_CS_DESC; }
        public void executionComplete(PreparedStatement sql) throws SQLException {
          CallableStatement cs = (CallableStatement) sql;
            prefixOrd = cs.getString(1);
            prefixStockLabel = cs.getString(2);
            prefixTTN = cs.getString(3);
            prefixDlv = cs.getString(4);
            prefixPerson = cs.getString(5);
            prefixStockLabelTP = cs.getString(6);
            prefixTask2Load = cs.getString(7);
          super.executionComplete(sql);
        }
        public boolean isStatementClosable() { return true; }
      }
    );
  }

  private static final String REG_BCD_CS_DEF_VAL =
    "begin\n" +
    "  BCD_Terminals_Srv.registerBarCode(\n" +
    "    :1, --sessionID number,\n" +
    "    :2, --scannedBarCode varchar2,\n" +
    "    :3, --bcType out varchar2,\n" +
    "    :4, --field1 out number,\n" +
    "    :5, --field2 out number,\n" +
    "    :6, --field3 out number,\n" +
    "    :7, --bcID out number\n" +
    "    :8  --bcdErrTxt out varchar2\n" +
    "  );\n" +
    "end;";
  private static final String REG_BCD_CS_DESC =
    "регистрирует сканирование кода и возвращает его тип и распарсенные данные";
  public BarCodeData registerBC(final int sessionID, final String scannedBarCode, final boolean doCommit) throws SQLException {
    ensurePrefixesKnown();
    final BarCodeData[] ret = new BarCodeData[1];
    Utils.lvSQLExecute(
      new PredefinedLVSQLExecutor() {
        public String getPreparedStatementName() { return "REG_BCD_CS"; }
        public PreparedStatement prepareStatement() throws SQLException {
          CallableStatement cs = (CallableStatement) super.prepareStatement();
          cs.setInt(1, sessionID);
          cs.setString(2, scannedBarCode);
          cs.registerOutParameter(3, Types.VARCHAR);
          cs.registerOutParameter(4, Types.INTEGER);
          cs.registerOutParameter(5, Types.INTEGER);
          cs.registerOutParameter(6, Types.INTEGER);
          cs.registerOutParameter(7, Types.INTEGER);
          cs.registerOutParameter(8, Types.VARCHAR);
          return cs;
        }
        public String getPreparedStatementDesc() { return REG_BCD_CS_DESC; }
        public void executionComplete(PreparedStatement sql) throws SQLException {
          CallableStatement cs = (CallableStatement) sql;
          ret[0] = createBarCodeDataObject(scannedBarCode, cs, doCommit);
          super.executionComplete(sql);
        }
        public boolean isStatementClosable() { return true; }
      }
    );
    return ret[0];
  }

  private static Integer pullInteger(CallableStatement cs, int pos) throws SQLException {
    int i = cs.getInt(pos);
    if ( cs.wasNull() ) return null;
    return i;
  }


  private static class LoadedStockLabelData extends AbstractIntegerIdentifiable implements StockLabelData, Loaded {
    private final Ingredient ingred;
    private final Depart dprt;
    private final IngredMod tim;
    private final int uniqComp;

    //private final Ingredient translated;
    //private final String bcdError;

    private LoadedStockLabelData(
      Integer id, Ingredient ingred, IngredMod tim, Depart dprt, int uniqComp
      //,Ingredient translated, String bcdError
    ) {
      super(id);
      this.ingred = ingred;
      this.tim = tim;
      this.dprt = dprt;
      this.uniqComp = uniqComp;

      //this.translated = translated;
      //this.bcdError = bcdError;
    }


    public Ingredient getIngredient() { return ingred; }
    public IngredMod getTIM() { return tim; }
    
    public Depart getDepart() { return dprt; }
    public int getUniqComp() { return uniqComp; }

    public boolean isTypical() { return tim != null; }


    //public Ingredient getTranslatedIngredient() { return translated; }
    //public String getError4BCD() { return bcdError; }


    @Override
    public String toString() {
      return super.toString() + ", ingred=" + ingred + ", tim=" + tim + ", dprt=" + dprt + ", uniqComp=" + uniqComp;
    }
  }

    private  class LoadedStockCellData extends AbstractIntegerIdentifiable implements StockCellData, Loaded {
        private final Cell cell;
        private final Depart dprt;

        private LoadedStockCellData(
                Integer id, Cell cell, Depart dprt
        ) {
            super(id);
            this.cell = cell;
            this.dprt = dprt;
        }

        public Cell getCell() { return cell; }

        public String getCellContent() throws SQLException {
          String ret = "";
          Integer cnt = 0;
          SortedSet<String> ss = getIngreds4CellStock(this.getID());
          for(final Iterator it = ss.iterator(); it.hasNext(); )
          {
            if (cnt >= 4) {
              ret = ret + "\nИ ещё " + (ss.size() - cnt) + " ТМЦ здесь...";
              //ss.size()
              return ret;
            }
            cnt++;
            Ingredient ingred = (Ingredient) ingredCache.getEntity(Integer.parseInt(it.next().toString()));
            SortedSet<RestsInfoRecord> rests = getRestsInfo(dprt.getID(), ingred.getID(), null);
            ret = ret + "<" + ingred.getID() + "> " + ingred.getLongName() + "\n";
            StringBuilder sb = new StringBuilder();
            for(final Iterator it2 = rests.iterator(); it2.hasNext(); )
            {
              RestsInfoRecord rir = (RestsInfoRecord) it2.next();
              sb.append(rir).append("\n");
            }
            ret = ret + sb + "\n";
          }
          return ret;
        }
        @Override
        public String toString() {
            return super.toString() + ", cell=" + cell;
        }
    }

  private static class LoadedPersonData extends AbstractIntegerIdentifiable implements PersonData, Loaded {

    private final Person p;

    private LoadedPersonData(Integer id, Person p) {
      super(id);
      this.p = p;
    }

    public Person getPerson() { return p; }

    @Override
    public String toString() {
      return super.toString() + ", person=" + p;
    }

  }

  private static class LoadedUnknownData extends AbstractIntegerIdentifiable implements UnknownData, Loaded {

    private final String data;

    private LoadedUnknownData(Integer id, String data) {
      super(id);
      this.data = data;
    }

    public String getData() { return data; }

    @Override
    public String toString() {
      return super.toString() + ", data=" + data;
    }

  }

  private static class LoadedErrorData extends AbstractIntegerIdentifiable implements ErrorData, Loaded {

    private final String errTxt;

    private LoadedErrorData(Integer bcID, String errTxt) {
      super(bcID);
      this.errTxt = errTxt;
    }

    public String getErrorText() { return errTxt; }

    @Override
    public String toString() {
      return super.toString() + ", errTxt=" + errTxt;
    }

  }


  private BarCodeData createBarCodeDataObject(String scannedBarCode, CallableStatement cs, boolean doCommit) throws SQLException {
    String bcType = cs.getString(3); // МОЖЕТ БЫТЬ NULL!!!!
    Integer fld1 = pullInteger(cs, 4);
    Integer fld2 = pullInteger(cs, 5);
    Integer fld3 = pullInteger(cs, 6);
    Integer bcID = pullInteger(cs, 7);
    String bcdErrTxt = cs.getString(8);

    if ( bcdErrTxt != null ) {
      return new LoadedErrorData(bcID, bcdErrTxt);
    }

    if ( bcType == null || bcType.trim().equals("") )
      return new LoadedUnknownData(bcID, scannedBarCode);

    if ( bcType.equals(prefixStockLabel) || bcType.equals(prefixStockLabelTP) ) {
      Ingredient ingred = (Ingredient) ingredCache.getEntity(fld1);
      Depart dprt = null;
      IngredMod tim = null;

      boolean isTP = bcType.equals(prefixStockLabelTP);
      
      if ( fld2 != null ) {
        if ( isTP ) {
          tim = (IngredMod) ingredModCache.getEntity(fld2);
        } else {
          dprt = (Depart) depCache.getEntity(fld2);
        }
      }

      return new LoadedStockLabelData(bcID, ingred, tim, dprt, fld3);
    }

    if (bcType.contains("-")) {
      Cell cell = (Cell) cellCache.getEntity(fld1);
      Depart dprt = (Depart) depCache.getEntity(fld2);
      return new LoadedStockCellData(fld1, cell, dprt);
    }

    if ( bcType.equals(prefixPerson) ) {
      Person p = (Person) personCache.getEntity(fld1);
      return new LoadedPersonData(bcID, p);
    }

    if ( bcType.equals(prefixTTN) ) {
      return loadTTNData(bcID, fld1);
    }

    StockTask.ID stID = null;

    if ( bcType.equals(prefixOrd) ) {
      stID = stockTasksCache.getStockTaskIDForOrd(fld1);
    }

    if ( bcType.equals(prefixDlv) ) {
      stID = stockTasksCache.getStockTaskIDForDlv(fld1);
    }

    if ( bcType.equals(prefixTask2Load) ) {
      stID = stockTasksCache.getStockTaskIDForLoad(fld1);
      //return new LoadedErrorData(bcID, "не умею пока работать с bcType=" + bcType);
    }

    if ( stID != null ) {
      StockTask st = stockTasksCache.getStockTask(stID, doCommit);
      return new LoadedStockTaskData(bcID, st);
    }

    return new LoadedErrorData(bcID, "неизвестный bcType=" + bcType);

  }


  //////////////////////////////////////////////

  public static class LoadedTTNData extends AbstractIntegerIdentifiable implements TTNData, Loaded {

    private final int ttnID;
    private final String number;
    private final Timestamp dt;

    private LoadedTTNData(Integer bcID, int ttnID, String number, Timestamp dt) {
      super(bcID);
      this.ttnID = ttnID;
      this.number = number;
      this.dt = dt;
    }

    public int getTTNID() { return ttnID; }
    public String getNumber() { return number; }
    public java.util.Date getDate() { return dt; }

    @Override
    public String toString() {
      return super.toString() + ", ttnID=" + ttnID + ", number=" + number + ", dt=" + dt;
    }
  }

  /*
  public interface StockTaskData extends BarCodeData, IntegerIdentifiable {
    StockTask getStockTask();
  }
  */

  public static class LoadedStockTaskData extends AbstractIntegerIdentifiable implements StockTaskData, Loaded {

    private final StockTask st;

    private LoadedStockTaskData(Integer bcID, StockTask st) {
      super(bcID);
      this.st = st;
    }

    public StockTask getStockTask() { return st; }

    @Override
    public String toString() {
      return super.toString() + ", stockTask=" + getStockTask();
    }

  }

  private static final String SEL_TTN_PS_DEF_VAL =
    "select t.TTN_D, fn.Doc_Full_Number\n" +
    "from TTN t, War_Docs_Full_Numbers fn\n" +
    "where fn.ttn_ID = t.ID and t.ID = :1";
  private static final String SEL_TTN_PS_DESC =
    "Загружает данные из указанной ТТН";

  private TTNData loadTTNData(final int barCodeID, final int ttnID) throws SQLException {
    final TTNData[] ret = new TTNData[1];

    Utils.lvSQLExecute(
      new PredefinedLVSQLExecutor() {
        public String getPreparedStatementName() { return "SEL_TTN_PS"; }
        public String getPreparedStatementDesc() { return SEL_TTN_PS_DESC; }
        public PreparedStatement prepareStatement() throws SQLException {
          PreparedStatement ps = super.prepareStatement();
          ps.setInt(1, ttnID);
          return ps;
        }

        protected String getProcessName() { return "тест"; }
        public boolean processResultSet(ResultSet rs) throws SQLException {
          ret[0] = new LoadedTTNData(barCodeID, ttnID, rs.getString(2), rs.getTimestamp(1));
          return false;
        }
      }
    );

    return ret[0];
  }

  //////////////////////////////////////////////

  private static class LoadedRestsInfoRecord implements RestsInfoRecord, Loaded {

    private final DictObjStoreTypePair ost;

    private final int total;
    private final int reserv;

    private LoadedRestsInfoRecord(DictObj obj, StoreType st, int total, int reserv) {
      this.ost = new DictObjStoreTypePair(obj, st);
      this.total = total;
      this.reserv = reserv;
    }

    public DictObj getDictObj() { return ost.getDictObj(); }
    public StoreType getStoreType() { return ost.getStoreType(); }
    public DictObjStoreTypePair getDictObjStoreTypePair() { return ost; }
    
    public int getTotal() { return total; }
    public int getReserv() { return reserv; }

    public int compareTo(RestsInfoRecord ot) {
      return this.ost.compareTo(ot.getDictObjStoreTypePair());
    }

    @Override
    public String toString() {
      return ost.toString() + ": " + total + "-" + reserv + "=" + (total-reserv);
    }
  }
  
  private final static String GET_REST_INFO_PS_DEF_VAL =
      "select r.Obj_ID, r.ST_ID, r.Total_Qua, r.Res_Qua\n" +
      "from War_Curr_Rests_Vw r, Ingredients i\n" +
      "where\n" +
      "  i.ID = r.Ingred_ID and\n" +
      "  i.UoM_ID = i.UoM_ID and\n" +
      "  r.stock_ID = :1 and\n" +
      "  r.Ingred_ID = :2 and\n" +
      "  nvl(r.TIM_ID, -1.23) = nvl(:3, -1.23)";
  private final static String GET_REST_INFO_PS_DESC =
      "Извлекает информацию по текущим остаткам";

  private SortedSet<RestsInfoRecord> getRestsInfo(final int stockID, final int ingredID, final Integer timID) throws SQLException {
    final SortedSet<RestsInfoRecord> ret = new TreeSet<RestsInfoRecord>();

    Utils.lvSQLExecute(
      new PredefinedLVSQLExecutor() {
        public String getPreparedStatementName() { return "GET_REST_INFO_PS"; }
        public String getPreparedStatementDesc() { return GET_REST_INFO_PS_DESC; }
        public PreparedStatement prepareStatement() throws SQLException {
          PreparedStatement ps = super.prepareStatement();
          ps.setInt(1, stockID);
          ps.setInt(2, ingredID);
          if ( timID != null ) {
            ps.setInt(3, timID);
          } else {
            ps.setNull(3, Types.INTEGER);
          }
          return ps;
        }
        public boolean processResultSet(ResultSet rs) throws SQLException {
          //ret[0] = new LoadedTTNData(barCodeID, ttnID, rs.getString(2), rs.getTimestamp(1));
          int id = rs.getInt(1);
          DictObj obj = (DictObj) dicObjCache.getEntity(id);
          assert obj != null;

          id = rs.getInt(2);
          StoreType st = (StoreType) storeTypeCache.getEntity(id);
          assert st != null;

          ret.add(new LoadedRestsInfoRecord(obj, st, rs.getInt(3), rs.getInt(4)));
          return true;
        }
      }
    );
    return ret;
  }

  private static final String GET_CELLS_4_INGR_STOCK_PS_DEF_VAL =
    "select a.Cell_Name\n" +
    "from BCD_TM_Ingreds_Cells_On_Stocks a\n" +
    "where a.Ingred_ID = :1 and a.Stock_ID = :2";
  private final static String GET_CELLS_4_INGR_STOCK_PS_DESC =
    "Извлекает названия ячеек для опр.ТМЦ на опр.сладе";
  
  private SortedSet<String> getCells4IngrStock(final int stockID, final int ingredID) throws SQLException {
    final SortedSet<String> ret = new TreeSet<String>();

    Utils.lvSQLExecute(
      new PredefinedLVSQLExecutor() {
        public String getPreparedStatementName() { return "GET_CELLS_4_INGR_STOCK_PS"; }
        public String getPreparedStatementDesc() { return GET_CELLS_4_INGR_STOCK_PS_DESC; }
        public PreparedStatement prepareStatement() throws SQLException {
          PreparedStatement ps = super.prepareStatement();
          ps.setInt(1, ingredID);
          ps.setInt(2, stockID);
          return ps;
        }
        public boolean processResultSet(ResultSet rs) throws SQLException {
          ret.add(rs.getString(1));
          return true;
        }
      }
    );
    return ret;
  }

  private static final String GET_SP_4_INGR_STOCK_PS_DEF_VAL =
    "select '[СП]'\n" +
      "from STP_INGREDS4STOCK a\n" +
      "where a.Ingred_ID = :1 and a.Stock_ID = :2 and rownum = 1";
  private final static String GET_SP_4_INGR_STOCK_PS_DESC =
    "Относится ли изделие к 'складской программе' для опр.ТМЦ на опр.сладе";

  private String getSP4IngrStock(final int stockID, final int ingredID) throws SQLException {
    //final SortedSet<String> ret = new TreeSet<String>();
    final String[] ret = {""};
    Utils.lvSQLExecute(
      new PredefinedLVSQLExecutor() {
        public String getPreparedStatementName() { return "GET_SP_4_INGR_STOCK_PS"; }
        public String getPreparedStatementDesc() { return GET_SP_4_INGR_STOCK_PS_DESC; }
        public PreparedStatement prepareStatement() throws SQLException {
          PreparedStatement ps = super.prepareStatement();
          ps.setInt(1, ingredID);
          ps.setInt(2, stockID);
          return ps;
        }
        public boolean processResultSet(ResultSet rs) throws SQLException {
          ret[0] = rs.getString(1);
          return true;
        }
      }
    );
    return ret[0];
  }

  private static final String GET_INGRS_4_CELL_STOCK_PS_DEF_VAL =
    "select a.ingred_id\n" +
      "from WMS_AST_CELL_INGREDS a\n" +
      "where a.cell_ID = :1 ";
  private final static String GET_INGRS_4_CELL_STOCK_PS_DESC =
    "Извлекает изделия в ячейке";

  private SortedSet<String> getIngreds4CellStock(final int cellID) throws SQLException {
    final SortedSet<String> ret = new TreeSet<String>();

    Utils.lvSQLExecute(
      new PredefinedLVSQLExecutor() {
        public String getPreparedStatementName() { return "GET_INGRS_4_CELL_STOCK_PS"; }
        public String getPreparedStatementDesc() { return GET_INGRS_4_CELL_STOCK_PS_DESC; }
        public PreparedStatement prepareStatement() throws SQLException {
          PreparedStatement ps = super.prepareStatement();
          ps.setInt(1, cellID);
          return ps;
        }
        public boolean processResultSet(ResultSet rs) throws SQLException {
          ret.add(rs.getString(1));
          return true;
        }
      }
    );
    return ret;
  }

  public IngredOnStockInfo getIngredOnStockInfo(int stockID, final int ingredID, final Integer timID) throws SQLException {
    return new LoadedIngredOnStockInfo(
      getRestsInfo(stockID, ingredID, timID),
      getCells4IngrStock(stockID, ingredID),
      getTasksInfo4IngrStock(stockID, ingredID, timID),
      getSP4IngrStock(stockID, ingredID)
    );
  }

  private static class LoadedIngredOnStockInfo implements IngredOnStockInfo, K3Factory.Loaded {

    private final SortedSet<RestsInfoRecord> rests;
    private final SortedSet<String> cells;
    private final String SP;
    private SortedMap<StockTask.ID, SortedMap<DictObjStoreTypePair, Number>> actTasksUsages;

    private LoadedIngredOnStockInfo(
      SortedSet<RestsInfoRecord> rests,
      SortedSet<String> cells,
      SortedMap<StockTask.ID, SortedMap<DictObjStoreTypePair, Number>> actTasksUsages,
      String SP
    ) {
      this.rests = rests;
      this.cells = cells;
      this.actTasksUsages = actTasksUsages;
      this.SP = SP;
    }
    
    public SortedSet<RestsInfoRecord> getRests() { return rests; }
    
    public SortedSet<String> getCells() { return cells; }

    public SortedMap<StockTask.ID, SortedMap<DictObjStoreTypePair, Number>> getActTasksUsages() { return actTasksUsages; }

    public String getSP() {return SP; }

  }

  ////////////////////////////

  private static final String GET_WWRS_PS_DEF_VAL =
    "select r.Task_Type, r.Task_ID, r.Obj_ID, r.ST_ID, r.Qua\n" +
    "from WMS_On_WhWrk_Reserves r\n" +
    "where\n" +
    "  r.Stock_ID = :1 and\n" +
    "  r.Ingred_ID = :2 and\n" +
    "  nvl(r.TIM_ID, -1.23) = nvl(:3, -1.23)";
  private static final String GET_WWRS_PS_DESC =
    "Извлекает перечень заданий на складской обработке, где имеется отгружаемая ТМЦ на указанно складе.";
  
  private SortedMap<StockTask.ID, SortedMap<DictObjStoreTypePair, Number>> getTasksInfo4IngrStock(
    final int stockID, final int ingredID, final Integer timID
  ) throws SQLException {
    
    final SortedMap<StockTask.ID, SortedMap<DictObjStoreTypePair, Number>> ret =
      new TreeMap<StockTask.ID, SortedMap<DictObjStoreTypePair, Number>>();

    Utils.lvSQLExecute(
      new PredefinedLVSQLExecutor() {
        public String getPreparedStatementName() { return "GET_WWRS_PS"; }
        public String getPreparedStatementDesc() { return GET_WWRS_PS_DESC; }
        public PreparedStatement prepareStatement() throws SQLException {
          PreparedStatement ps = super.prepareStatement();
          ps.setInt(1, stockID);
          ps.setInt(2, ingredID);

          if ( timID != null ) {
            ps.setInt(3, timID);
          } else {
            ps.setNull(3, Types.INTEGER);
          }

          return ps;
        }
        public boolean processResultSet(ResultSet rs) throws SQLException {
          StockTask.ID taskID = stockTasksCache.getStockTaskID(rs.getString(1).charAt(0), rs.getInt(2));
          DictObjStoreTypePair ost = new DictObjStoreTypePair(
            (DictObj) dicObjCache.getEntity(rs.getInt(3)),
            (StoreType) storeTypeCache.getEntity(rs.getInt(4))
          );
          Integer q = rs.getInt(5);

          SortedMap<DictObjStoreTypePair, Number> mByTask = ret.get(taskID);
          if ( mByTask == null ) {
            ret.put(taskID, mByTask = new TreeMap<DictObjStoreTypePair, Number>());
          }
          mByTask.put(ost, q);
          return true;
        }
      }
    );
    return ret;
  }  

}
/* end of file*/

