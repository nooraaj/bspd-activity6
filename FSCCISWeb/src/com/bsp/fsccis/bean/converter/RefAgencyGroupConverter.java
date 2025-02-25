package com.bsp.fsccis.bean.converter;

import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.bsp.fsccis.entity.RefAgencyGroup;
import com.bsp.fsccis.facade.GenericFacade;
import com.bsp.fsccis.util.FSCCISServiceLocator;

@FacesConverter("RefAgencyGroupConverter")
public class RefAgencyGroupConverter  implements Converter{
	   @EJB
	    GenericFacade facade;

	    @Override
	    public Object getAsObject(FacesContext context, UIComponent component, String value) {
			FSCCISServiceLocator locator = FSCCISServiceLocator.getInstance();
			facade = (GenericFacade) locator.locateFacade(GenericFacade.class);
			Object result = null;
			if (value != null) {
				result = facade.find(Integer.parseInt(value),RefAgencyGroup.class);
			}
			return result;
	    }

	    @Override
	    public String getAsString(FacesContext context, UIComponent component, Object value) {
			String result = "";
			if (value != null) { 
				result = ((RefAgencyGroup) value).getAgencyGroupId().toString();
			}
			return result;
	    }
}
