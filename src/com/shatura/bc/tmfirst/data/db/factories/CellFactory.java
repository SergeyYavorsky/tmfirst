/* Made by SNS */

package com.shatura.bc.tmfirst.data.db.factories;

import com.shatura.bc.tmfirst.data.Cell;
import ru.sns.db.defined.PredefinedConnection;
import ru.sns.jext.ImmutableStore;
import ru.sns.obj.AbstractIntegerIDNamedEntity;
import ru.sns.obj.db.factories.IDNamedEntityFactory;
import ru.sns.util.colls.idents.IntegerIdentifiable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

public class CellFactory extends IDNamedEntityFactory {

  public CellFactory(PredefinedConnection conn) {
    super(conn);
  }

  private static final String LOAD_CELLS_PS_DEF_VAL =
      "select p.ID, p.Name\n" +
      "from\n" +
      "  table(cast(:1 as Srv_UID_Table)) t,\n" +
      "  wms_ast_cells_vw p\n" +
      "where p.ID = t.COLUMN_VALUE";
  private static final String LOAD_CELLS_PS_DESC =
      "Загружает указанные ячейки";

  public Map<Integer, IntegerIdentifiable> loadIIEntities(Set<Integer> ids) {
    return super.loadIIEntities(ids, "LOAD_CELLS_PS", LOAD_CELLS_PS_DESC);
  }

  /**
   *
   * @param rs
   * @return IntegerIdentifiable - потому, что в prepareStatement() устанавливаются Integer, как ID
   * @throws <{SQLException}>
   */
  protected IntegerIdentifiable createIIEntity(ResultSet rs) throws SQLException {
    return new LoadedCell(ImmutableStore.getInteger(rs.getInt(1)), rs.getString(2));
  }

  private static class LoadedCell extends AbstractIntegerIDNamedEntity implements Cell, Loaded {

    private LoadedCell(Integer id, String name) {
      super(id, name);
    }

  }

}

/* end of file*/

