package Main;

import Servicios.BaseDatosServicios;
import Servicios.BootStrapServicios;
import Servicios.UsuarioServicios;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            // Iniciando el servicio de Base de datos
            BootStrapServicios.iniciarBaseDatos();

            // Prueba de conexión
            BaseDatosServicios.getInstancia().testConexion();

            // Creando tablas de la Base de datos
            BootStrapServicios.crearTablas();

            // Crear usuario por defecto
            UsuarioServicios serviciouser = new UsuarioServicios();
            serviciouser.crearUsuarioPorDefecto();

            Enrutamiento.crearRutas();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}