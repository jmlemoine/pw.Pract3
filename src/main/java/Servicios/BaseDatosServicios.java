
package Servicios;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BaseDatosServicios {
    private static BaseDatosServicios baseDatos;
    private String URL = "jdbc:h2:tcp://localhost/~/manga-anime-empire";

    // Obtener una instancia de la base de datos en el caso de que no exista.

    public static BaseDatosServicios getInstancia() {
        if (baseDatos == null)
            baseDatos = new BaseDatosServicios();

        return baseDatos;
    }

    // Obtener una conexión de la base de datos para ejeuctar statements y demás.

    public Connection getConexion() {
        Connection conexion = null;

        try {
            conexion = DriverManager.getConnection(URL, "sa", "");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return conexion;
    }

    // Probar la conexión con la base de datos para probar que la aplicación pueda correr correctamente

    public void testConexion() {
        try {
            getConexion().close();
            System.out.println("Conexión realizado con exito...");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}
