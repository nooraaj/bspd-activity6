package com.bsp.fsccis.bean.filter;

import java.io.IOException;
import java.util.logging.Logger;

import javax.faces.application.ResourceHandler;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bsp.fsccis.bean.login.AuthBean;

//@WebFilter("/views/*")
public class AuthFilter implements Filter {
	private static final Logger LOGGER = Logger.getLogger(AuthFilter.class
			.getSimpleName());

	@Inject
	AuthBean auth;
	

	@Override
	public void destroy() {
		LOGGER.info("destroy");
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		LOGGER.info("doFilter");
		
		final String AJAX_REDIRECT_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		        + "<partial-response><redirect url=\"%s\"></redirect></partial-response>";
		
		//IMPL: http://stackoverflow.com/questions/8480100/how-implement-a-login-filter-in-jsf
		
		HttpServletRequest request = (HttpServletRequest) req;
	    HttpServletResponse response = (HttpServletResponse) res;
	    String loginURL = request.getContextPath() + "/index.xhtml";

		LOGGER.info("getRequestURL: " + request.getRequestURL());
		LOGGER.info("getQueryString: " + request.getQueryString());
	    
		boolean loginRequest = request.getRequestURI().equals(loginURL);
		boolean resourceRequest = request.getRequestURI().startsWith(
				request.getContextPath() + ResourceHandler.RESOURCE_IDENTIFIER
						+ "/");
		boolean ajaxRequest = "partial/ajax".equals(request
				.getHeader("Faces-Request"));
		
		if(auth.isLoggedIn() || loginRequest || resourceRequest){
			chain.doFilter(request, response);
		}else if (ajaxRequest){
        	request.getSession().setAttribute("initarg", request.getRequestURL());
			response.setContentType("text/xml");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().printf(AJAX_REDIRECT_XML, loginURL); // So, return special XML response instructing JSF ajax to send a redirect.
        }
        else {
        	request.getSession().setAttribute("initarg", request.getRequestURL());
            response.sendRedirect(loginURL); // So, just perform standard synchronous redirect.
        }
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {

	}

}
