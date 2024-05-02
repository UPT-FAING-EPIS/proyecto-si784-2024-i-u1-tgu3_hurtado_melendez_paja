/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package Controlador;

import Modelo.*;
import ModeloDAO.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

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

import java.io.FileWriter;
import java.util.Iterator;

@WebServlet(name = "IdiomaServlet", urlPatterns = { "/IdiomaServlet" })
public class IdiomaServlet extends HttpServlet {

    //SOLUCION ERROR
    private static final String LISTAR_IDIOMAS_PAGE = "admin/idiomas/listarIdiomas.jsp";
    private static final String PARAM_ID_IDIOMA = "idIdioma";
    private static final String ERROR_PAGE = "error.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

                String action = request.getParameter("accion");
                String ruta = request.getParameter("ruta");
                String page = "index.jsp"; // Página predeterminada
            
                if (action != null) {
                    switch (action) {
                        case "listarIdiomas":
                            page = obtenerPaginaListarIdiomas(ruta);
                            break;
                        case "inicio":
                            page = obtenerPaginaInicio(ruta);
                            break;
                        case "agregarIdiomas":
                            page = "admin/idiomas/agregarIdiomas.jsp";
                            break;
                        case "editarIdiomas":
                            page = obtenerPaginaEditarIdiomas(request);
                            break;
                        case "exportarPdf":
                            exportarPdf(response);
                            return; // Retorna para evitar el envío de la respuesta dos veces
                        case "exportarCsv":
                            exportarCsv(response);
                            return; // Retorna para evitar el envío de la respuesta dos veces
                        case "eliminarIdioma":
                            page = eliminarIdioma(request);
                            break;
                        default:
                            page = ERROR_PAGE;
                            break;
                    }
                }

        RequestDispatcher dispatcher = request.getRequestDispatcher(page);
        dispatcher.forward(request, response);
    }

    private String obtenerPaginaListarIdiomas(String ruta) {
        switch (ruta) {
            case "app":
                return "app/learn/idiomas.jsp";
            case "admin":
                return LISTAR_IDIOMAS_PAGE;
            default:
                System.out.println("Estamos en listar idiomas");
                return "app/index.jsp";
        }
    }
    
    private String obtenerPaginaInicio(String ruta) {
        switch (ruta) {
            case "app":
                return "app/index.jsp";
            case "admin":
                return "admin/index.jsp";
            default:
                return ERROR_PAGE;
        }
    }
    
    private String obtenerPaginaEditarIdiomas(HttpServletRequest request) {
        String idIdiomaStr = request.getParameter(PARAM_ID_IDIOMA);
        if (idIdiomaStr != null) {
            try {
                int idIdioma = Integer.parseInt(idIdiomaStr);
                ClsModeloDaoIdioma dao = new ClsModeloDaoIdioma();
                ClsModeloIdioma idioma = dao.obtenerIdiomaPorId(idIdioma);
                if (idioma != null) {
                    request.setAttribute("idioma", idioma);
                    return "admin/idiomas/editarIdiomas.jsp";
                }
            } catch (NumberFormatException e) {
                // Manejar el caso en que el ID de idioma no es válido
            }
        }
        return ERROR_PAGE;
    }
    
    private void exportarPdf(HttpServletResponse response) throws IOException {
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
        } catch (DocumentException ex) {
            Logger.getLogger(IdiomaServlet.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (document != null && document.isOpen()) {
                document.close();
            }
        }
    }
    
    private void exportarCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=ListadoIdiomas.csv");
    
        try (CSVWriter writer = new CSVWriter(response.getWriter())) {
            writer.writeNext(new String[]{"ID", "Nombre", "Descripción", "URL Banner"});
    
            ClsModeloDaoIdioma daoIdioma = new ClsModeloDaoIdioma();
            List<ClsModeloIdioma> IDIO = daoIdioma.obtenerTodosIdiomas();
    
            for (ClsModeloIdioma idiomaExcel : IDIO) {
                String[] rowData = {
                        String.valueOf(idiomaExcel.getIdIdioma()),
                        idiomaExcel.getNombre(),
                        idiomaExcel.getDescripcion(),
                        idiomaExcel.getUrlBanner()
                };
                writer.writeNext(rowData);
            }
        } catch (IOException ex) {
            Logger.getLogger(IdiomaServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String eliminarIdioma(HttpServletRequest request) {
        int idIdiomaEliminar = Integer.parseInt(request.getParameter(PARAM_ID_IDIOMA));
        ClsModeloDaoIdioma daoIdiomaEliminar = new ClsModeloDaoIdioma();
        boolean exitoEliminacion = daoIdiomaEliminar.eliminarIdioma(idIdiomaEliminar);
        if (exitoEliminacion) {
            return LISTAR_IDIOMAS_PAGE;
        } else {
            return ERROR_PAGE;
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
                            page = agregarIdioma(request);
                            break;
                        case "actualizarIdioma":
                            page = actualizarIdioma(request);
                            break;
                        case "insertarIdiomaCSV":
                            page = insertarIdiomaCSV(request);
                            break;
                        default:
                            page = ERROR_PAGE;
                            break;
                    }
                }

        RequestDispatcher dispatcher = request.getRequestDispatcher(page);
        dispatcher.forward(request, response);

    }

    private String agregarIdioma(HttpServletRequest request) {
        ClsModeloIdioma nuevoIdioma = new ClsModeloIdioma();
        nuevoIdioma.setNombre(request.getParameter("nombre"));
        nuevoIdioma.setDescripcion(request.getParameter("descripcion"));
        nuevoIdioma.setUrlBanner(request.getParameter("urlBanner"));
    
        ClsModeloDaoIdioma daoIdioma = new ClsModeloDaoIdioma();
        boolean exito = daoIdioma.agregarIdioma(nuevoIdioma);
    
        return exito ? LISTAR_IDIOMAS_PAGE : ERROR_PAGE;
    }
    
    private String actualizarIdioma(HttpServletRequest request) {
        int idIdioma = Integer.parseInt(request.getParameter(PARAM_ID_IDIOMA));
        String nombre = request.getParameter("nombre");
        String descripcion = request.getParameter("descripcion");
        String urlBanner = request.getParameter("urlBanner");
    
        ClsModeloIdioma idiomaActualizado = new ClsModeloIdioma(idIdioma, nombre, descripcion, urlBanner);
    
        ClsModeloDaoIdioma daoIdiomaActualizar = new ClsModeloDaoIdioma();
        boolean exitoActualizacion = daoIdiomaActualizar.actualizarIdioma(idiomaActualizado);
    
        return exitoActualizacion ? LISTAR_IDIOMAS_PAGE : ERROR_PAGE;
    }
    
    @SuppressWarnings("finally")
    private String insertarIdiomaCSV(HttpServletRequest request) {
        if (ServletFileUpload.isMultipartContent(request)) {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
    
            try {
                List<FileItem> items = upload.parseRequest(request);
                for (FileItem item : items) {
                    if (!item.isFormField()) {
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
            } catch (FileUploadException | IOException | CsvValidationException ex) {
                Logger.getLogger(IdiomaServlet.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                return LISTAR_IDIOMAS_PAGE;
            }
        } else {
            System.out.println("No se encontró el archivo CSV en la solicitud");
            return LISTAR_IDIOMAS_PAGE;
        }
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
