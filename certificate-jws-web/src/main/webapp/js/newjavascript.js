$("#enviar").click(function () {
//Pega o valor do arquivo escolhido
    var id = [];
    $.each($("input[name='arquivo']:checked"), function(){            
    	id.push($(this).val());
    });
    $.get("api/token/generate/" + id, function (data) {
        $("#hash").val(data);
        $('#jnlpForm').submit();
    });

});