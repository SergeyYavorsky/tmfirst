package com.shatura.bc.tmfirst.data;

import ru.sns.obj.IDNamedEntity;
import ru.sns.util.colls.idents.IntegerIdentifiable;
import com.shatura.bc.tmfirst.logic.ex.LogicException;
import com.shatura.bc.tmfirst.logic.ex.Retrievable;
import com.shatura.bc.tmfirst.logic.ex.UserShowable;

public interface Ingredient extends IDNamedEntity, IntegerIdentifiable {

  public class TransException extends LogicException implements Retrievable, UserShowable {
    public TransException(String message) {
      super(message, null, null);
    }

    public String getReadableDescription(){
      return getMessage();
    }
  }

  String getLongName();
  String getSortName();

  Ingredient getTranslatedIngred() throws TransException;

}
