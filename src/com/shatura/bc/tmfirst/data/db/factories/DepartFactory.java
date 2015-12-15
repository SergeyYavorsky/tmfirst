/* Made by SNS */

package com.shatura.bc.tmfirst.data.db.factories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import ru.sns.db.defined.PredefinedConnection;
import ru.sns.jext.ImmutableStore;
import ru.sns.obj.AbstractIntegerIDNamedEntity;
import ru.sns.obj.db.factories.IDNamedEntityFactory;
import ru.sns.util.colls.idents.IntegerIdentifiable;
import com.shatura.bc.tmfirst.data.Depart;

public class DepartFactory extends IDNamedEntityFactory {

  public DepartFactory(PredefinedConnection conn) {
    super(conn);
  }

  private static final String LOAD_DEPS_PS_DEF_VAL =
      "select d.ID, d.Name\n" +
      "from\n" +
      "  table(cast(:1 as Srv_UID_Table)) t,\n" +
      "  Depart d\n" +
      "where d.ID = t.COLUMN_VALUE";
  private static final String LOAD_DEPS_PS_DESC =
      "Загружает указанные подразделения";

  public Map<Integer, IntegerIdentifiable> loadIIEntities(Set<Integer> ids) {
    return super.loadIIEntities(ids, "LOAD_DEPS_PS", LOAD_DEPS_PS_DESC);
  }

  /**
   *
   * @param rs
   * @return IntegerIdentifiable - потому, что в prepareStatement() устанавливаются Integer, как ID
   * @throws <{SQLException}>
   */
  protected IntegerIdentifiable createIIEntity(ResultSet rs) throws SQLException {
    return new LoadedDepart(ImmutableStore.getInteger(rs.getInt(1)), rs.getString(2));
  }

  private static class LoadedDepart extends AbstractIntegerIDNamedEntity implements Depart, Loaded {

    private LoadedDepart(Integer id, String name) {
      super(id, name);
    }

  }

}

/* end of file*/

