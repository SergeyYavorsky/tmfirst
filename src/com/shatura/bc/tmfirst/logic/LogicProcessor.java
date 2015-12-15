package com.shatura.bc.tmfirst.logic;

import java.io.IOException;
import net.wimpi.telnetd.io.BasicTerminalIO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.shatura.bc.tmfirst.TerminalSession;
import com.shatura.bc.tmfirst.logic.ex.LogicException;
import com.shatura.bc.tmfirst.logic.ex.Retrievable;
import com.shatura.bc.tmfirst.logic.ex.UserShowable;
import com.shatura.bc.tmfirst.logic.states.ErrorTerminalSessionState;
import com.shatura.bc.tmfirst.logic.states.TerminalSessionState;

public class LogicProcessor {

  private static final Log log = LogFactory.getLog(LogicProcessor.class);

  public static void process(TerminalSessionState state) {

    while ( state != null ) {

      log.info("������ ��������� " + state.getName());

      //TerminalSessionState.Ask ask;
      UserQuery query;

      try{
        query = state.show();
      } catch ( IOException ex ) {
        log.fatal("�������� ����� ��� ������� ������������ ��������� " + state.getName(), ex);
        log.fatal("�������� ������");
        return;
      } catch ( LogicException exLog ) {
        log.warn("� �������� ������ ���������=" + state + " ��������� ������", exLog);
        if ( exLog instanceof UserShowable ) {
          state = new ErrorTerminalSessionState(state, null, (UserShowable) exLog);
          continue;
        } else if ( exLog instanceof Retrievable ) {
          log.warn("������ �������, ��� �� ������������ - ��������� �� �����");
          continue;
        } else {
          log.fatal("������ �������, ��� ��� ��������...");
          break;
        }
      }
      log.info(
          "��������� " + state.getName() + " ������� �������� � ��� " +
          ( query != null ? "����������� ������ ��� " + query.getClass().getName() : "������ �� ����������� - ����������..." )
      );

      TerminalSession ts = state.getTerminalSession();
      if ( query != null ) {
        try {
          if ( ts != null ) ts.setListener(state);
          state.getTerminalSession().queryUser(query);
        } catch ( BasicTerminalIO.ReadCancelled rc ) {
          log.warn("�������� ����� ��������, ���������������� ���������� " + state.getName());
        } catch ( IOException ex ) {
          log.fatal("�������� ����� ��� �������� ����� ������ � ��������� " + state.getName(), ex);
          log.fatal("�������� ������");
          return;
        } finally {
          if ( ts != null ) ts.setListener(null);
        }

        log.info("��������� " + state.getName() + " � ��������� �������� ������");
      }

      try{
        state = state.processData(query);

      } catch ( LogicException ex ) {
        log.warn("� �������� ��������� ������=" + query + " ���������� " + state.getName() + " ��������� ������", ex);
        if ( ex instanceof UserShowable ) {
          state = new ErrorTerminalSessionState(state, ex.getErrAction(), (UserShowable) ex);
        } else if ( ex instanceof Retrievable ) {
          log.warn("������ �������, ��� ��� ������������ - ��������� �� �����");
        } else {
          log.fatal("������ �������, ��� ��� ��������...");
          state = null;
        }
      }
      log.info("��������� ���������� ����� " + (state != null ? state.getName() : "�������"));
    }

    log.info("��������� �������� ������");
  }

}

