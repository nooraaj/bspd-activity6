package ph.gov.bsp.jsf.security;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

public class ViewExpiredPageExceptionHandlerFactory extends ExceptionHandlerFactory {

	
	/**
	 * Place inside faces-config.xml
	 * <factory>
    	<exception-handler-factory>ph.gov.bsp.jsf.security.ViewExpiredPageExceptionHandlerFactory</exception-handler-factory>
    	</factory>
	 */
	
	private ExceptionHandlerFactory parent;
	
	public ViewExpiredPageExceptionHandlerFactory(ExceptionHandlerFactory parent){
		this.parent = parent;
	}
	
	@Override
	public ExceptionHandler getExceptionHandler() {
		ExceptionHandler result = parent.getExceptionHandler();
		result = new ViewExpiredPageExceptionHandler(result);
		
		return result;
	}
	
}
