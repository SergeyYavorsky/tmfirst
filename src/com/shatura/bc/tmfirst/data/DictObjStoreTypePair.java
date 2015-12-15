package com.shatura.bc.tmfirst.data;

public class DictObjStoreTypePair implements Comparable<DictObjStoreTypePair> {

  private final DictObj obj;
  private final StoreType st;

  public DictObjStoreTypePair(DictObj obj, StoreType st) {
    this.obj = obj;
    this.st = st;  
  }

  public DictObj getDictObj() { return obj; }
  public StoreType getStoreType() { return st; }

  public int hashCode() { return obj.getID() ^ st.getID(); }

  public boolean equals(DictObjStoreTypePair ot) {
    return this.obj.equals(ot.obj) && this.st.equals(ot.st);
  }

  @Override
  public boolean equals(Object ot) {
    if ( ot instanceof DictObjStoreTypePair ) {
      return equals((DictObjStoreTypePair) ot);
    } else {
      return false;
    }
  }

  public int compareTo(DictObjStoreTypePair ot) {
    int ret = this.obj.getName().compareTo(ot.obj.getName());
    if ( ret != 0 ) return ret;
    return this.st.getName().compareTo(ot.st.getName());
  }


  @Override
  public String toString() {
    return obj.getName() + "-" + st.getName();
  }



}



