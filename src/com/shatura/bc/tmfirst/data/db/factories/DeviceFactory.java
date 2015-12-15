/* Made by SNS */

package com.shatura.bc.tmfirst.data.db.factories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import ru.sns.db.defined.PredefinedConnection;
import ru.sns.obj.db.factories.IDNamedEntityFactory;
import ru.sns.util.colls.idents.AbstractIntegerIdentifiable;
import ru.sns.util.colls.idents.IntegerIdentifiable;
import com.shatura.bc.tmfirst.data.Device;
import com.shatura.bc.tmfirst.data.Ingredient;
import com.shatura.bc.tmfirst.data.db.IngredientCache;

public class DeviceFactory extends IDNamedEntityFactory {

  private final IngredientCache ingrCache;

  public DeviceFactory(PredefinedConnection conn, IngredientCache ingrCache) {
    super(conn);
    this.ingrCache = ingrCache;
  }

  private static final String LOAD_DEVS_PS_DEF_VAL =
      "select /*+ CARDINALITY(t 1) */\n" +
      "  d.ID, d.Ingred_ID, d.S_N\n" +
      "from\n" +
      "  table(cast(:1 as Srv_UID_Table)) t,\n" +
      "  BCD_TM_Devices d\n" +
      "where d.ID = t.COLUMN_VALUE";
  private static final String LOAD_DEVS_PS_DESC =
      "Загружает указанные устройства";

  public Map<Integer, IntegerIdentifiable> loadIIEntities(Set<Integer> ids) {
    return super.loadIIEntities(ids, "LOAD_DEVS_PS", LOAD_DEVS_PS_DESC);
  }

  /**
   *
   * @param rs
   * @return IntegerIdentifiable - потому, что в prepareStatement() устанавливаются Integer, как ID
   * @throws <{SQLException}>
   */
  protected IntegerIdentifiable createIIEntity(ResultSet rs) throws SQLException {

    return new LoadedDevice(
      rs.getInt(1),
      (Ingredient) ingrCache.getEntity(rs.getInt(2)),
      rs.getString(3)
    );
  }

  private static class LoadedDevice extends AbstractIntegerIdentifiable implements Device, Loaded {

    private final String sn;
    private final Ingredient ingred;

    private LoadedDevice(Integer id, Ingredient ingred, String sn) {
      super(id);
      this.sn = sn;
      this.ingred = ingred;
    }

    public String getSerNum() { return sn; }
    public Ingredient getIngredient() { return ingred; }

  }

}

/* end of file*/

