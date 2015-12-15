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
import com.shatura.bc.tmfirst.data.Person;

public class PersonFactory extends IDNamedEntityFactory {

  public PersonFactory(PredefinedConnection conn) {
    super(conn);
  }

  private static final String LOAD_PERS_PS_DEF_VAL =
      "select p.ID_Ankt, p.Name\n" +
      "from\n" +
      "  table(cast(:1 as Srv_UID_Table)) t,\n" +
      "  Kdr_Empl_Short_Names p\n" +
      "where p.ID_Ankt = t.COLUMN_VALUE";
  private static final String LOAD_PERS_PS_DESC =
      "Загружает указанных людей";

  public Map<Integer, IntegerIdentifiable> loadIIEntities(Set<Integer> ids) {
    return super.loadIIEntities(ids, "LOAD_PERS_PS", LOAD_PERS_PS_DESC);
  }

  /**
   *
   * @param rs
   * @return IntegerIdentifiable - потому, что в prepareStatement() устанавливаются Integer, как ID
   * @throws <{SQLException}>
   */
  protected IntegerIdentifiable createIIEntity(ResultSet rs) throws SQLException {
    return new LoadedPerson(ImmutableStore.getInteger(rs.getInt(1)), rs.getString(2));
  }

  private static class LoadedPerson extends AbstractIntegerIDNamedEntity implements Person, Loaded {

    private LoadedPerson(Integer id, String name) {
      super(id, name);
    }

  }

}

/* end of file*/

