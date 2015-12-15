/* Made by SNS */

package com.shatura.bc.tmfirst.data.db.factories;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import ru.sns.db.Utils;
import ru.sns.db.defined.PredefinedConnection;
import ru.sns.obj.AbstractIntegerIDNamedEntity;
import ru.sns.obj.db.factories.IDNamedEntityFactory;
import ru.sns.util.colls.idents.IntegerIdentifiable;
import com.shatura.bc.tmfirst.data.Ingredient;
import com.shatura.bc.tmfirst.data.db.IngredientCache;

public class IngredientFactory extends IDNamedEntityFactory {

  private static final Logger log = Logger.getLogger(IngredientFactory.class);

  public IngredientFactory(PredefinedConnection conn) {
    super(conn);
  }

  private static final String LOAD_INGREDS_PS_DEF_VAL =
      "select l.ID, l.Name fullName, s.Name shortName\n" +
      "from\n" +
      "  table(cast(:1 as Srv_UID_Table)) t,\n" +
      "  Dict_Ingr_LName l, Dict_Ingr_LSName s \n" +
      "where l.ID = t.COLUMN_VALUE and s.ID(+) = l.ID";
  private static final String LOAD_INGREDS_PS_DESC =
      "Загружает указанные ингредиенты";

  public Map<Integer, IntegerIdentifiable> loadIIEntities(Set<Integer> ids) {
    return super.loadIIEntities(ids, "LOAD_INGREDS_PS", LOAD_INGREDS_PS_DESC);
  }

  /**
   *
   * @param rs
   * @return IntegerIdentifiable - потому, что в prepareStatement() устанавливаются Integer, как ID
   * @throws <{SQLException}>
   */
  protected IntegerIdentifiable createIIEntity(ResultSet rs) throws SQLException {
    int id = rs.getInt(1);
    String lName = rs.getString(2);
    String sName = rs.getString(3);
    Object o = translateIngred(id);

    if ( o instanceof Integer ) {
      Integer trID = (Integer) o;
      if ( id != trID.intValue() ) {
        o = getIngredientCache().getEntity((Integer) o);
      }
    }
    LoadedIngredient ret = new LoadedIngredient(id, lName, sName, o);
    if ( o instanceof Integer ) {
      // сам себе translated
      ret.trEntity = ret;
    }

    return ret;
  }

  private IngredientCache getIngredientCache() {
    return (IngredientCache) this.getOtherUniqueInstance(IngredientCache.class);
  }

  public static class LoadedIngredient extends AbstractIntegerIDNamedEntity implements Ingredient, Loaded {

    private final String longName;
    private final String shortName;
    private Object trEntity;

    private static final String selectName(String longName, String shortName) {
      return
        shortName == null || shortName.trim().equals("") ?
        longName : shortName;
    }

    private LoadedIngredient(Integer id, String longName, String shortName, Object trEntity) {
      super(id, selectName(longName, shortName));
      this.longName = longName;
      this.shortName = shortName;
      this.trEntity = trEntity;
    }

    private void resolveTranslated(Map<Integer, Ingredient> loaded) {
      if ( trEntity != null ) {
        //Ingredient tr = loaded.get((Integer) trEntity);
        trEntity = loaded.get((Integer) trEntity);
      }
    }

    public Ingredient getTranslatedIngred() throws TransException {

      if ( trEntity instanceof String ) {
        throw new TransException(
          getName() + "(" + getID() + "):" +
          (String) trEntity
        );
      }

      if ( trEntity == null ) return this;

      return (Ingredient) trEntity;
    }

    public String getLongName() { return longName; }

    public String getSortName() { return shortName; }

  }


  private static final String TRANSL_INGRED_CS_DEF_VAL =
    "begin\n" +
    "  BCD_Terminals_Srv.translateAndCheckIngred(\n" +
    "    :1, --ingredID number,\n" +
    "    :2, --ingrtedID2Work out number,\n" +
    "    :3  --errMessage out varchar2\n" +
    "  );\n" +
    "end;";
  private static final String TRANSL_INGRED_CS_DESC =
    "Транслирует конкретную ТМЦ в абстрактную, если она ещё не абстрактная.";

  public Object translateIngred(final int ingredID) throws SQLException {
    final Object ret[] = new Object[]{null};

    Utils.lvSQLExecute(
      new PredefinedLVSQLExecutor() {
        public String getPreparedStatementName() { return "TRANSL_INGRED_CS"; }
        public PreparedStatement prepareStatement() throws SQLException {
          CallableStatement cs = (CallableStatement) super.prepareStatement();
          cs.setInt(1, ingredID);
          cs.registerOutParameter(2, Types.INTEGER);
          cs.registerOutParameter(3, Types.VARCHAR);
          return cs;
        }
        public String getPreparedStatementDesc() { return TRANSL_INGRED_CS_DESC; }
        public void executionComplete(PreparedStatement sql) throws SQLException {
          CallableStatement cs = (CallableStatement) sql;
          int wrkIngredID = cs.getInt(2);
          if ( ! cs.wasNull() ) {
            ret[0] = new Integer(wrkIngredID);
            assert cs.getString(3) == null : "При выданном wrkIngredID=" + wrkIngredID + " не пустое сообщение=" + cs.getString(3);
          } else {
            ret[0] = cs.getString(3);
          }
          super.executionComplete(sql);
        }
      }
    );

    return ret[0];
  }


  //////////////////////////////////////////

  private static final String PLE_LOAD_INGREDS_PS_DEF_VAL =
    "select ic.Ingred_ID, ic.Full_Name, ic.Short_Name, ic.tr_Ingred_ID\n" +
    "from BCD_TM_PreLoad_IngrCache ic";
  private static final String PLE_LOAD_INGREDS_PS_DESC =
    "Загружает нужные ТМЦ в кеш";

  public Map<Integer, Ingredient> preloadIngreds(int limit) throws SQLException {
    final Map<Integer, Ingredient> ret = new HashMap<Integer, Ingredient>(16384);

    final int counterStop = limit > 0 ? limit : Integer.MAX_VALUE;

    log.info("предзагрузка кешей ТМЦ");

    Utils.lvSQLExecute(
      new PredefinedLVSQLExecutor() {
        int counter = 0;
        public String getPreparedStatementName() { return "PLE_LOAD_INGREDS_PS"; }
        public String getPreparedStatementDesc() { return PLE_LOAD_INGREDS_PS_DESC; }
        protected String getProcessName() { return "тест"; }
        public boolean processResultSet(ResultSet rs) throws SQLException {
          int id = rs.getInt(1);
          String lName = rs.getString(2);
          String sName = rs.getString(3);
          Integer trIngred = rs.getInt(4);
          if ( rs.wasNull() ) trIngred = null;

          LoadedIngredient li = new LoadedIngredient(id, lName, sName, trIngred);

          Ingredient old = ret.put(li.getIntegerID(), li);
          assert old == null : "Повторный возврат ТМЦ. old=" + old + ", new=" + li;
          if ( ++counter % 256 == 0 ) {
            log.info("загружено " + counter + " ТМЦ...");
          }

          return counter < counterStop;
        }
      }
    );

    log.info("Ура. Предзагрузка кешей ТМЦ завершена. Кол-во загруженных ТМЦ=" + ret.size());

    for ( Ingredient i : ret.values() ) {
      LoadedIngredient li = (LoadedIngredient) i;
      li.resolveTranslated(ret);
    }

    return ret;
  }

}

/* end of file*/

