$("#enviar").click(function () {
//Pega o valor do arquivo escolhido
    var id = [];
    $.each($("input[name='arquivo']:checked"), function(){            
    	id.push($(this).val());
    });
    $.getJSON("api/token/generate/" + id, function (data) {
        $("#hash").val(data.message);
        $('#jnlpForm').submit();
    });

});