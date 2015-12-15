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
import com.shatura.bc.tmfirst.data.IngredMod;

public class IngredModFactory extends IDNamedEntityFactory {

  public IngredModFactory(PredefinedConnection conn) {
    super(conn);
  }

  private static final String LOAD_IMS_PS_DEF_VAL =
      "select im.ID, im.Short_Descr\n" +
      "from\n" +
      "  table(cast(:1 as Srv_UID_Table)) t,\n" +
      "  Dict_TP_Ingred_Mods im\n" +
      "where im.ID = t.COLUMN_VALUE";
  private static final String LOAD_IMS_PS_DESC =
      "Загружает указанные модификации";

  public Map<Integer, IntegerIdentifiable> loadIIEntities(Set<Integer> ids) {
    return super.loadIIEntities(ids, "LOAD_IMS_PS", LOAD_IMS_PS_DESC);
  }

  /**
   *
   * @param rs
   * @return IntegerIdentifiable - потому, что в prepareStatement() устанавливаются Integer, как ID
   * @throws <{SQLException}>
   */
  protected IntegerIdentifiable createIIEntity(ResultSet rs) throws SQLException {
    return new LoadedIngredMod(ImmutableStore.getInteger(rs.getInt(1)), rs.getString(2));
  }

  private static class LoadedIngredMod extends AbstractIntegerIDNamedEntity implements IngredMod, Loaded {

    private LoadedIngredMod(Integer id, String name) {
      super(id, name);
    }

  }

}
