$(document).ready(function(){
    $('#btn-search').click(function(){
        location.href="/search/" + $('#search-content').val() + "/1";
    });
});
