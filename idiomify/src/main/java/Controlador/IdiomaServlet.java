/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controlador;

import Modelo.*;
import ModeloDAO.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
//LIBRERIAS PARA EGENRAR PEF
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;

@WebServlet(name = "IdiomaServlet", urlPatterns = { "/IdiomaServlet" })
public class IdiomaServlet extends HttpServlet {
    private static final String ADMIN_LISTAR_IDIOMAS_PAGE = "admin/idiomas/listarIdiomas.jsp";
    private static final Logger logger = Logger.getLogger(IdiomaServlet.class.getName());
    private static final String ERROR_PAGE = "error.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("accion");
        String ruta = request.getParameter("ruta");
        String page = "index.jsp"; // Default page

        if (action != null) {
            switch (action) {
                case "listarIdiomas":
                    if ("app".equals(ruta)) {
                        page = "app/learn/idiomas.jsp";
                    } else if ("admin".equals(ruta)) {
                        page = ADMIN_LISTAR_IDIOMAS_PAGE;
                    } else {
                        page = "app/index.jsp";
                        // Utiliza el logger para imprimir el mensaje
                        logger.info("Estamos en listar idiomas");
                    }
                    break;
                case "inicio":
                    page = ("app".equals(ruta)) ? "app/index.jsp" : "admin/index.jsp";
                    break;
                case "agregarIdiomas":
                    page = "admin/idiomas/agregarIdiomas.jsp";
                    break;
                case "editarIdiomas":
                    try {
                        handleEditIdiomas(request, response);
                    } catch (ServletException | IOException e) {
                        // Manejar las excepciones ServletException e IOException aquí
                        e.printStackTrace(); // Otra forma de manejar las excepciones, puedes personalizarlo según tus
                                             // necesidades
                    }
                    return;
                case "exportarPdf":
                    exportarPdf(response);
                    return;
                case "exportarCsv":
                    exportarCsv(response);
                    return;
                case "eliminarIdioma":

                    try {
                        handleEliminarIdioma(request, response);
                    } catch (ServletException | IOException e) {
                        // Manejar las excepciones ServletException e IOException aquí
                        e.printStackTrace(); // Otra forma de manejar las excepciones, puedes personalizarlo según tus
                                             // necesidades
                    }
                    return;
                default:
                    page = ERROR_PAGE;
                    break;
            }
        }

