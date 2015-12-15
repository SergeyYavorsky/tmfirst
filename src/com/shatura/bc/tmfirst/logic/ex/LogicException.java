package com.shatura.bc.tmfirst.logic.ex;

import com.shatura.bc.tmfirst.logic.acts.Action;

public abstract class LogicException extends Exception {

  protected final Action errAction;

  protected LogicException(String message, Action errAction, Throwable cause) {
    super(message, cause);
    this.errAction = errAction;
  }

  public Action getErrAction() { return errAction; }

}
