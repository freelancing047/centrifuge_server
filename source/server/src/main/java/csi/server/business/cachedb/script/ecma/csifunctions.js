load("nashorn:mozilla_compat.js");
importPackage(java.util);
importPackage(java.text);
importClass(Packages.csi.server.util.CsiTypeUtil);
importClass(Packages.csi.server.business.cachedb.script.CsiScriptLib);


var _LIB = new BasicFunc();

__noSuchProperty__ = function(name) {
    return 1;
}

/**
 * ScriptedFunc class to supported basic scripted 
 * function types in the model
 *
 */
 function BasicFunc() {
     BasicFunc.prototype.substringOneBased = function(value, start, end) {
        return String(CsiScriptLib.substringOneBased(value, start, end));
     }
    
    BasicFunc.prototype.duration = function(date1, date2, unit) {
        return Number(CsiScriptLib.duration(date1, date2, unit));
    }
 }

// variable to access Desktop functions
var CSI = new DTFunc();  

/**
 * DTFunc class to support desktop functionality
 * 
 */

function DTFunc() {
    var dateUnitMap = new Array();
    dateUnitMap['years'] = java.util.Calendar.YEAR;
    dateUnitMap['months'] = java.util.Calendar.MONTH;
    dateUnitMap['weeks'] = java.util.Calendar.WEEK_OF_YEAR;
    dateUnitMap['days'] = java.util.Calendar.DAY_OF_YEAR;
    dateUnitMap['hours'] = java.util.Calendar.HOUR_OF_DAY;
    dateUnitMap['minutes'] = java.util.Calendar.MINUTE;
    dateUnitMap['seconds'] = java.util.Calendar.SECOND;

    /**
     * CsiFunc methods
     * 
     */

    DTFunc.prototype.replaceNull = function(value, replaceValue) {
        if (value == null) {
            return replaceValue;
        }

        return value;
    }

    DTFunc.prototype.isNull = function(value) {
        return (value == null || value.length == 0)
    }

    /*
     * String functions
     */
    DTFunc.prototype.length = function(value) {
        return value.length;
    }

    DTFunc.prototype.dateAdd = function(value, offset, unit) {
        if (value) {
            return null;
        }
        
        if (offset) {
            offset = 0;
        }
        
        var cal = new java.util.GregorianCalendar();
        var target = new java.util.Date(value);
        cal.setTime(target);
        
        var calUnit = dateUnitMap[String(unit).toLowerCase()];
        if (calUnit == null) {
            calUnit = java.util.Calendar.DAY_OF_YEAR;
        }
                
        cal.add(calUnit, offset);
        
        return cal.getTime();
    }
    
    DTFunc.prototype.dateSubtract = function(value, offset, unit) {        
        return this.dateAdd(value, -offset, unit);
    }    


    DTFunc.prototype.trim = function(value) {
        return java.lang.String(value).trim();
    }

    DTFunc.prototype.ltrim = function(value) {
        return java.lang.String(value).replaceAll("^\\s+", "");
    }

    DTFunc.prototype.rtrim = function(value) {
        return java.lang.String(value).replaceAll("\\s+$", "");
    }

    DTFunc.prototype.index = function(value, search) {
        return value.indexOf(search);
    }

    DTFunc.prototype.format = function(value, pattern) {
        // TODO: implement me
        return java.lang.String(value);
    }

    DTFunc.prototype.left = function(value, offset) {
        return String(value).substr(0, offset);
    }

    DTFunc.prototype.right = function(value, offset) {
        return String(value).substr(-offset);
    }

    DTFunc.prototype.substr = function(value, start, length) {
    	var offset = start - 1;
        return String(value).slice( offset, offset+length);
    }

    DTFunc.prototype.tokenAll = function(value, delimiter) {
        // use java to split so we don't have to convert
        // for javascript array to java array
        return String(value).split(delimiter);
    }

    DTFunc.prototype.token = function(value_in, range, delimiter) {
    	var value = String( value_in );
        var vals = value.split(delimiter);  
        var rangeStr = String(range);
        var tr = rangeStr.replace(/^\s+|\s+$/g,"");
        var isNeg = (tr.charAt(0) == '-');
        if(isNeg){
        	var irange = eval(rangeStr);
        	if (irange<0) {
            	var absidx = Math.abs(irange);
            	if (absidx > vals.length) {
                	return '';
            	} else {          	
            	   	return vals[vals.length - absidx ];              
            	}
        	}
       }

        var idxs = rangeStr.split('-');
        if (idxs == null || idxs.length == 0 || idxs.length > 2) {
            //return '!BAD INDEX!';
            return '';
        }
        if (idxs[0] > vals.length) {
            //return '!BAD INDEX!';
            return '';
        }

        if (idxs.length == 1) {
            return vals[idxs[0] - 1];
        }

        if (idxs[1] > vals.length) {
            //return '!BAD RANGE!';
            return '';
        }

        return vals.slice(idxs[0] - 1, idxs[1]).join("");
    }

    DTFunc.prototype.tokenCount = function(value, delimiter) {
        return this.tokenAll(value, delimiter).length;
    }

    DTFunc.prototype.tokenExtensions = function(target, tokens, delimiter, replace) {
        var tokens_str = String(tokens);
        return CsiScriptLib.getStringTokens( target, tokens_str, delimiter, replace );
    }

    DTFunc.prototype.toUpper = function(value) {
        if (value == null) {
            return value;
        }
        return value.toUpperCase();
    }

    DTFunc.prototype.toLower = function(value) {
        if (value == null) {
            return value;
        }
        return value.toLowerCase();

    }

    DTFunc.prototype.reverseString = function(value) {
        if (value == null || value.length == 1) {
            return value;
        }

        return (new java.lang.StringBuffer(value)).reverse().toString();
    }

    /*
     * Date functions
     */

    DTFunc.prototype.formatDate = function(value, format) {
        if (value == null || value == "" )
            return value;

        var sdf = new SimpleDateFormat( format );
        if( value instanceof java.util.Date ) {
            return sdf.format(value);
        } 
        
        if( !isNaN( value )  ) {
            var source = new java.util.Date( parseInt( value ) );
            return sdf.format( source );
        } else if( value instanceof String || (typeof value) == 'string') {
            var parser = new SimpleDateFormat();
            var source = parser.parse( value );
            return sdf.format( source );
        }
        
        return "";
    }

    DTFunc.prototype.monthYear = function(value) {
        return this.formatDate(value, 'MM/yyyy');
    }

    DTFunc.prototype.month = function(value) {
        return this.formatDate(value, '(MM) MMMM');
    }

    DTFunc.prototype.date = function(value) {
        return this.formatDate(value, 'dd/MM/yyyy');
    }

    DTFunc.prototype.day = function(value) {
        return this.formatDate('dd');
    }

    DTFunc.prototype.weekday = function(value) {
        return this.formatDate(value, '(dd) EEE');
    }

    DTFunc.prototype.weekdayOrdered = function(value) {
        var cal = new GregorianCalendar();
        cal.setTime(java.util.Date(value));
        return "("+cal.get(Calendar.DAY_OF_WEEK)+") " + this.formatDate(value, 'EEE');
    }

    /*
     * returns 1,2,3,4 as the quarter of the year giving the date
     */
    DTFunc.prototype.calendarQuarter = function(value) {
        var cal = new GregorianCalendar();
        cal.setTime(java.util.Date(value));
        return Math.floor((cal.get(Calendar.MONTH))/3) + 1;
    }

    /*
     * calculate moon phases giving the date
     * NEW, WAXING CRESCENT, FIRST QUARTER, WAXING GIBBOUS, FULL,
     * WANING GIBBOUS, THIRD QUARTER and WANING CRESCENT
     */
    DTFunc.prototype.lunarStage = function(value) {
        var cal = new GregorianCalendar();
        cal.setTime(java.util.Date(value));
        var year = cal.get(Calendar.YEAR);
        var month = cal.get(Calendar.MONTH)+1;
        var day = cal.get(Calendar.DAY_OF_MONTH);

        return CsiScriptLib.calcMoonPhase( year, month, day );

    }

    DTFunc.prototype.time = function(value) {
        return this.formatDate(value, 'hh:mm:ss aa');
    }

    DTFunc.prototype.year = function(value) {
        var cal = new GregorianCalendar();
        cal.setTime(java.util.Date(value));
        return cal.get(Calendar.YEAR);
    }

    DTFunc.prototype.week = function(value) {
        var cal = new GregorianCalendar();
        cal.setTime(java.util.Date(value));
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    DTFunc.prototype.hour = function(value) {
        var cal = new GregorianCalendar();
        cal.setTime(java.util.Date(value));
        return cal.get(Calendar.HOUR) + 1;
    }

    DTFunc.prototype.minute = function(value) {
        var cal = new GregorianCalendar();
        cal.setTime(java.util.Date(value));
        return cal.get(Calendar.MINUTE);
    }

    DTFunc.prototype.second = function(value) {
        var cal = new GregorianCalendar();
        cal.setTime(java.util.Date(value));
        return cal.get(Calendar.SECOND);
    }

    DTFunc.prototype.hourMinute = function(value) {
        return this.formatDate(value, 'hh:mm');
    }

    /*
     * Math functions
     */

    DTFunc.prototype.abs = function(value) {
        return java.lang.Math.abs(value);
    }

    DTFunc.prototype.loge = function(value) {
        return java.lang.Math.log(value);
    }

    DTFunc.prototype.log10 = function(value) {
        return java.lang.Math.log(value) / java.lang.Math.log(10);
    }

    DTFunc.prototype.round = function(value) {
        return java.lang.Math.round(value);
    }

    DTFunc.prototype.floor = function(value) {
        return java.lang.Math.floor(value);
    }

    DTFunc.prototype.ceil = function(value) {
        return java.lang.Math.ceil(value);
    }

    /*
     * Operator functions
     */

    DTFunc.prototype.like = function(value, pattern) {
        if (value == null || value.length == 0) {
            return false;
        }

        // convert from vb like pattern to regex pattern
        // TODO: handle standalone match of ] literal.
        var expr = java.lang.String(pattern).replaceAll("\\[([\\[\\?\\*])\\]", '\\$1');
        expr = java.lang.String(expr).replaceAll("\\[!", '[^');
        expr = java.lang.String(expr).replaceAll("\\*", '.*');
        expr = java.lang.String(expr).replaceAll("#", '[0-9]');

        var matches = value.match(new RegExp(expr, "g"));
        return (matches != null);
    }

    // TODO: handle dates better
    DTFunc.prototype.between = function(value, test1, test2) {
        if (typeof value == 'object') {
            var v = java.util.Date(value);
            var t1 = java.util.Date(test1);
            var t2 = java.util.Date(test2);
            return v.after(t1) && v.before(t2);
        } else {
            return (value >= test1 && value <= test2);
        }
    }

    DTFunc.prototype.isIn = function(value, test) {
        var vals = String(test).split(',');
        for ( var i = 0; i < vals.length; i++) {
            if (value == this.trim(vals[i])) {
                return true;
            }
        }

        return false;
    }

    DTFunc.prototype.ifOp = function(value1, value2, operator, truepart,
            falsepart) {
        var result;

        if (operator == '=' || operator == '==' ) {
            result = (value1 == value2) ? truepart : falsepart;
        } else if (operator == '!=') {
            result = (value1 != value2) ? truepart : falsepart;
        } else if (operator == '>') {
            result = (value1 > value2) ? truepart : falsepart;
        } else if (operator == '>=') {
            result = (value1 >= value2) ? truepart : falsepart;
        } else if (operator == '<') {
            result = (value1 < value2) ? truepart : falsepart;
        } else if (operator == '<=') {
            result = (value1 <= value2) ? truepart : falsepart;
        }

        return result;
    }
    /*
     * Geo related functions
     * Great circle distance formula for two points on the globe
     */
    DTFunc.prototype.distanceBetween = function(lat1, lon1, lat2, lon2, unit) {
        var distance;

        var Rkm = 6371;         //radius of the radius in kilometers
        var Rmiles = 3956;      //radius of the radius in miles

        var dLat = (lat2 - lat1) * Math.PI/180;
        var dLon = (lon2 - lon1) * Math.PI/180;
        var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(lat1 * Math.PI/180) * Math.cos(lat2 * Math.PI/180) *
            Math.sin(dLon/2) * Math.sin(dLon/2);
        var c = 2 * Math.asin(Math.sqrt(a));


        if ( unit == null || unit == '' || unit == "MILE" || unit == "Mile" || unit == "mile" || unit == "mi" || unit == "MI" ) {
            distance = Rmiles * c;
        }

        if ( unit == "KM" || unit == "km" || unit == "kM"  || unit == "Km" ) {
            distance = Rkm * c;
        }
        return java.lang.Math.round(distance * 1000)/1000;
    }

    /*
     * Latitude Decimal to Degrees formatter
     */

    DTFunc.prototype.decimalLatToDegrees = function( lat ){
        if ( isNaN(lat) || lat == null )
            return "";

        var hemisphere = ( lat < 0 ) ? 'S' : 'N'; // south if negative
        return CsiScriptLib.decimalToDMS( lat, hemisphere );
    };

    /*
     * Longitude Decimal to Degrees formatter
     */

    DTFunc.prototype.decimalLongToDegrees = function( lon ){
        if ( isNaN(lon) || lon == null )
            return "";

        var hemisphere = ( lon < 0 ) ? 'W' : 'E';  // west if negative
        return CsiScriptLib.decimalToDMS( lon, hemisphere );
    };

    /*
     * Lat/long Degrees to Decimal formatter
     */

    DTFunc.prototype.degreesToDecimal = function( degrees, minutes, seconds, hemisphere ){
        var ddVal = degrees + minutes / 60 + seconds / 3600;
        ddVal = ( hemisphere == 'S' || hemisphere == 'W' ) ? ddVal * -1 : ddVal;
        return CsiScriptLib.roundToDecimal( ddVal, 5 );
    };

    /*
     * IP Address to Number converter
     */
    DTFunc.prototype.ipDotToNumber = function( ipaddress ) {
        if ( ipaddress == null || ipaddress == "")
            return ipaddress;

        var d = ipaddress.split('.');
        return ((((((+d[0])*256)+(+d[1]))*256)+(+d[2]))*256)+(+d[3]);
    };

    /*
     * IP in Number to dot-notation converter
     */
    DTFunc.prototype.ipNumberToDot = function ( ipnum ) {
        var d = ipnum%256;
        for (var i = 3; i > 0; i--) {
            ipnum = Math.floor(ipnum/256);
            d = ipnum%256 + '.' + d;
        }
        return d;
    };

    /*
     * IPV6 convert to decimal
     */

    DTFunc.prototype.ipV6ToDecimal = function( ipv6 ) {

        return CsiScriptLib.ipV6toDecimal( ipv6 );

    };

    /*
     * Decimal to IPV6
     */
    DTFunc.prototype.decimaltoIpV6 = function ( decimal, compressed ) {
        return CsiScriptLib.decimaltoIpV6( decimal, compressed )

    };

}

