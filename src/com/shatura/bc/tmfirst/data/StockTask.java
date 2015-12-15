package com.shatura.bc.tmfirst.data;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SortedSet;

import com.shatura.bc.tmfirst.TerminalSession;
import com.shatura.bc.tmfirst.logic.states.TerminalSessionState;
import ru.sns.util.colls.idents.Identifiable;
import com.shatura.bc.tmfirst.logic.ex.LogicException;

public interface StockTask extends Identifiable {

  static interface ID extends Comparable<ID>{
    char getType();
    int getID();
    public int hashCode();
    public boolean equals(Object o);
    public String toString();
  }

  static enum ProcResult {
    SUCCESS('s', null),
    ALREADY_ADDED('a', "��� ������! �������?"),
    NEED_QUA_REACHED('f', "��� ����������."),
    NOT_NEEDED('n', "�� �����!");

    public static ProcResult getProcResult(char c) {
      switch ( c ) {
        case 'a': return ALREADY_ADDED;
        case 'f': return NEED_QUA_REACHED;
        case 'n': return NOT_NEEDED;
        case 's': return SUCCESS;
        default: throw new Error("illegal ProcResult c=" + c);
      }
    }

    private final char alias;
    private final String message;

    private ProcResult(char alias, String message) {
      this.alias = alias;
      this.message = message;
    }

    public char getAlias() { return alias; }

    public String getMessage() { return message; }
  }

  static interface RelInfoRecord extends Comparable<RelInfoRecord> {
    int compareTo(RelInfoRecord ot);
    String toString();
  }

  static interface ProcessInLockedTask {
    void process() throws SQLException, LogicException;
  }

  /**
   * ��������� ���������� � ���������� ������� �� �������� � �������� ��� �������������, ���
   * �������, ��� ��� ��� ���������������.
   * @param taskType ��� ������� �� ��������
   * @param taskID ������������� ������� �� ��������
   * @param taskDeleted 'y' - ������� �������, 'n' - "�����"
   * @param rlsTime �����, ����� ������� ���� ��������, null - ������� ��� �� ��������
   * @param rlsStockID ������������� �������, ��� ������ ���.���. ��� ������ ��� ��������� ������
   * @param whWrkTime ����� ������ ����.��������� ���������, null - ����.���. ��������� �� ������
   * @param scansEnabled 'n' - ������������ ���������, 'y' - ���������
   * @param isBlocked 'y' - ������� ������������� ������ ���������, 'n' - ���� ������� ������������ �������
  procedure getWhWrkTaskInfo(
    taskType char, taskID number,
    taskDeleted out char,
    rlsTime out date,
    rlsStockID out number,
    whWrkTime out date,
    scansEnabled out char,
    isBlocked out char
  );
  */

  ID getID();

  boolean isDeleted();

  Date getReleaseTime();

  Depart getRlsStock();

  Depart getDstStock();

  Date getWhWrkTime();

  boolean isScanEnabled();

  boolean isBlocked();

  SortedSet<StockTask.RelInfoRecord> getCurrStat() throws SQLException;

  StockTask.RelInfoRecord getCurrStat4Ingred(Ingredient ingred) throws SQLException;

  //////////////////////

  void lockAndRefreshInfo(ProcessInLockedTask proc) throws SQLException, LogicException;

  public void commit() throws SQLException;

  StockTask.ProcResult processStockLabelData(
      StockLabelData sld, Person scanEmpl, StockTask.ProcResult knownPrevResult
  ) throws SQLException, Ingredient.TransException;

  //////////////////////

  static class Utils {
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("dd/MM/yy HH:mm");

    /*
    public static boolean isProcessingEnabled(StockTask st) {
      return
        ( !st.isDeleted() ) &&
        st.getReleaseTime() == null &&
        st.getRlsStock() != null &&
        st.getWhWrkTime() != null &&
        st.isScanEnabled() &&
        ( !st.isBlocked());
    }
    */

    public static String getDenyMessage(StockTask st) {
      if ( st.isBlocked() ) {
        return "�������������\n���������";
      } else if ( st.isDeleted() ) {
        return "�������!";
     /* } else if ( st.getReleaseTime() != null) {
        return "���. " + TIME_FORMAT.format(st.getReleaseTime()) + "\n" + st.getRlsStock().getName();*/
      } else if ( st.getWhWrkTime() == null ) {
        return "�� �������� �� �����";
      /*} else if ( ! st.isScanEnabled() ) {
        return "����.���������";   */
      }
      return null;
    }

    public static String toString(StockTask st) {
      return
        "ID=" + st.getID() +
        ", deleted=" + st.isDeleted() +
        ", releaseTime=" + st.getReleaseTime() +
        ", rlsStock=" + st.getRlsStock() +
        ", whWrkTime=" + st.getWhWrkTime() +
        ", scanEnabled=" + st.isScanEnabled() +
        ", blocked=" + st.isBlocked();
    }

  }

}
