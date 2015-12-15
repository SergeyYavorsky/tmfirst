package com.shatura.bc.tmfirst.data.db.factories;

import java.util.Map;
import java.util.Set;
import java.sql.ResultSet;
import java.sql.SQLException;
import ru.sns.obj.db.factories.IDNamedEntityFactory;
import ru.sns.obj.AbstractIntegerIDNamedEntity;
import ru.sns.db.defined.PredefinedConnection;
import ru.sns.util.colls.idents.IntegerIdentifiable;
import ru.sns.jext.ImmutableStore;
import com.shatura.bc.tmfirst.data.DictObj;
import com.shatura.bc.tmfirst.data.StoreType;

public class StoreTypeFactory extends IDNamedEntityFactory {

  public StoreTypeFactory(PredefinedConnection conn) {
    super(conn);
  }

  private static final String LOAD_STS_PS_DEF_VAL =
      "select ctp.ID, nvl(ctp.Sh_Abbr, ctp.Abbr) Name\n" +
      "from\n" +
      "  table(cast(:1 as Srv_UID_Table)) t,\n" +
      "  Cur_Tov_Purposes ctp\n" +
      "where ctp.ID = t.COLUMN_VALUE";
  private static final String LOAD_STS_PS_DESC =
      "Загружает указанные типы хранения";

  public Map<Integer, IntegerIdentifiable> loadIIEntities(Set<Integer> ids) {
    return super.loadIIEntities(ids, "LOAD_STS_PS", LOAD_STS_PS_DESC);
  }

  /**
   *
   * @param rs
   * @return IntegerIdentifiable - потому, что в prepareStatement() устанавливаются Integer, как ID
   * @throws <{SQLException}>
   */
  protected IntegerIdentifiable createIIEntity(ResultSet rs) throws SQLException {
    return new LoadedStoreType(ImmutableStore.getInteger(rs.getInt(1)), rs.getString(2));
  }

  private static class LoadedStoreType extends AbstractIntegerIDNamedEntity implements StoreType, Loaded {

    private LoadedStoreType(Integer id, String name) {
      super(id, name);
    }

  }

}
