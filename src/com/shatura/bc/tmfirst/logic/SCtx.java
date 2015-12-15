package com.shatura.bc.tmfirst.logic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.shatura.bc.tmfirst.data.PersonData;
import com.shatura.bc.tmfirst.data.Session;
import com.shatura.bc.tmfirst.data.StockTaskData;

public class SCtx {

  private static final Log log = LogFactory.getLog(SCtx.class);

  private Session sess;
  private PersonData currPerson;
  private StockTaskData currTask;

  public SCtx() {
  }

  public Session getSession() { return sess; }

  public void setSession(Session sess) {
    log.info("для сессии присвоили sess=" + sess);
    this.sess = sess;
  }

  ///////////////////////

  public PersonData getCurrPersonData() {
    return this.currPerson;
  }

  public void setCurrPersonData(PersonData pd) {
    log.info("для сессии присвоили PersonData=" + pd);
    this.currPerson = pd;
  }

  ///////////////////////

  public StockTaskData getCurrTask() {
    return this.currTask;
  }

  public void setCurrTask(StockTaskData st) {
    log.info("для сессии присвоили StockTaskData=" + st);
    this.currTask = st;
  }

}

