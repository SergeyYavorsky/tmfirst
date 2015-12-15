package com.shatura.bc.tmfirst.logic.ex;

public class InvalidUserNumber extends LogicException implements UserShowable, Retrievable {

  //private final String invNumStr;

  public InvalidUserNumber(String invNumStr, NumberFormatException tr) {
    super("введено некорректное число: " + invNumStr, null, tr);
    //this.invNumStr = invNumStr;
  }


  public String getReadableDescription() {
    //return "Ошибка в БД.\n" + super.toString() + "\nПовторите '" + errAction.getName() + "'";
    return getMessage();
  }
  

}