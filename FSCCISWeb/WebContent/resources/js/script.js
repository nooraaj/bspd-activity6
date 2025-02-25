/**
 * 
 */

function synchronizeRowsHeight() {
	
	//thead
    var $rows = $(document.getElementById('form:permissionTable_frozenThead')).find('tr');
        $rows.each(function(index) {
            var $row = $(this);
            $row.innerHeight($(document.getElementById('form:permissionTable_scrollableThead')).outerHeight());
        });
        
    //body 
    var heights = [];
    $('#form\\:permissionTable_frozenTbody').find('> tr:visible').each(function(i) {
        heights[i] = $(this).height();
    });

    // get visible table one rows before we remove it from the dom
    var tableOneRows = $('#form\\:permissionTable_scrollableTbody').find('tr:visible'); 

    $.each(tableOneRows, function(i) { 
        $(this).height(heights[i]);
    });

}  