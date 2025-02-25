/**
 * 
 */

PrimeFaces.widget.Password.prototype.testStrength= function(password) {
	var value = 0;   
	if(hasUpperCase = /[A-Z]/.test(password)){
		value += 20;
	}
	if(hasLowerCase = /[a-z]/.test(password)){
		value += 20;
	}
	if(hasNumbers = /\d/.test(password)){
		value += 20;
	}
	if(hasNonalphas = /\W/.test(password)){
		value += 20;
	}
	if (password.length > 10){
		value += 20;
	}else{
		value = 10;
	}   
	if (hasUpperCase + hasLowerCase + hasNumbers + hasNonalphas < 4){ 
		value = 10;
	}
	return value;
};