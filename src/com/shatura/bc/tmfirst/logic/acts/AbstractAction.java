package com.shatura.bc.tmfirst.logic.acts;

import ru.sns.obj.NamedEntity;
import ru.sns.reflect.UniqueInstancesFactory;
import com.shatura.bc.tmfirst.logic.ex.ProgramError;

public abstract class AbstractAction implements Action, NamedEntity, UniqueInstancesFactory.Singleton {

  public AbstractAction() {
  }

  public abstract String getName();

  private static UniqueInstancesFactory f = new UniqueInstancesFactory();

  static Action getAction(Class actionClass) throws ProgramError {
    try{
      return (Action) f.getInstance(actionClass);
    } catch ( UniqueInstancesFactory.UIFException ex ) {
      throw new ProgramError("не могу создать Action=" + actionClass.getName(), null, ex);
    }
  }

  public static GoToMainMenuAction getGoToMainMenuAction() throws ProgramError {
    return (GoToMainMenuAction) getAction(GoToMainMenuAction.class);
  }

  public static ProcessScannedCodeAction4MainMenu getProcessScannedCodeAction4MainMenu() throws ProgramError {
    return (ProcessScannedCodeAction4MainMenu) getAction(ProcessScannedCodeAction4MainMenu.class);
  }

  public static ProcessScannedCodeAction4StockTask getProcessScannedCodeAction4StockTask() throws ProgramError {
    return (ProcessScannedCodeAction4StockTask) getAction(ProcessScannedCodeAction4StockTask.class);
  }

  public static GetStockTaskInfoAction getGetStockTaskInfoAction() throws ProgramError {
    return (GetStockTaskInfoAction) getAction(GetStockTaskInfoAction.class);
  }



}
