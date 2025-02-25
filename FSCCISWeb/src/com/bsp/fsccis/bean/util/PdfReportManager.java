package com.bsp.fsccis.bean.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.faces.bean.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@Named("pdfReportManager")
@ApplicationScoped
public class PdfReportManager {
	private static final Logger LOGGER = Logger.getLogger(PdfReportManager.class.getSimpleName());
	
	public StreamedContent getStream() {
		

		FacesContext context = FacesContext.getCurrentInstance();
		String fileName = context.getExternalContext().getRequestParameterMap().get("fileName");
		// Set standard HTTP/1.1 no-cache headers.
		HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
		response.setHeader("Cache-Control",
				"private, no-store, no-cache, must-revalidate");


		// Set standard HTTP/1.0 no-cache header.
		response.setHeader("Pragma", "no-cache");
			
		ByteArrayOutputStream baos = null;
		
		InputStream input = context.getExternalContext().getResourceAsStream("/resources/pdf/" + fileName);
		if (FacesContext.getCurrentInstance().getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            return new DefaultStreamedContent();
        } else {        	
    		//return new DefaultStreamedContent(input);
        	new DefaultStreamedContent();
			return DefaultStreamedContent.builder().stream(() -> input).build();
        }
	}
	private StreamedContent file;

    public StreamedContent getFile() {
    	FacesContext context = FacesContext.getCurrentInstance();
    	String fileName = context.getExternalContext().getRequestParameterMap().get("fileName");
    	InputStream input = context.getExternalContext().getResourceAsStream("/resources/pdf/" + fileName);
    	//file = new DefaultStreamedContent(input, "application/ms-excel", fileName);
    	file = DefaultStreamedContent.builder()
                .contentType("application/ms-excel")
                //.contentType(fileName)
                .name(fileName)
                .stream(() -> input)
                .build();
        return file;
    }
	
	

}
