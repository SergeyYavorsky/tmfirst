package com.shatura.bc.tmfirst.logic.ex;

import com.shatura.bc.tmfirst.logic.acts.Action;

public class ProgramError extends LogicException implements Fatal, UserShowable {

  public ProgramError(String message) {
    this(message, null);
  }

  public ProgramError(String message, Action errAction) {
    this(message, errAction, null);
  }

  public ProgramError(String message, Action errAction, Throwable cause) {
    super(message, errAction, cause);
  }

  public String getReadableDescription() {
    return "прогр.ош.ќбр.в SD.\n" + super.toString();
  }

}

