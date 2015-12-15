package com.shatura.bc.tmfirst;

import ru.sns.env.ApplicationEnvironmentException;
import com.shatura.bc.tmfirst.env.Environment;

public class Test {

  private Test() {
  }

  private static void doDoings() throws ApplicationEnvironmentException {
    Environment env = Environment.get();

    /*
    try {
      DBOpers.getDBOpers().refreshDBConnect();
    } catch ( SQLException ex ) {
      throw new ApplicationEnvironmentException("проблема при инициализации кешей", ex);
    }
    Manager man = env.getDataManager();
    */

    env.getTelnetD().start();


  }


  public static void main(String[] args) {
    try {

      doDoings();

    } catch ( Exception ex ) {
      System.out.println("ex=" + ex);
      ex.printStackTrace();
    }

  }

}

/* end of file*/

