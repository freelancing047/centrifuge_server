

var geometry = new Object();

geometry.computeHeight = function( targetElement, isIE )
{
    var ele = targetElement;
    if( ele == null ) {
        return NaN;
    }

    var value;
    if( isIE == true ) {
        value = document.body.offsetHeight - ele.offsetTop;
    } else {
        value = document.body.clientHeight - ele.offsetTop;
    }

    return value;
}

// this is only good for nodes that are just under body?
geometry.resizeContent = function( targetId )
{
    var node = document.getElementById( targetId );
    if( node == null ) {
        return;
    }

    var value = geometry.computeHeight( node );
    if( !isNaN( value ) ) {
        node.style.height = value+"px";
    }
}

