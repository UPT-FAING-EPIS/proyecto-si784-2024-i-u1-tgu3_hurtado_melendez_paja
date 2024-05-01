package Controlador;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import Modelo.ClsModeloCurso;
import ModeloDAO.ClsModeloDaoCurso;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfWriter;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;


//PARA EL PDF
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.Image;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;



import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.apache.commons.io.IOUtils.writer;

@WebServlet(name = "CursoServlet", urlPatterns = {"/CursoServlet"})
public class CursoServlet extends HttpServlet {

    //SOLUCION ERROR
    static final String PARAM_ID_IDIOMA = "idIdioma";
    static final String HEADER_IDIOMA = "FKidIdioma";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                String action = request.getParameter("accion");
                String ruta = request.getParameter("ruta");
                String page = "index.jsp";
        
                //SOLUCION ERROR
                final String PARAM_ID_CURSO = "idCurso";
                final String LISTAR_CURSOS_PAGE = "admin/cursos/listarCursos.jsp?idIdioma=";
                final String ERROR_PAGE = "error.jsp";


                if (action == null) {
                    RequestDispatcher dispatcher = request.getRequestDispatcher(page);
                    dispatcher.forward(request, response);
                    return;
                }
        
                switch (action) {
                    case "listarCursos":
                        if ("app".equals(ruta)) {
                            page = "app/learn/cursos.jsp";
                        } else if ("landing".equals(ruta)) {
                            page = "landing/cursos.jsp";
                        } else if ("admin".equals(ruta)) {
                            page = "admin/cursos/listarCursos.jsp";
                        } else {
                            page = "app/index.jsp";
                            System.out.println("Estamos en listar idiomas");
                        }
                        break;
                    case "agregarCursos":
                        page = "admin/cursos/agregarCursos.jsp";
                        break;
                    case "editarCursos":
                        page = "admin/cursos/editarCursos.jsp";
                        break;
                    case "eliminarCurso":
                        String idCursoEliminar = request.getParameter(PARAM_ID_CURSO);
                        ClsModeloDaoCurso daoCursoEliminar = new ClsModeloDaoCurso();
                        boolean exitoEliminacion = daoCursoEliminar.eliminarCurso(idCursoEliminar);
        
                        if (exitoEliminacion) {
                            String idIdiomaStr = request.getParameter(PARAM_ID_IDIOMA);
                            System.out.println("var de delete:" + idIdiomaStr);
                            page = LISTAR_CURSOS_PAGE + idIdiomaStr;
                        } else {
                            page = ERROR_PAGE;
                        }
                        break;
                    case "exportarPdf":
                        page = exportarPdf(request, response);
                        break;
                    case "exportarCsv":
                        page = exportarCsv(request, response);
                        break;
                    default:
                        page = ERROR_PAGE;
                        break;
                }

