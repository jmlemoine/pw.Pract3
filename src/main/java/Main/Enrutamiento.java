package Main;

import Objetos.Articulo;
import Objetos.Etiqueta;
import Objetos.Usuario;
import Servicios.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;
import org.jasypt.util.text.StrongTextEncryptor;
import spark.Session;

import javax.security.auth.login.Configuration;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;
import static sun.rmi.transport.TransportConstants.Version;

public class Enrutamiento {

    static ArrayList<Etiqueta> etiquetasAux = new ArrayList<>();
    static ArrayList<Articulo> articulos = ArticuloServicios.listarArticulos();
    static String nombreUsuario = "";
    static Boolean etiquetasBool = false;
    static Usuario usuario;

    public static void crearRutas() {
        final Configuration configuration = new Configuration(new Version(2, 3, 23));
        configuration.setClassForTemplateLoading(Main.class, "/");

        staticFiles.location("/publico");

        before("/", (req, res) -> {
            if (req.cookie("sesionSemanal") != null) {
                StrongTextEncryptor encriptador = new StrongTextEncryptor();
                encriptador.setPassword("manga-anime-empire");
                String sesionSemanal = encriptador.decrypt(req.cookie("sesionSemanal"));

                Usuario usuarioRestaurado = UsuarioServicios.restaurarSesion(sesionSemanal);
                nombreUsuario = usuarioRestaurado.getUsername();
                usuario = usuarioRestaurado;
                req.session().attribute("sesionUsuario", usuarioRestaurado);

                if (usuarioRestaurado != null) {
                    req.session(true);
                    req.session().attribute("sesionUsuario", usuarioRestaurado);
                }
            }

            if (req.session().attribute("sesionUsuario") == null) {
                res.redirect("/login");
            }
        });

        before("/registrar", (req, res) -> {
            if (req.session().attribute("sesionUsuario") == null) {
                res.redirect("/");
            }
            if (!usuario.isAdminstrator()) {
                res.redirect("/");
            }
        });

        before("/articulo/crear", (req, res) -> {
            if (req.session().attribute("sesionUsuario") == null) {
                res.redirect("/");
            }

            if (!usuario.isAdminstrator()) {
                if (!usuario.isAutor()) {
                    res.redirect("/");
                }
            }
        });

        before("/articulo/editar/:id", (req, res) -> {
            if (req.session().attribute("sesionUsuario") == null) {
                res.redirect("/");
            }
            if (!usuario.isAdminstrator()) {
                if (!usuario.isAutor()) {
                    res.redirect("/");
                }
            }
        });

        before("/articulo/eliminar/:id", (req, res) -> {
            if (req.session().attribute("sesionUsuario") == null) {
                res.redirect("/");
            }

            if (!usuario.isAdminstrator()) {
                if (!usuario.isAutor()) {
                    res.redirect("/");
                }
            }
        });

        before("articulo/:id", (req, res) -> {
            if (req.session().attribute("sesionUsuario") == null) {
                res.redirect("/login");
            }
        });

        get("/", (req, res) -> {
            StringWriter writer = new StringWriter();
            Map<String, Object> atributos = new HashMap<>();
            Template template = configuration.getTemplate("plantillas/Index.ftl");

            articulos = ArticuloServicios.listarArticulos();

            Articulo articulo = new Articulo();
            articulo.setListaEtiquetas(etiquetasAux);

            atributos.put("articulos", articulos);
            atributos.put("estaLogueado", req.session().attribute("sesionUsuario") != null);
            atributos.put("nombreUsuario", nombreUsuario);
            atributos.put("tienePermisos", usuario.isAdminstrator() || usuario.isAutor());
            atributos.put("esAdmin", usuario.isAdminstrator());
            /*
            atributos.put("etiquetasBool", etiquetasBool);

            if(etiquetasBool) {
                atributos.put("etiquetas", articulo.getListaEtiquetas().get(1));
            }
            */
            template.process(atributos, writer);

            return writer;
        });

        get("/login", (req, res) -> {
            StringWriter writer = new StringWriter();
            Map<String, Object> atributos = new HashMap<>();
            Template template = configuration.getTemplate("plantillas/Login.ftl");
            template.process(atributos, writer);

            return writer;
        });

        post("/login", (req, res) -> {
            try {
                nombreUsuario = req.queryParams("username");
                String contrasena = req.queryParams("password");
                usuario = UsuarioServicios.elUsuarioExiste(nombreUsuario, contrasena);

                if (usuario != null) {
                    req.session().attribute("sesionUsuario", usuario);

                    if (req.queryParams("guardarSesion") != null) {
                        String sesionID = req.session().id();
                        StrongTextEncryptor encriptador = new StrongTextEncryptor();
                        encriptador.setPassword("manga-anime-empire");
                        String sesionIDEncriptado = encriptador.encrypt(sesionID);

                        System.out.println("Sesión sin encriptar: " + sesionID);
                        System.out.println("Sesión encriptada: " + sesionIDEncriptado);

                        res.cookie("/", "sesionSemanal", sesionIDEncriptado, 604800, false);
                        UsuarioServicios.guardarSesion(req.session().id(), usuario.getId());
                    }

                    res.redirect("/");
                } else {
                    res.redirect("/login");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        });

        get("/registrar", (req, res) -> {
            StringWriter writer = new StringWriter();
            Map<String, Object> atributos = new HashMap<>();
            Template template = configuration.getTemplate("plantillas/Registro.ftl");

            atributos.put("estaLogueado", req.session().attribute("sesionUsuario") != null);
            atributos.put("nombreUsuario", nombreUsuario);
            atributos.put("tienePermisos", usuario.isAdminstrator() || usuario.isAutor());
            atributos.put("esAdmin", usuario.isAdminstrator());

            template.process(atributos, writer);

            return writer;
        });

        post("/registrarUsuario", (req, res) -> {
            String nombreUsuario = req.queryParams("username");
            String contrasena = req.queryParams("password");
            Long id = UsuarioServicios.conseguirTamano() + 1;
            Usuario usuarioNuevo = UsuarioServicios.elUsuarioExiste(nombreUsuario, contrasena);
            UsuarioServicios servicioUsuario = new UsuarioServicios();
            boolean seraAutor = false;
            boolean seraAdmin = false;

            if (req.queryParams("seraAutor") != null) {
                seraAutor = true;
            }

            if (req.queryParams("seraAdmin") != null) {
                seraAdmin = true;
                seraAutor = true;
            }

            if (usuarioNuevo == null) {
                servicioUsuario.registrarUsuarios(id, "'" + nombreUsuario + "'", "'" + contrasena + "'", seraAdmin, seraAutor);
                res.redirect("/");
            } else {
                res.redirect("/registrar");
            }

            return null;
        });

        get("/salir", (req, res) ->
        {
            Session sesion = req.session(true);
            sesion.invalidate();

            res.removeCookie("sesionSemanal");

            res.redirect("/");

            return null;
        });

        path("/articulo", () -> {
            get("/crear", (req, res) -> {
                if (usuario.isAutor()) {
                    StringWriter writer = new StringWriter();
                    Map<String, Object> atributos = new HashMap<>();
                    Template template = configuration.getTemplate("plantillas/CrearArticulo.ftl");

                    atributos.put("estaLogueado", req.session().attribute("sesionUsuario") != null);
                    atributos.put("nombreUsuario", nombreUsuario);
                    atributos.put("tienePermisos", usuario.isAdminstrator() || usuario.isAutor());
                    atributos.put("esAdmin", usuario.isAdminstrator());
                    template.process(atributos, writer);

                    return writer;
                }
                res.redirect("/");
                return null;
            });

            post("/crear", (req, res) -> {
                if (usuario.isAutor()) {
                    long idArticulo = ArticuloServicios.conseguirTamano() + 1;
                    String titulo = req.queryParams("titulo");
                    String cuerpo = req.queryParams("cuerpo");
                    long usuarioID = usuario.getId();

                    LocalDate fecha = new java.sql.Date(new java.util.Date().getTime()).toLocalDate();
                    ArticuloServicios.crearArticulo(idArticulo, titulo, cuerpo, usuarioID, fecha);

                    String[] etiquetas = req.queryParams("etiquetas").split(",");

                    long articuloID = ArticuloServicios.buscarArticulo(idArticulo).getId();
                    long etiquetaIDAux;

                    for (int i = 0; i < etiquetas.length; i++) {
                        if (EtiquetaServicios.conseguirID("select * from etiquetas;") != -1) {
                            etiquetaIDAux = EtiquetaServicios.conseguirID("select * from etiquetas;") + 1;
                        } else {
                            etiquetaIDAux = 1;
                        }
                        BootStrapServicios.ejecutarSQL("MERGE INTO etiquetas \n" +
                                "KEY(ID) \n" +
                                "VALUES (" + etiquetaIDAux + ", " + "'" + etiquetas[i] + "');");
                        long etiquetaID = EtiquetaServicios.conseguirID("select * from etiquetas where etiqueta = '" + etiquetas[i] + "';");
                        BootStrapServicios.ejecutarSQL("insert into articulosYetiquetas (articulo, etiqueta) values(" + articuloID + ", " + etiquetaID + ");");
                    }
                }
                res.redirect("/");

                return null;
            });

            get("/editar/:id", (req, res) -> {
                StringWriter writer = new StringWriter();
                Map<String, Object> atributos = new HashMap<>();
                Template template = configuration.getTemplate("plantillas/EditarArticulo.ftl");

                Articulo articulo = ArticuloServicios.buscarArticulo(Long.parseLong(req.params("id")));

                atributos.put("articulo", articulo);
                atributos.put("estaLogueado", req.session().attribute("sesionUsuario") != null);
                atributos.put("nombreUsuario", nombreUsuario);
                atributos.put("tienePermisos", usuario.isAdminstrator() || usuario.isAutor());
                atributos.put("esAdmin", usuario.isAdminstrator());
                template.process(atributos, writer);

                return writer;
            });

            post("/editar/:id", (req, res) -> {
                long id = Integer.parseInt(req.params("id"));
                String titulo = req.queryParams("titulo");
                String cuerpo = req.queryParams("cuerpo");
                long usuarioID = usuario.getId();

                LocalDate fecha = new java.sql.Date(new java.util.Date().getTime()).toLocalDate();
                ArticuloServicios.crearArticulo(id, titulo, cuerpo, usuarioID, fecha);

                String[] etiquetas = req.queryParams("etiquetas").split(",");

                long etiquetaIDAux;

                BootStrapServicios.ejecutarSQL("DELETE FROM articulosyetiquetas WHERE articulo = " + id);

                for (int i = 0; i < etiquetas.length; i++) {
                    if (EtiquetaServicios.conseguirID("select * from etiquetas;") != -1) {
                        etiquetaIDAux = EtiquetaServicios.conseguirID("select * from etiquetas;") + 1;
                    } else {
                        etiquetaIDAux = 1;
                    }
                    BootStrapServicios.ejecutarSQL("MERGE INTO etiquetas \n" +
                            "KEY(ID) \n" +
                            "VALUES (" + etiquetaIDAux + ", " + "'" + etiquetas[i] + "');");
                    long etiquetaID = EtiquetaServicios.conseguirID("select * from etiquetas where etiqueta = '" + etiquetas[i] + "';");
                    BootStrapServicios.ejecutarSQL("insert into articulosYetiquetas (articulo, etiqueta) values(" + id + ", " + etiquetaID + ");");
                }

                res.redirect("/");

                return null;
            });
            get("/eliminar/:id", (req, res) -> {
                if (usuario.isAdminstrator() || usuario.isAutor()) {
                    StringWriter writer = new StringWriter();
                    Map<String, Object> atributos = new HashMap<>();
                    Template template = configuration.getTemplate("plantillas/EliminarArticulo.ftl");

                    Articulo articulo = ArticuloServicios.buscarArticulo(Long.parseLong(req.params("id")));

                    atributos.put("articulo", articulo);
                    atributos.put("estaLogueado", req.session().attribute("sesionUsuario") != null);
                    atributos.put("nombreUsuario", nombreUsuario);
                    atributos.put("tienePermisos", usuario.isAdminstrator() || usuario.isAutor());
                    atributos.put("esAdmin", usuario.isAdminstrator());
                    template.process(atributos, writer);

                    return writer;
                }
                res.redirect("/");
                return null;
            });

            post("/eliminar/:id", (req, res) -> {
                if (usuario.isAdminstrator() || usuario.isAutor()) {
                    BootStrapServicios.ejecutarSQL("DELETE FROM comentarios where articuloid = " + req.params("id"));
                    ArrayList<Long> etiquetasID = EtiquetaServicios.conseguirIDEtiquetas(Long.parseLong(req.params("id")));

                    for (Long etiqueta : etiquetasID) {
                        BootStrapServicios.ejecutarSQL("DELETE FROM articulosyetiquetas where articulo = " + req.params("id"));
                        BootStrapServicios.ejecutarSQL("DELETE FROM etiquetas where id = " + etiqueta);
                    }

                    ArticuloServicios.eliminarArticulo(Long.parseLong(req.params("id")));
                }
                res.redirect("/");
                return null;
            });

            get("/:id", (req, res) -> {
                StringWriter writer = new StringWriter();
                Map<String, Object> atributos = new HashMap<>();
                Template template = configuration.getTemplate("plantillas/Articulo.ftl");

                Articulo articulo = ArticuloServicios.buscarArticulo(Long.parseLong(req.params("id")));

                atributos.put("articulo", articulo);
                atributos.put("estaLogueado", req.session().attribute("sesionUsuario") != null);
                atributos.put("nombreUsuario", nombreUsuario);
                atributos.put("tienePermisos", usuario.isAdminstrator() || usuario.isAutor());
                atributos.put("esAdmin", usuario.isAdminstrator());
                template.process(atributos, writer);

                return writer;
            });

            post("/:id/comentar", (req, res) -> {
                Long id = ComentarioServicios.conseguirTamano() + 1;
                Long articuloID = Long.parseLong(req.params("id"));
                String comentario = req.queryParams("comentario");
                Long autor = usuario.getId();

                ComentarioServicios.crearComentario(id, comentario, autor, articuloID);

                res.redirect("/articulo/" + articuloID);
                return null;
            });
        });

        notFound((req, res) -> {
            StringWriter writer = new StringWriter();
            Template template = configuration.getTemplate("plantillas/404.ftl");
            Map<String, Object> atributos = new HashMap<>();
            atributos.put("estaLogueado", req.session().attribute("sesionUsuario") != null);
            atributos.put("nombreUsuario", nombreUsuario);
            atributos.put("tienePermisos", usuario.isAdminstrator() || usuario.isAutor());
            atributos.put("esAdmin", usuario.isAdminstrator());

            template.process(atributos, writer);
            res.status(404);
            res.body(writer.toString());

            return writer;
        });
    }


}