        try {
            RequestDispatcher dispatcher = request.getRequestDispatcher(page);
            dispatcher.forward(request, response);
        } catch (ServletException | IOException e) {
            // Manejar las excepciones ServletException e IOException aquí
            e.printStackTrace(); // Otra forma de manejar las excepciones, puedes personalizarlo según tus
                                 // necesidades
        }
    }

    private void handleEditIdiomas(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idIdiomaStr = request.getParameter("idIdioma");

        if (idIdiomaStr != null) {
            try {
                int idIdioma = Integer.parseInt(idIdiomaStr);
                ClsModeloDaoIdioma dao = new ClsModeloDaoIdioma();
                ClsModeloIdioma idioma = dao.obtenerIdiomaPorId(idIdioma);

                if (idioma != null) {
                    request.setAttribute("idioma", idioma);
                    request.getRequestDispatcher("admin/idiomas/editarIdiomas.jsp").forward(request, response);
                } else {
                    request.getRequestDispatcher(ERROR_PAGE).forward(request, response);
                }
            } catch (NumberFormatException e) {
                request.getRequestDispatcher(ERROR_PAGE).forward(request, response);
            }
        } else {
            request.getRequestDispatcher(ERROR_PAGE).forward(request, response);
        }
    }

    private void exportarPdf(HttpServletResponse response) {
        ClsModeloDaoIdioma dao = new ClsModeloDaoIdioma();
        List<ClsModeloIdioma> idiomas = dao.obtenerTodosIdiomas();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=ListadoIdiomas.pdf");

        Document document = new Document();
        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            for (ClsModeloIdioma idioma : idiomas) {
                document.add(new Paragraph("ID: " + idioma.getIdIdioma()));
                document.add(new Paragraph("Nombre: " + idioma.getNombre()));
                document.add(new Paragraph("Descripción: " + idioma.getDescripcion()));
                document.add(new Paragraph("URL Banner: " + idioma.getUrlBanner()));
                document.add(new Paragraph("----------------------------------------"));
            }
        } catch (DocumentException | IOException ex) {
            Logger.getLogger(IdiomaServlet.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    private void exportarCsv(HttpServletResponse response) {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=ListadoIdiomas.csv");

        try (CSVWriter writer = new CSVWriter(response.getWriter())) {
            writer.writeNext(new String[] { "ID", "Nombre", "Descripción", "URL Banner" });

            ClsModeloDaoIdioma daoIdioma = new ClsModeloDaoIdioma();
            List<ClsModeloIdioma> idiomas = daoIdioma.obtenerTodosIdiomas();

            for (ClsModeloIdioma idioma : idiomas) {
                String[] rowData = {
                        String.valueOf(idioma.getIdIdioma()),
                        idioma.getNombre(),
                        idioma.getDescripcion(),
                        idioma.getUrlBanner()
                };
                writer.writeNext(rowData);
            }
        } catch (IOException ex) {
            Logger.getLogger(IdiomaServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void handleEliminarIdioma(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int idIdiomaEliminar = Integer.parseInt(request.getParameter("idIdioma"));
        ClsModeloDaoIdioma daoIdiomaEliminar = new ClsModeloDaoIdioma();
        boolean exitoEliminacion = daoIdiomaEliminar.eliminarIdioma(idIdiomaEliminar);

        if (exitoEliminacion) {
            response.sendRedirect(ADMIN_LISTAR_IDIOMAS_PAGE);
        } else {
            request.getRequestDispatcher(ERROR_PAGE).forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("accion");
        String page = "index.jsp"; // Página predeterminada

        if (action != null) {
            switch (action) {
                case "agregarIdioma":
                    // Crear un objeto ClsModeloIdioma y asignar los datos del formulario
                    ClsModeloIdioma nuevoIdioma = new ClsModeloIdioma();
                    nuevoIdioma.setNombre(request.getParameter("nombre"));
                    nuevoIdioma.setDescripcion(request.getParameter("descripcion"));
                    nuevoIdioma.setUrlBanner(request.getParameter("urlBanner"));

                    // Crear una instancia de ClsModeloDaoIdioma y agregar el idioma
                    ClsModeloDaoIdioma daoIdioma = new ClsModeloDaoIdioma();
                    boolean exito = daoIdioma.agregarIdioma(nuevoIdioma);

                    if (exito) {
                        // La inserción fue exitosa, puedes redirigir a la página de listado de idiomas
                        page = ADMIN_LISTAR_IDIOMAS_PAGE;
                    } else {
                        // Manejar el caso en que la inserción no fue exitosa (por ejemplo, mostrar un
                        // mensaje de error)
                        page = ERROR_PAGE;
                    }
                    break;
                case "actualizarIdioma":
                    try {
                        // Obtener los datos actualizados del idioma desde los parámetros de la
                        // solicitud
                        int idIdioma = Integer.parseInt(request.getParameter("idIdioma"));
                        String nombre = request.getParameter("nombre");
                        String descripcion = request.getParameter("descripcion");
                        String urlBanner = request.getParameter("urlBanner");

                        ClsModeloIdioma idiomaActualizado = new ClsModeloIdioma(idIdioma, nombre, descripcion,
                                urlBanner);

                        // Crear una instancia de ClsModeloDaoIdioma y actualizar el idioma
                        ClsModeloDaoIdioma daoIdiomaActualizar = new ClsModeloDaoIdioma();
                        boolean exitoActualizacion = daoIdiomaActualizar.actualizarIdioma(idiomaActualizado);

                        if (exitoActualizacion) {
                            // La actualización fue exitosa, puedes redirigir a la página de listado de
                            // idiomas o a otra página
                            page = ADMIN_LISTAR_IDIOMAS_PAGE;
                        } else {
                            // Manejar el caso en que la actualización no fue exitosa (por ejemplo, mostrar
                            // un mensaje de error)
                            page = ERROR_PAGE;
                        }
                    } catch (NumberFormatException e) {
                        // Manejar la excepción NumberFormatException aquí
                        e.printStackTrace(); // Otra forma de manejar la excepción, puedes personalizarlo según tus
                                             // necesidades
                        page = ERROR_PAGE;
                    }
                    break;
                case "insertarIdiomaCSV":
                    if (ServletFileUpload.isMultipartContent(request)) {
                        DiskFileItemFactory factory = new DiskFileItemFactory();
                        ServletFileUpload upload = new ServletFileUpload(factory);

                        try {
                            List<FileItem> items = upload.parseRequest(request);
                            for (FileItem item : items) {
                                if (!item.isFormField()) {
                                    // Lógica para procesar el contenido del archivo CSV
                                    try (CSVReader csvReader = new CSVReader(
                                            new BufferedReader(new InputStreamReader(item.getInputStream())))) {
                                        csvReader.skip(1);
                                        String[] linea;
                                        while ((linea = csvReader.readNext()) != null) {
                                            ClsModeloIdioma nuevoIdiomacsv = new ClsModeloIdioma();
                                            nuevoIdiomacsv.setNombre(linea[0]);
                                            nuevoIdiomacsv.setDescripcion(linea[1]);
                                            nuevoIdiomacsv.setUrlBanner(linea[2]);

                                            ClsModeloDaoIdioma daoIdiomacsv = new ClsModeloDaoIdioma();
                                            daoIdiomacsv.agregarIdioma(nuevoIdiomacsv);
                                        }
                                    }
                                }
                            }
                        } catch (FileUploadException ex) {
                            // Manejar la excepción FileUploadException
                            Logger.getLogger(IdiomaServlet.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (CsvValidationException | IOException ex) {
                            // Manejar las excepciones CsvValidationException e IOException
                            Logger.getLogger(IdiomaServlet.class.getName()).log(Level.SEVERE, null, ex);
                        } finally {
                            // Redirige a la página de listado de idiomas después de la importación
                            page = ADMIN_LISTAR_IDIOMAS_PAGE;
                        }
                    } else {
                        page = ADMIN_LISTAR_IDIOMAS_PAGE;
                        logger.info("No se encontró el archivo CSV en la solicitud");
                        // Manejar el caso en que no se encuentra el archivo CSV
                    }
                    break;
                default:
                    // Lógica para manejar otros casos o errores
                    page = ERROR_PAGE;
                    break;
            }

        }

        try {
            RequestDispatcher dispatcher = request.getRequestDispatcher(page);
            dispatcher.forward(request, response);
        } catch (ServletException | IOException e) {
            // Manejar las excepciones ServletException e IOException aquí
            e.printStackTrace(); // Otra forma de manejar las excepciones, puedes personalizarlo según tus
                                 // necesidades
        }

    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