        RequestDispatcher dispatcher = request.getRequestDispatcher(page);
        dispatcher.forward(request, response);
    }

    private String exportarPdf(HttpServletRequest request, HttpServletResponse response) {
        String idIdiomaStr = request.getParameter(PARAM_ID_IDIOMA);
        int idIdioma = Integer.parseInt(idIdiomaStr);
    
        ClsModeloDaoCurso dao = new ClsModeloDaoCurso();
        List<ClsModeloCurso> cursos = dao.listarCursosPorIdIdioma(idIdioma);
    
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=ListadoCursos.pdf");
    
        Document document = new Document();
        PdfWriter writer = null;
        try {
            writer = PdfWriter.getInstance(document, response.getOutputStream());
            document.open();
            String imageUrl = "https://i.ibb.co/JsQy6xm/idiomifylogo.png";
            Image logo = Image.getInstance(new URL(imageUrl));
            document.add(logo);
            document.add(new Paragraph("Idiomify tu aplicacion para conocer nuevos idiomas"));
            document.add(new Paragraph("             "));
    
            PdfPTable table = new PdfPTable(5);
            table.addCell("ID Curso");
            table.addCell("ID Idioma");
            table.addCell("Nombre");
            table.addCell("Descripción");
            table.addCell("URL Banner");
    
            for (ClsModeloCurso curso : cursos) {
                table.addCell(curso.getIdCurso());
                table.addCell(String.valueOf(curso.getFKidIdioma()));
                table.addCell(curso.getNombre());
                table.addCell(curso.getDescripcion());
                table.addCell(curso.getUrlBanner());
            }
    
            document.add(table);
            document.add(new Paragraph("        "));
            document.add(new Paragraph("Fecha de creación: " + new Date()));
            String nombreUsuario = "Idiomify con Amor :)";
            document.add(new Paragraph("De: " + nombreUsuario));
    
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            if (document != null && document.isOpen()) {
                document.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
    
        return "admin/index.jsp";
    }
    
    private String exportarCsv(HttpServletRequest request, HttpServletResponse response) {
        String idIdiomatxt = request.getParameter(PARAM_ID_IDIOMA);
        int idIdiomaint = Integer.parseInt(idIdiomatxt);
    
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=ListadoCursos.csv");
    
        try (CSVWriter writers = new CSVWriter(response.getWriter())) {
            writers.writeNext(new String[]{"ID", HEADER_IDIOMA, "Nombre", "Descripcion", "URL Banner"});
    
            ClsModeloDaoCurso daoCurso = new ClsModeloDaoCurso();
            List<ClsModeloCurso> cursocsv = daoCurso.listarCursosPorIdIdioma(idIdiomaint);
    
            for (ClsModeloCurso cursoExcel : cursocsv) {
                String[] rowData = {
                        String.valueOf(cursoExcel.getIdCurso()),
                        String.valueOf(cursoExcel.getFKidIdioma()),
                        cursoExcel.getNombre(),
                        cursoExcel.getDescripcion(),
                        cursoExcel.getUrlBanner()
                };
                writers.writeNext(rowData);
            }
        } catch (IOException ex) {
            Logger.getLogger(IdiomaServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    
        return "admin/index.jsp";
    }
    


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("accion");
        String page = "index.jsp";
  
        //SOLUCION ERROR
        final String PARAM_ID_CURSO = "idCurso";
        final String LISTAR_CURSOS_PAGE = "admin/cursos/listarCursos.jsp?idIdioma=";
        final String ERROR_PAGE = "error.jsp";


        if (action != null) {
            switch (action) {
                case "agregarCursos":
                    page = agregarCurso(request, PARAM_ID_CURSO, HEADER_IDIOMA, LISTAR_CURSOS_PAGE, ERROR_PAGE);
                    break;
                case "actualizarCurso":
                    page = actualizarCurso(request, PARAM_ID_CURSO, HEADER_IDIOMA, LISTAR_CURSOS_PAGE, ERROR_PAGE);
                    break;
                case "insertarCursosCSV":
                    page = insertarCursosCSV(request, ERROR_PAGE, LISTAR_CURSOS_PAGE, ERROR_PAGE);
                    break;
                default:
                    page = ERROR_PAGE;
                    break;
            }
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher(page);
        dispatcher.forward(request, response);
    }

    private String agregarCurso(HttpServletRequest request, String PARAM_ID_CURSO, String HEADER_IDIOMA,
                            String LISTAR_CURSOS_PAGE, String ERROR_PAGE) {
    ClsModeloCurso nuevoCurso = new ClsModeloCurso();
    nuevoCurso.setIdCurso(request.getParameter(PARAM_ID_CURSO));
    nuevoCurso.setFKidIdioma(Integer.parseInt(request.getParameter(HEADER_IDIOMA)));
    nuevoCurso.setUrlBanner(request.getParameter("urlBanner"));
    nuevoCurso.setNombre(request.getParameter("nombre"));
    nuevoCurso.setDescripcion(request.getParameter("descripcion"));

    ClsModeloDaoCurso daoCurso = new ClsModeloDaoCurso();
    boolean exito = daoCurso.agregarCurso(nuevoCurso);

    if (exito) {
        String idIdiomaStr = request.getParameter(HEADER_IDIOMA);
        return LISTAR_CURSOS_PAGE + idIdiomaStr;
    } else {
        return ERROR_PAGE;
    }
}

private String actualizarCurso(HttpServletRequest request, String PARAM_ID_CURSO, String HEADER_IDIOMA,
                                String LISTAR_CURSOS_PAGE, String ERROR_PAGE) {
    String idCurso = request.getParameter(PARAM_ID_CURSO);
    int FKidIdioma = Integer.parseInt(request.getParameter(HEADER_IDIOMA));
    String urlBanner = request.getParameter("urlBanner");
    String nombre = request.getParameter("nombre");
    String descripcion = request.getParameter("descripcion");

    ClsModeloCurso cursoActualizado = new ClsModeloCurso(idCurso, FKidIdioma, urlBanner, nombre, descripcion);

    ClsModeloDaoCurso daoCursoActualizar = new ClsModeloDaoCurso();
    boolean exitoActualizacion = daoCursoActualizar.actualizarCurso(cursoActualizado);

    if (exitoActualizacion) {
        String idIdiomaStr = request.getParameter(HEADER_IDIOMA);
        return LISTAR_CURSOS_PAGE + idIdiomaStr;
    } else {
        return ERROR_PAGE;
    }
}

private String insertarCursosCSV(HttpServletRequest request, String ERROR_PAGE, String LISTAR_CURSOS_PAGE, String HEADER_IDIOMA) {
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
                            ClsModeloCurso nuevoCursoCsv = new ClsModeloCurso();
                            nuevoCursoCsv.setIdCurso(linea[0]);
                            nuevoCursoCsv.setFKidIdioma(Integer.parseInt(request.getParameter(HEADER_IDIOMA)));
                            nuevoCursoCsv.setUrlBanner(linea[2]);
                            nuevoCursoCsv.setNombre(linea[3]);
                            nuevoCursoCsv.setDescripcion(linea[4]);

                            ClsModeloDaoCurso daoCursoCsv = new ClsModeloDaoCurso();
                            boolean cursoAgregado = daoCursoCsv.agregarCurso(nuevoCursoCsv);

                            if (cursoAgregado) {
                                System.out.println("Curso agregado exitosamente: " + nuevoCursoCsv.getIdCurso());
                            } else {
                                System.out.println("Error al agregar el curso: " + nuevoCursoCsv.getIdCurso());
                            }
                        }
                    }
                }
            }
        } catch (FileUploadException ex) {
            ex.printStackTrace();  
        } catch (CsvValidationException | IOException ex) {
            ex.printStackTrace();  
            Logger.getLogger(CursoServlet.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            String idIdiomaStr = request.getParameter(HEADER_IDIOMA);
            return LISTAR_CURSOS_PAGE + idIdiomaStr;
        }
    } else {
        return LISTAR_CURSOS_PAGE; 
    }
}

    @Override
    public String getServletInfo() {
        return "Descripción corta";
    }
}
