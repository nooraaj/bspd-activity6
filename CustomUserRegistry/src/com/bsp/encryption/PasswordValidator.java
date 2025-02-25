package com.bsp.encryption;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
public class PasswordValidator{
	
	  private Pattern pattern;
	  private Matcher matcher;
 
	  //source:http://www.mkyong.com/regular-expressions/how-to-validate-password-with-regular-expression/
//	  (			# Start of group
//			  (?=.*\d)		#   must contains one digit from 0-9
//			  (?=.*[a-z])	#   must contains one lowercase characters
//			  (?=.*[A-Z])	#   must contains one uppercase characters
//			  (?=.*[@#$%])	#   must contains one special symbols in the list "@#$%"
//			  .				#   match anything with previous condition checking
//			  {10,20}		#   length at least 10 characters and maximum of 20	
//	  )			# End of group
	  //complex
//	  private static final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{10,20})";
	  //simpler
	  private static final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{10,20})";
	        
	  public PasswordValidator(){
		  pattern = Pattern.compile(PASSWORD_PATTERN);
	  }
	  
	  /**
	   * Validate password with regular expression
	   * @param password password for validation
	   * @return true valid password, false invalid password
	   */
	  public boolean validate(final String password){
		  
		  matcher = pattern.matcher(password);
		  return matcher.matches();
	    	    
	  }
}