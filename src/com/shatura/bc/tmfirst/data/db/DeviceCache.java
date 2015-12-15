package com.shatura.bc.tmfirst.data.db;

import ru.sns.obj.db.IDNamedEntityCache;
import com.shatura.bc.tmfirst.data.db.factories.DeviceFactory;

public class DeviceCache<Device> extends IDNamedEntityCache {

  public DeviceCache(DeviceFactory fact) {
    super(fact);
  }

}
