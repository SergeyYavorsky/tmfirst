package com.shatura.bc.tmfirst.logic.ex;

public class InvalidUserNumber extends LogicException implements UserShowable, Retrievable {

  //private final String invNumStr;

  public InvalidUserNumber(String invNumStr, NumberFormatException tr) {
    super("������� ������������ �����: " + invNumStr, null, tr);
    //this.invNumStr = invNumStr;
  }


  public String getReadableDescription() {
    //return "������ � ��.\n" + super.toString() + "\n��������� '" + errAction.getName() + "'";
    return getMessage();
  }
  

}