package ph.gov.bsp.jsf.security;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

public class ViewExpiredPageExceptionHandler extends
		ExceptionHandlerWrapper {
	
	private static final Logger LOGGER = Logger.getLogger(ViewExpiredPageExceptionHandler.class.getSimpleName());

	private ExceptionHandler wrapped;

	public ViewExpiredPageExceptionHandler(ExceptionHandler wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public ExceptionHandler getWrapped() {
		return wrapped;
	}

	@Override
	public void handle() throws FacesException {
		for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents()
				.iterator(); i.hasNext();) {
			ExceptionQueuedEvent event = i.next();
			ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
			Throwable t = context.getException();
			
			t.printStackTrace();
			
			if (t instanceof ViewExpiredException) {
				LOGGER.info("handle()");
				ViewExpiredException vee =(ViewExpiredException) t;
				FacesContext fc = FacesContext.getCurrentInstance();
				Map<String, Object> requestMap = fc.getExternalContext().getRequestMap();
				NavigationHandler nav = fc.getApplication().getNavigationHandler();

				try {
					requestMap.put("currentViewId", vee.getViewId());
					nav.handleNavigation(fc, null, "/index.xhtml");
					fc.getPartialViewContext().setRenderAll(true);
					fc.renderResponse();
				}catch (Exception ex){
				} finally {
					i.remove();
				}
			}
		}
		getWrapped().handle();
	}

}
