package Controlador;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import Modelo.ClsModeloLeccion;
import ModeloDAO.ClsModeloDaoLeccion;
import java.util.List;
import javax.servlet.RequestDispatcher;

@WebServlet(name = "LeccionServlet", urlPatterns = {"/LeccionServlet"})
public class LeccionServlet extends HttpServlet {

    //SOLUCION ERROR
    private static final String PARAM_ID_LECCION = "idLeccion";
    private static final String LISTAR_LECCIONES_URL_PREFIX = "admin/lecciones/listarLecciones.jsp?idCurso=";
    private static final String ERROR_PAGE = "error.jsp";
    private static final String PARAM_FK_ID_CURSO = "FKidCurso";



    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("accion");
                String ruta = request.getParameter("ruta");
        String page = "index.jsp";

        if (action != null) {
            switch (action) {
                case "listarLecciones":
                        if("app".equals(ruta)){
                            page = "app/learn/lecciones.jsp";
                        
                        }
                        else if("admin".equals(ruta)){
                            page = "admin/lecciones/listarLecciones.jsp";
                        
                        }else{
                          // Lógica para listar archivos
                            page = "app/index.jsp";
                            System.out.println("Estamos en listar idiomas");
                        }
                    break;
                case "agregarLecciones":
                    page = "admin/lecciones/agregarLecciones.jsp";
                    break;
                case "editarLecciones":
                    page = "admin/lecciones/editarLecciones.jsp";
                    break;
                case "eliminarLeccion":
                    String idLeccionEliminar = request.getParameter(PARAM_ID_LECCION);
                    ClsModeloDaoLeccion daoLeccionEliminar = new ClsModeloDaoLeccion();
                    boolean exitoEliminacion = daoLeccionEliminar.eliminarLeccion(idLeccionEliminar);

                    if (exitoEliminacion) {
                        String idCurso = request.getParameter("idCurso");
                        page = LISTAR_LECCIONES_URL_PREFIX + idCurso;
                    } else {
                        page = ERROR_PAGE;
                    }
                    break;
                default:
                    page = ERROR_PAGE;
                    break;
            }
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher(page);
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("accion");
        String page = "index.jsp";

        if (action != null) {
            switch (action) {
                case "agregarLecciones":
                    ClsModeloLeccion nuevaLeccion = new ClsModeloLeccion();
                    nuevaLeccion.setIdLeccion(request.getParameter(PARAM_ID_LECCION));
                    nuevaLeccion.setFKidCurso(request.getParameter(PARAM_FK_ID_CURSO));
                    nuevaLeccion.setFKidIdioma(Integer.parseInt(request.getParameter("FKidIdioma")));
                    nuevaLeccion.setTitulo(request.getParameter("titulo"));
                    nuevaLeccion.setUrlBanner(request.getParameter("urlBanner"));

                    ClsModeloDaoLeccion daoLeccion = new ClsModeloDaoLeccion();
                    boolean exito = daoLeccion.agregarLeccion(nuevaLeccion);

                    if (exito) {
                        String idCurso = request.getParameter(PARAM_FK_ID_CURSO);
                        page = LISTAR_LECCIONES_URL_PREFIX + idCurso;
                    } else {
                        page = ERROR_PAGE;
                    }
                    break;
                case "actualizarLeccion":
                    String idLeccion = request.getParameter(PARAM_ID_LECCION);
                    String FKidCurso = request.getParameter(PARAM_FK_ID_CURSO);
                    int FKidIdioma = Integer.parseInt(request.getParameter("FKidIdioma"));
                    String titulo = request.getParameter("titulo");
                    String urlBanner = request.getParameter("urlBanner");

                    ClsModeloLeccion leccionActualizada = new ClsModeloLeccion(idLeccion, FKidCurso, FKidIdioma, titulo, urlBanner);

                    ClsModeloDaoLeccion daoLeccionActualizar = new ClsModeloDaoLeccion();
                    boolean exitoActualizacion = daoLeccionActualizar.actualizarLeccion(leccionActualizada);

                    if (exitoActualizacion) {
                       page = LISTAR_LECCIONES_URL_PREFIX + FKidCurso;
                        System.out.println("rutaa:"+page);
                    } else {
                        page = ERROR_PAGE;
                    }
                    break;
                default:
                    page = ERROR_PAGE;
                    break;
            }
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher(page);
        dispatcher.forward(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Descripción corta";
    }
}
