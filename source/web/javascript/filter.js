function Filter(){	
}

Filter.startsWith = function (value, pattern, caseFlag){	
	var regex = new RegExp("^"+ Filter.formatPattern(pattern), 'g' + (caseFlag == false ? "i" : "" ));
	return regex.test(value);	
}

Filter.endsWith = function (value, pattern, caseFlag){
	var regex = new RegExp(Filter.formatPattern(pattern) + "$", "g" + (caseFlag == false ? "i" : "" ));
	return regex.test(value);	
}

Filter.contains = function (value, pattern, caseFlag){
	var regex = new RegExp(Filter.formatPattern(pattern), "g" + (caseFlag == false ? "i" : "" ));
	return regex.test(value);	
}
Filter.equals = function (value1, value2, caseFlag){
	var regex = new RegExp("^" +Filter.formatPattern(value1) + "$", "g" + (caseFlag == false ? "i" : "" ));
	return regex.test(value2);
}
Filter.isGreaterThan = function (value1, value2, caseFlag){
	if (_.isNumber(value1) && _.isNumber(value2)) {
		if(value1 > value2) {
			return true;
		}
	}
	
	var str1 = new String(value1);
	var str2 = new String(value2);
	var result = false;
	if(caseFlag) {
		if (str1 > str2) {
			result = true;
		}
	} else {
		str1 = str1.toLowerCase();
		str2 = str2.toLowerCase();
		if (str1 > str2) {
			result = true;
		}
	}
	return result;
}
Filter.isGreaterThanOrEqualTo = function (value1, value2, caseFlag){
	if (_.isNumber(value1) && _.isNumber(value2)) {
		if(value1 >= value2) {
			return true;
		}
	}
	var str1 = new String(value1);
	var str2 = new String(value2);
	var result = false;
	if(caseFlag) {
		if ( (str1 > str2) || (str1 == str2) ) {
			result = true;
		}
	} else {
		str1 = str1.toLowerCase();
		str2 = str2.toLowerCase();
		if ( (str1 > str2) || (str1 == str2) ) {
			result = true;
		}
	}
	return result;
}
Filter.isLessThan = function (value1, value2, caseFlag){
	if (_.isNumber(value1) && _.isNumber(value2)) {
		if(value1 < value2) {
			return true;
		}
	}
	var str1 = new String(value1);
	var str2 = new String(value2);
	var result = false;
	if(caseFlag) {
		if (str1 < str2) {
			result = true;
		}
	} else {
		str1 = str1.toLowerCase();
		str2 = str2.toLowerCase();
		if ( str1 < str2) {
			result = true;
		}
	}
	return result;
}
Filter.isLessThanOrEqualTo = function (value1, value2, caseFlag){
	if (_.isNumber(value1) && _.isNumber(value2)) {
		if(value1 <= value2) {
			return true;
		}
	}
	var str1 = new String(value1);
	var str2 = new String(value2);
	var result = false;
	if(caseFlag) {
		if ( (str1 < str2) || (str1 == str2) ) {
			result = true;
		}
	} else {
		str1 = str1.toLowerCase();
		str2 = str2.toLowerCase();
		if ( (str1 < str2) || (str1 == str2) ) {
			result = true;
		}
	}
	return result;
}
Filter.patternMatch = function (value, pattern, caseFlag){	
	var regex = new RegExp(Filter.formatPattern(pattern), "g" + (caseFlag == false ? "i" : "" ));
	return regex.test(value);
}

Filter.formatPattern = function(pattern) {
	if(_.isString(pattern)) {
		return pattern.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
	}
	return pattern;
	
}