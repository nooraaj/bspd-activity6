package org.owasp.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author OWASP
 * 
 * Add this in the project's web.xml to enable feature
 * <filter>
        <filter-name>ClickjackFilterDeny</filter-name>
        <filter-class>org.owasp.filters.ClickjackFilter</filter-class>
        <init-param>
            <param-name>mode</param-name>
            <param-value>DENY</param-value>
        </init-param>
    </filter>

    <filter>
        <filter-name>ClickjackFilterSameOrigin</filter-name>
        <filter-class>org.owasp.filters.ClickjackFilter</filter-class>
        <init-param>
            <param-name>mode</param-name>
            <param-value>SAMEORIGIN</param-value>
        </init-param>
    </filter>

     <!--  use the Deny version to prevent anyone, including yourself, from framing the page -->
    <filter-mapping> 
        <filter-name>ClickjackFilterDeny</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>

    <!-- use the SameOrigin version to allow your application to frame, but nobody else
    <filter-mapping> 
        <filter-name>ClickjackFilterSameOrigin</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    -->
 *
 */
public class ClickjackFilter implements Filter  
{

    private String mode = "SAMEORIGIN";

    /**
     * Add X-FRAME-OPTIONS response header to tell IE8 (and any other browsers who
     * decide to implement) not to display this content in a frame. For details, please
     * refer to http://blogs.msdn.com/sdl/archive/2009/02/05/clickjacking-defense-in-ie8.aspx.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse)response;
        //If you have Tomcat 5 or 6, there is a known bug using this code.  You must have the doFilter first:
//        chain.doFilter(request, response);
//        res.addHeader("X-FRAME-OPTIONS", mode );            
        //Otherwise use this:
        res.addHeader("X-FRAME-OPTIONS", mode );          
        res.addHeader("Content-Security-Policy", "default-src 'self' 'unsafe-inline' 'unsafe-eval'");
        res.addHeader("X-Content-Security-Policy", "default-src 'self' 'unsafe-inline' 'unsafe-eval'");
        res.addHeader("X-WebKit-CSP", "default-src 'self' 'unsafe-inline' 'unsafe-eval'");
        res.addHeader("X-Content-Type-Options", "nosniff");
        res.addHeader("Strict-Transport-Security", "max-age=31536000 ; includeSubDomains");
        res.addHeader("X-XSS-Protection","1; mode=block");
        chain.doFilter(request, response);
    }

    public void destroy() {
    }

    public void init(FilterConfig filterConfig) {
        String configMode = filterConfig.getInitParameter("mode");
        if ( configMode != null ) {
            mode = configMode;
        }
    }
}