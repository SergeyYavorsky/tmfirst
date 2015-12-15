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
import com.shatura.bc.tmfirst.data.Depart;
import com.shatura.bc.tmfirst.data.DictObj;

public class DictObjFactory extends IDNamedEntityFactory {

  public DictObjFactory(PredefinedConnection conn) {
    super(conn);
  }

  private static final String LOAD_OBJS_PS_DEF_VAL =
      "select o.ID, nvl(o.Abbr, o.Name) Name\n" +
      "from\n" +
      "  table(cast(:1 as Srv_UID_Table)) t,\n" +
      "  Objects o\n" +
      "where o.ID = t.COLUMN_VALUE";
  private static final String LOAD_OBJS_PS_DESC =
      "Загружает указанные объекты учёта";

  public Map<Integer, IntegerIdentifiable> loadIIEntities(Set<Integer> ids) {
    return super.loadIIEntities(ids, "LOAD_OBJS_PS", LOAD_OBJS_PS_DESC);
  }

  /**
   *
   * @param rs
   * @return IntegerIdentifiable - потому, что в prepareStatement() устанавливаются Integer, как ID
   * @throws <{SQLException}>
   */
  protected IntegerIdentifiable createIIEntity(ResultSet rs) throws SQLException {
    return new LoadedDictObj(ImmutableStore.getInteger(rs.getInt(1)), rs.getString(2));
  }

  private static class LoadedDictObj extends AbstractIntegerIDNamedEntity implements DictObj, Loaded {

    private LoadedDictObj(Integer id, String name) {
      super(id, name);
    }

  }

}
