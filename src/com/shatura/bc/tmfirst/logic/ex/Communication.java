package com.shatura.bc.tmfirst.logic.ex;

import com.shatura.bc.tmfirst.logic.acts.Action;

public class Communication extends LogicException implements Fatal {

  protected Communication(String message, Action errAction, Throwable cause) {
    super(message, errAction, cause);
  }

}
