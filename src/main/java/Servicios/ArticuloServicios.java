
package Servicios;

import Objetos.Articulo;
import Objetos.Comentario;
import Objetos.Etiqueta;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class ArticuloServicios  {
    public static ArrayList<Articulo> listarArticulos() {
        Connection conexion = BaseDatosServicios.getInstancia().getConexion();
        ArrayList<Articulo> articulos = new ArrayList<>();

        try {
            // Consultando todos los articulos.
            String articulosQuery = "SELECT * FROM articulos ORDER BY fecha DESC;";

            // Ejecuta el query pasado por parámetro "usuarioDefecto".
            Statement statement = conexion.createStatement();
            ResultSet resultado = statement.executeQuery(articulosQuery);

            while(resultado.next()) {
                ArrayList<Comentario> comentarios = ComentarioServicios.listarComentarios(resultado.getLong("id"));
                ArrayList<Etiqueta> etiquetas = EtiquetaServicios.conseguirEtiquetas(resultado.getLong("id"));

                articulos.add(
                        new Articulo(resultado.getLong("id"),
                                resultado.getNString("titulo"),
                                resultado.getNString("cuerpo"),
                                UsuarioServicios.buscarUsuario(resultado.getLong("usuarioid")),
                                resultado.getDate("fecha"),
                                comentarios,
                                etiquetas
                        )
                );
            }

            statement.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally{
            try {
                conexion.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return articulos;
    }

    public static Articulo buscarArticulo(long id) {
        Articulo articulo = null;
        Connection conexion = BaseDatosServicios.getInstancia().getConexion();

        try {
            // Crear un artículo si no existe y si existe lo actualiza.
            String articuloEncontrado = "SELECT * FROM articulos WHERE id = " + id + ";";

            // Ejecutar el query pasado por parámetro "usuarioDefecto".
            PreparedStatement prepareStatement = conexion.prepareStatement(articuloEncontrado);
            ResultSet rs = prepareStatement.executeQuery();

            while(rs.next()) {
                ArrayList<Comentario> comentarios = ComentarioServicios.listarComentarios(rs.getLong("id"));
                ArrayList<Etiqueta> etiquetas = EtiquetaServicios.conseguirEtiquetas(rs.getLong("id"));

                // TODO Conseguir los verdaderos datos del usuario
                articulo = new Articulo(
                        rs.getLong("id"),
                        rs.getNString("titulo"),
                        rs.getNString("cuerpo"),
                        UsuarioServicios.buscarUsuario(rs.getLong("usuarioid")),
                        rs.getDate("fecha"),
                        comentarios, etiquetas
                );
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally{
            try {
                conexion.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return articulo;
    }

    public static boolean crearArticulo(long id, String titulo, String cuerpo, long usuarioID, LocalDate fecha) {
        boolean creadoCorrectamente = false;
        Connection conexion = BaseDatosServicios.getInstancia().getConexion();

        try {
            // Crearlo si no existe y si existe actualizarlo.
            String articuloNuevo = "MERGE INTO articulos \n" +
                    "KEY(ID) \n" +
                    "VALUES (" + id + ",'" + titulo + "','" + cuerpo + "'," + usuarioID + ",'" + fecha + "');";

            // Ejecutar el query pasado por parámetro "usuarioDefecto".
            PreparedStatement prepareStatement = conexion.prepareStatement(articuloNuevo);

            // Si se ejecutó el query bien pues la cantidad de filas de la tabla debe ser mayor a 0, pues se ha insertado una fila.
            int fila = prepareStatement.executeUpdate();
            creadoCorrectamente = fila > 0 ;
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally{
            try {
                conexion.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return creadoCorrectamente;
    }

    public static void eliminarArticulo(Long id) {
        Connection conexion = BaseDatosServicios.getInstancia().getConexion();
        ArrayList<Articulo> articulos = new ArrayList<>();
        boolean creadoCorrectamente;

        try {
            // Consultando y eliminando el articulo que tenga el id indicando.
            String eliminarArticuloQuery = "DELETE FROM articulos where ID = " + id + ";";

            // Ejecutar el query pasado por parámetro "usuarioDefecto".
            PreparedStatement prepareStatement = conexion.prepareStatement(eliminarArticuloQuery);

            // Si se ejecutó el query bien pues la cantidad de filas de la tabla debe ser mayor a 0, pues se ha insertado una fila.
            int fila = prepareStatement.executeUpdate();
            creadoCorrectamente = fila > 0 ;

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally{
            try {
                conexion.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static Long conseguirTamano() {
        Long ultimoID = new Long(0);
        Connection conexion = BaseDatosServicios.getInstancia().getConexion();

        try {
            // Crearlo si no existe y si existe actualizarlo.
            String conseguirTamanoTabla = "SELECT TOP 1 * FROM articulos ORDER BY ID DESC;";

            // Ejecutar el query pasado por parámetro "usuarioDefecto".
            PreparedStatement prepareStatement = conexion.prepareStatement(conseguirTamanoTabla);
            ResultSet resultado = prepareStatement.executeQuery();
            while(resultado.next()){
                ultimoID = resultado.getLong("id");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally{
            try {
                conexion.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return ultimoID;
    }
}
