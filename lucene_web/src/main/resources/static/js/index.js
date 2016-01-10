$(window).resize(function(){
    $('.screen-center').css("padding-top", ($(window).height() - $('.screen-center').outerHeight())/2 + $(document).scrollTop());
});

$(document).ready(function(){
    $(window).resize()
});
