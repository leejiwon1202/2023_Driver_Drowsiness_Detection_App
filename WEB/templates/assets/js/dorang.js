/*!
=========================================================
* Dorang Landing page
=========================================================

* Copyright: 2019 DevCRUD (https://devcrud.com)
* Licensed: (https://devcrud.com/licenses)
* Coded by www.devcrud.com

=========================================================

* The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
*/

 // toggle 
$(document).ready(function(){
  $('.modal-toggle').click(function(){
    $('.modalBox').toggleClass('show');
    $('.modalBox-iframe').attr("src", "/video");
  })
  $('.modalBox').click(function(){
    $(this).removeClass('show');
    $('.modalBox-iframe').attr("src", "/");
  });
});


// smooth scroll
$(document).ready(function(){
  $(".navbar .nav-link").on('click', function(event) {
    if (this.hash !== "") {
      event.preventDefault();
      var hash = this.hash;
      $('html, body').animate({
        scrollTop: $(hash).offset().top
      }, 700, function(){
        window.location.hash = hash;
      });
    } 
  });
}); 


$(document).ready(function () {
    var socket = io();
    socket.on('response', function (msg, cb) {
        Swal.fire({
            title: 'WARNING !!!',
            text: "눈을 깜빡여주시길 바랍니다.",
            icon: 'warning',
            confirmButtonColor: '#d33',
            confirmButtonText: '확인',
        })
    });
});

