package com.bsp.fsccis.bean.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("DateOrderValidator")
public class DateOrderValidator implements Validator {
	public static final Logger LOGGER = Logger.getLogger(DateOrderValidator.class.getSimpleName());

	@Override
	public void validate(FacesContext arg0, UIComponent component, Object dateObj)
			throws ValidatorException {
		try {
			if (dateObj != null) {
				Date date1 = null;
				if(!(dateObj instanceof Date)){
					showErrorMessage("Invalid date value.");
					return;
				}else{
					date1 = (Date) dateObj;
				}
				UIInput date2Component = (UIInput) component.getAttributes().get("date2");
				if(date2Component != null && date2Component.getSubmittedValue() != null){
			        String date2String = (String) date2Component.getSubmittedValue();
			        if(!date2String.equals("")){
				        String dateFormat=  (String) component.getAttributes().get("dateFormat");
				        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
	
				        if(sdf.parse(date2String).before(date1)){
					        String errObj =  (String) component.getAttributes().get("msg");
					        date2Component.setValid(false);
				        	showErrorMessage(errObj);
				        }
			        }
				}
			}
		} catch (ValidatorException e1) {
			throw e1;
		} catch (Exception e1) {
			e1.printStackTrace();
			showErrorMessage("Invalid date value.");
			return;
		}
	}

	private void showErrorMessage(String msgString) {
		FacesMessage msg = new FacesMessage("Validation Error.", msgString);
		msg.setSeverity(FacesMessage.SEVERITY_ERROR);
		throw new ValidatorException(msg);
	}

}
