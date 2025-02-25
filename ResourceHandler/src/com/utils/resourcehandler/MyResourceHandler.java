package com.utils.resourcehandler;


import java.io.IOException;
import java.util.logging.Logger;

import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.omnifaces.util.Faces;
//import com.sun.faces.util.Util;

public class MyResourceHandler extends ResourceHandlerWrapper {

  // Properties
  // -----------------------------------------------------------------------------------------------------

  private static final Logger LOGGER = Logger.getLogger(MyResourceHandler.class.getSimpleName());
  private final ResourceHandler wrapped;

  public MyResourceHandler (final ResourceHandler wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public ResourceHandler getWrapped() {
    return wrapped;
  }


  /**
   * @see javax.faces.application.ResourceHandlerWrapper#isResourceRequest(javax.faces.context.FacesContext)
   */
  @Override
  public boolean isResourceRequest(final FacesContext context) {
    return super.isResourceRequest(context);
  }

  @Override
  public void handleResourceRequest(FacesContext context) throws IOException {
    String resourceId = normalizeResourceRequest(context);
//	  LOGGER.info("handleResourceRequest()");
//	  String resourceId = context.getExternalContext().getRequestServletPath();
//	  LOGGER.info("resourceId: " + resourceId);
//	  LOGGER.info("ResourceHandler.RESOURCE_IDENTIFIER: " + ResourceHandler.RESOURCE_IDENTIFIER);
    if (null != resourceId && resourceId.startsWith(RESOURCE_IDENTIFIER)) {
      Resource resource = null;
      String resourceName = null;
      if (ResourceHandler.RESOURCE_IDENTIFIER.length() < resourceId.length()) {
        resourceName = resourceId.substring(RESOURCE_IDENTIFIER.length() + 1);
//        LOGGER.info("res: " + resourceName);
        if (!StringUtils.isEmpty(resourceName)) {
          resource =
              context
                  .getApplication()
                  .getResourceHandler()
                  .createResource(resourceName,
                      context.getExternalContext().getRequestParameterMap().get("ln"));
        }
      }
      if (resource == null) {
        HttpServletResponse response =
            (HttpServletResponse) context.getExternalContext().getResponse();
        response.sendError(404);
        return;
      }
    }
	super.handleResourceRequest(context);
  }
  

  private String normalizeResourceRequest(FacesContext context) {
	  
    String path;
    String facesServletMapping = Faces.getMapping();
//    LOGGER.info("getMapping: " + facesServletMapping);
    // If it is extension mapped
    if (!Faces.isPrefixMapping(facesServletMapping)) {
      path = context.getExternalContext().getRequestServletPath();
      // strip off the extension
      int i = path.lastIndexOf(".");
      if (0 < i) {
        path = path.substring(0, i);
      }
    } else {
      path = context.getExternalContext().getRequestPathInfo();
    }
    return path;
  }


}