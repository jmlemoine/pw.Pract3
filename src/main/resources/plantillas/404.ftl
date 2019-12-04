<#import "/plantillas/Base.ftl" as base>
<@base.pagina logueado=estaLogueado usuario=nombreUsuario fondo="404-fondo.png" permisos=tienePermisos>
    <div class="col-12 no-encontrado py-5 px-1">
        <h5 class="upbottom-letters text-white text-shadow float-right">
            No se encontró
            <br>
            el recurso que querías
            <br>
            VE A <a href="/" class="text-white"><strong>Inicio</strong></a>
        </h5>
        <h5 class="upbottom-letters text-white text-shadow float-left">
            Estado 404
        </h5>
    </div>
</@base.pagina>