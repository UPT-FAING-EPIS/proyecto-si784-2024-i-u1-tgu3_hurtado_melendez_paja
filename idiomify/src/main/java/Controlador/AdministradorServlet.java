/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controlador;

import Modelo.ClsModeloAdministrador;
import ModeloDAO.ClsModeloDaoAdministrador;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import Modelo.ClsModeloAuditoria;
import ModeloDAO.ClsModeloDaoAuditoria;


/**
 *
 * @author LENOVO
 */
@WebServlet(name = "AdministradorServlet", urlPatterns = { "/AdministradorServlet" })
public class AdministradorServlet extends HttpServlet {
    private static final LoginServlet loginServlet = new LoginServlet();
    private static final ClsModeloDaoAuditoria daoAudi = new ClsModeloDaoAuditoria();
    private static final ClsModeloAuditoria nuevoAudi = new ClsModeloAuditoria();
    private static final String ERROR_PAGE = "error.jsp";
    private static final String LISTAR_ADMIN_PAGE = "admin/administradores/listarAdministradores.jsp";
    private static final String ADMIN_INDEX_PAGE = "admin/index.jsp";
    private static final Logger logger = Logger.getLogger(AdministradorServlet.class.getName());
    private static final String ADMIN_LIST_PAGE = "admin/administradores/listarAdmnistradores.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("accion");
        String page = "index.jsp";

        if (action != null) {
            switch (action) {
                case "listarAdmin":
                    page = mostrarListaAdministradores();
                    break;
                case "agregarAdmin":
                    page = mostrarFormularioAgregarAdministrador();
                    break;
                case "editarAdmin":
                    page = mostrarFormularioEditarAdministrador();
                    break;
                case "exportarCsv":
                    exportarCSV(response);
                    break;
                case "exportarPdf":
                    exportarPDF(response);
                    break;
                default:
                    page = ERROR_PAGE;
                    break;
            }
        }
        try {
            RequestDispatcher dispatcher = request.getRequestDispatcher(page);
            dispatcher.forward(request, response);
        } catch (ServletException | IOException e) {
            e.printStackTrace();
        }
    }

    private String mostrarListaAdministradores() {
        return LISTAR_ADMIN_PAGE;
    }

    private String mostrarFormularioAgregarAdministrador() {
        return LISTAR_ADMIN_PAGE;
    }

    private String mostrarFormularioEditarAdministrador() {
        return LISTAR_ADMIN_PAGE;
    }

    private void exportarCSV(HttpServletResponse response) {
        // Configurar la respuesta para la descarga de un archivo CSV
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=ListadoAdministradores.csv");

        // Crear un objeto CSVWriter para escribir el archivo CSV
        try (CSVWriter writer = new CSVWriter(response.getWriter())) {
            // Escribir la fila de encabezado del archivo CSV
            writer.writeNext(new String[] {
                    "DNI",
                    "Nombre",
                    "Apellido",
                    "Fecha de Nacimiento",
                    "Género",
                    "País",
                    "Ciudad",
                    "Email",
                    "Teléfono",
                    "Rol",
                    "Estado",
                    "Foto de Perfil"
            });

            // Obtener los datos de los administradores y escribirlos al archivo CSV
            escribirDatosAdministradoresCSV(writer);
        } catch (IOException ex) {
            // Manejar la excepción IOException
            Logger.getLogger(AdministradorServlet.class.getName()).log(Level.SEVERE, "Error al exportar el archivo CSV",
                    ex);
        }
    }

    private void escribirDatosAdministradoresCSV(CSVWriter writer) {
        ClsModeloDaoAdministrador daoAdmin = new ClsModeloDaoAdministrador();
        List<ClsModeloAdministrador> administradores = daoAdmin.obtenerTodosAdministradores();

        // Escribir los datos de los administradores al archivo CSV
        for (ClsModeloAdministrador admin : administradores) {
            String[] rowData = {
                    admin.getDni(),
                    admin.getNombre(),
                    admin.getApellido(),
                    admin.getFechaNacimiento(),
                    admin.getGenero(),
                    admin.getPais(),
                    admin.getCiudad(),
                    admin.getEmail(),
                    String.valueOf(admin.getTelefono()),
                    admin.getRol(),
                    String.valueOf(admin.getEstado()),
                    admin.getFotoPerfil()
            };
            writer.writeNext(rowData);
        }
    }

    private void exportarPDF(HttpServletResponse response) {
        ClsModeloDaoAdministrador daoAdmin = new ClsModeloDaoAdministrador();
        List<ClsModeloAdministrador> administradores = daoAdmin.obtenerTodosAdministradores();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=ListadoAdministradores.pdf");

        Document documentAdmins = new Document();
        PdfWriter writerAdmins = null;

        try {
            writerAdmins = PdfWriter.getInstance(documentAdmins, response.getOutputStream());
            documentAdmins.open();

            // Agregar el logo y encabezado al documento desde una URL
            agregarEncabezadoPDF(documentAdmins);

            // Agregar la tabla de administradores al documento PDF
            agregarTablaAdministradoresPDF(documentAdmins, administradores);

            // Agregar pie de página al documento PDF
            agregarPiePaginaPDF(documentAdmins);

        } catch (DocumentException | IOException e) {
            // Manejar la excepción IOException
            e.printStackTrace(); // Opcional: imprime el rastreo de la pila para el diagnóstico
            Logger.getLogger(AdministradorServlet.class.getName()).log(Level.SEVERE, "Error al exportar el archivo PDF",
                    e);
        } finally {
            cerrarDocumentoPDF(documentAdmins, writerAdmins);
        }
    }

    private void agregarEncabezadoPDF(Document documentAdmins) throws IOException, DocumentException {
        String imageUrl = "https://i.ibb.co/JsQy6xm/idiomifylogo.png";
        Image logo = Image.getInstance(new URL(imageUrl));
        documentAdmins.add(logo);
        documentAdmins.add(new Paragraph("Idiomify tu aplicación para conocer nuevos idiomas"));
        documentAdmins.add(new Paragraph("             "));
    }

    private void agregarTablaAdministradoresPDF(Document documentAdmins, List<ClsModeloAdministrador> administradores)
            throws DocumentException {
        PdfPTable tableAdmins = new PdfPTable(13);
        // Agregar encabezados a la tabla
        agregarEncabezadosTablaPDF(tableAdmins);
        // Rellenar la tabla con la información de los administradores
        for (ClsModeloAdministrador admin : administradores) {
            agregarFilaTablaPDF(tableAdmins, admin);
        }
        // Agregar la tabla al documento
        documentAdmins.add(tableAdmins);
    }

    private void agregarEncabezadosTablaPDF(PdfPTable tableAdmins) {
        tableAdmins.addCell("DNI");
        tableAdmins.addCell("Nombre");
        tableAdmins.addCell("Apellido");
        tableAdmins.addCell("Fecha de Nacimiento");
        tableAdmins.addCell("Género");
        tableAdmins.addCell("País");
        tableAdmins.addCell("Ciudad");
        tableAdmins.addCell("Email");
        tableAdmins.addCell("Teléfono");
        tableAdmins.addCell("Rol");
        tableAdmins.addCell("Fecha de Registro");
        tableAdmins.addCell("Fecha de Actualización");
        tableAdmins.addCell("Estado");
        tableAdmins.addCell("URL Foto de Perfil");
    }

    private void agregarFilaTablaPDF(PdfPTable tableAdmins, ClsModeloAdministrador admin) {
        tableAdmins.addCell(admin.getDni());
        tableAdmins.addCell(admin.getNombre());
        tableAdmins.addCell(admin.getApellido());
        tableAdmins.addCell(admin.getFechaNacimiento());
        tableAdmins.addCell(admin.getGenero());
        tableAdmins.addCell(admin.getPais());
        tableAdmins.addCell(admin.getCiudad());
        tableAdmins.addCell(admin.getEmail());
        tableAdmins.addCell(String.valueOf(admin.getTelefono()));
        tableAdmins.addCell(admin.getRol());
        tableAdmins.addCell(String.valueOf(admin.getFechaRegistro()));
        tableAdmins.addCell(String.valueOf(admin.getFechaActualizacion()));
        tableAdmins.addCell(String.valueOf(admin.getEstado()));
        tableAdmins.addCell(admin.getFotoPerfil());
    }

    private void agregarPiePaginaPDF(Document documentAdmins) throws DocumentException {
        documentAdmins.add(new Paragraph("        "));
        documentAdmins.add(new Paragraph("Fecha de creación: " + new Date()));
        String nombreUsuario = "Idiomify con Amor :)";
        documentAdmins.add(new Paragraph("De: " + nombreUsuario));
    }

    private void cerrarDocumentoPDF(Document documentAdmins, PdfWriter writerAdmins) {
        if (documentAdmins != null && documentAdmins.isOpen()) {
            documentAdmins.close();
        }
        if (writerAdmins != null) {
            writerAdmins.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("accion");
        String page = "index.jsp"; // Default page
        HttpSession session = request.getSession();

        ClsModeloAdministrador adminAutenticado = (ClsModeloAdministrador) session.getAttribute("adminAutenticado");
        if (action != null) {
            switch (action) {
                case "registrarAdministrador":
                    page = registrarAdministrador(request, adminAutenticado);
                    break;
                case "actualizarAdministrador":
                    page = actualizarAdministrador();
                    break;

                case "autenticarAdministrador":
                    page = autenticarAdministrador(request, session);
                    break;
                case "editarAdministrador":
                    page = mostrarFormularioEditarAdministrador(request);
                    break;
                case "insertarAdministradorCSV":
                    page = insertarAdministradorCSV(request, adminAutenticado);
                    break;
                default:
                    page = ERROR_PAGE;
                    break;
            }
        }

        try {
            RequestDispatcher dispatcher = request.getRequestDispatcher(page);
            dispatcher.forward(request, response);
        } catch (ServletException | IOException e) {
            e.printStackTrace();
        }
    }

    private static final String PARAM_EMAIL = "email";
    private static final String PARAM_PASSWORD = "password";

    private String registrarAdministrador(HttpServletRequest request, ClsModeloAdministrador adminAutenticado) {
        // Obtén los datos del formulario de registro
        String dni = request.getParameter("dni");
        String nombre = request.getParameter("nombre");
        String apellido = request.getParameter("apellido");
        String fechaNacimiento = request.getParameter("fechaNacimiento");
        String genero = request.getParameter("genero");
        String pais = request.getParameter("pais");
        String ciudad = request.getParameter("ciudad");
        String email = request.getParameter(PARAM_EMAIL);
        int telefono = Integer.parseInt(request.getParameter("telefono"));
        String password = request.getParameter(PARAM_PASSWORD); // Asegúrate de encriptar la contraseña antes de
        // enviarla a la base de datos
        String rol = request.getParameter("rol");
        int estado = 1; // Puedes establecer el estado inicial según tus necesidades // Puedes manejar
                        // la imagen de perfil si es necesario
        String fotoPerfil = "fotos/fotodefault.png";
        // Crea un objeto ClsModeloUsuario con los datos del formulario
        ClsModeloAdministrador nuevoAdmin = new ClsModeloAdministrador();
        nuevoAdmin.setDni(dni);
        nuevoAdmin.setNombre(nombre);
        nuevoAdmin.setApellido(apellido);
        nuevoAdmin.setFechaNacimiento(fechaNacimiento);
        nuevoAdmin.setGenero(genero);
        nuevoAdmin.setPais(pais);
        nuevoAdmin.setCiudad(ciudad);
        nuevoAdmin.setEmail(email);
        nuevoAdmin.setTelefono(telefono);
        nuevoAdmin.setPasswordHash(password);
        nuevoAdmin.setRol(rol);
        nuevoAdmin.setEstado(estado);
        nuevoAdmin.setFotoPerfil(fotoPerfil);

        // Check if logger is enabled for INFO level
        if (logger.isLoggable(Level.INFO)) {
            // Log input data using parameterized logging
            logger.info(String.format(
                    "Nombre: %s, Apellido: %s, Fecha de Nacimiento: %s, Género: %s, País: %s, Ciudad: %s, Email: %s, Password: %s, Estado: %d",
                    nombre, apellido, fechaNacimiento, genero, pais, ciudad, email, password, estado));
        }

        // Llama al método registrarUsuario de ClsModeloDaoUsuario
        ClsModeloDaoAdministrador daoAdmin = new ClsModeloDaoAdministrador();
        boolean exitoRegistro = daoAdmin.registrarAdministrador(nuevoAdmin);

        String page;
        if (exitoRegistro) {
            String dniAdmin = adminAutenticado.getDni();
            nuevoAudi.setFKidAdmin(Integer.parseInt(dniAdmin));
            nuevoAudi.setAccion("registrarAdministrador"); // Set appropriate action value
            daoAudi.agregarAuditoria(nuevoAudi);
            page = ADMIN_INDEX_PAGE;
            logger.info("Registro exitoso");
        } else {
            page = ERROR_PAGE;
            logger.severe("Error en el registro");
        }
        return page;
    }

    private String actualizarAdministrador() {
        return ADMIN_LIST_PAGE;
    }

    private String autenticarAdministrador(HttpServletRequest request, HttpSession session) {
        String correo = request.getParameter(PARAM_EMAIL);
        String clave = request.getParameter(PARAM_PASSWORD);

        String captchaText = request.getParameter("captcha");

        // Formatea los valores de correo y clave en un solo mensaje
        String loginMessage = "Email: " + correo + ", Password: " + clave;
        logger.info(loginMessage);

        String captchaTextFromSession = (String) session.getAttribute("captchaText");
        String message = String.format("Captcha from Session: %s", captchaTextFromSession);
        logger.info(message);

        String page; // Declara la variable page
        ClsModeloAdministrador adminAutenticado; // Declara la variable adminAutenticado

        if (captchaText == null || captchaTextFromSession == null
                || !captchaText.equalsIgnoreCase(captchaTextFromSession)) {
            // CAPTCHA incorrecto
            // Aquí puedes manejar cómo deseas responder a un captcha incorrecto
            // Puedes redirigir a una página de error, imprimir un mensaje, etc.
            return "landing/loginadmin_2.jsp?mensaje=captcha";
        }

        // Llama al método autenticarAdministrador de ClsModeloDaoAdministrador
        ClsModeloDaoAdministrador daoAdminAu = new ClsModeloDaoAdministrador();

        adminAutenticado = daoAdminAu.autenticarAdministrador(correo, clave);

        if (adminAutenticado != null) {
            // La autenticación fue exitosa
            // Puedes almacenar la información del administrador en la sesión si es
            // necesario
            request.getSession().setAttribute("adminAutenticado", adminAutenticado);
            page = ADMIN_INDEX_PAGE; // Redirige a la página del perfil del administrador
            logger.info("Autenticación exitosa");
        } else {
            // La autenticación falló, puedes redirigir a una página de error o mostrar un
            // mensaje
            logger.info("Autenticación fallida");
            page = "landing/loginadmin_1.jsp?mensaje=correopassword";
        }

        return page; // Retorna la variable page
    }

    

    private String mostrarFormularioEditarAdministrador(HttpServletRequest request) {
        // Declare adminAutenticado, action, and page variables
        ClsModeloAdministrador adminAutenticado = null;
        String action = ""; // Assuming action is a String variable
        String page = ""; // Declare page variable

        // Obtén los datos del formulario de registro
        String dni2 = request.getParameter("dni");
        String nombre2 = request.getParameter("nombre");
        String apellido2 = request.getParameter("apellido");
        String fechaNacimiento2 = request.getParameter("fechaNacimiento");
        String genero2 = request.getParameter("genero");
        String pais2 = request.getParameter("pais");
        String ciudad2 = request.getParameter("ciudad");
        String email2 = request.getParameter(PARAM_EMAIL);
        int telefono2 = Integer.parseInt(request.getParameter("telefono"));
        String password2 = request.getParameter(PARAM_PASSWORD); // Asegúrate de encriptar la contraseña antes
        // de enviarla a la base de datos
        String rol2 = request.getParameter("rol");
        int estado2 = 1; // Puedes establecer el estado inicial según tus necesidades // Puedes manejar
                         // la imagen de perfil si es necesario
        String fotoPerfil2 = "fotos/fotodefault.png";
        // Crea un objeto ClsModeloUsuario con los datos del formulario
        ClsModeloAdministrador nuevoAdmin2 = new ClsModeloAdministrador();
        nuevoAdmin2.setDni(dni2);
        nuevoAdmin2.setNombre(nombre2);
        nuevoAdmin2.setApellido(apellido2);
        nuevoAdmin2.setFechaNacimiento(fechaNacimiento2);
        nuevoAdmin2.setGenero(genero2);
        nuevoAdmin2.setPais(pais2);
        nuevoAdmin2.setCiudad(ciudad2);
        nuevoAdmin2.setEmail(email2);
        nuevoAdmin2.setTelefono(telefono2);
        nuevoAdmin2.setPasswordHash(password2);
        nuevoAdmin2.setRol(rol2);
        nuevoAdmin2.setEstado(estado2);
        nuevoAdmin2.setFotoPerfil(fotoPerfil2);

        // Llama al método registrarUsuario de ClsModeloDaoUsuario
        ClsModeloDaoAdministrador daoAdmin2 = new ClsModeloDaoAdministrador();
        boolean exitoRegistro2 = daoAdmin2.actualizarAdministrador(nuevoAdmin2);
        String dniAdmin2 = adminAutenticado.getDni();
        if (exitoRegistro2) {

            nuevoAudi.setFKidAdmin(Integer.parseInt(dniAdmin2));
            nuevoAudi.setAccion(action);
            daoAudi.agregarAuditoria(nuevoAudi);
            // El registro fue exitoso
            // Puedes redirigir a una página de éxito o a la página de inicio de sesión
            page = ADMIN_INDEX_PAGE;
            System.out.println("llego a  exito");
        } else {
            // El registro falló, puedes redirigir a una página de error o mostrar un
            // mensaje
            page = ERROR_PAGE;
            System.out.println("aqui error despues del esxito");
        }

        return page;
    }

    private String insertarAdministradorCSV(HttpServletRequest request, ClsModeloAdministrador adminAutenticado) {
        String page = "";
        String action = "";
        // Verifica si la solicitud contiene datos multipartes (archivo adjunto)
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
                            csvReader.skip(1); // Saltar la fila de encabezado

                            String[] linea;
                            while ((linea = csvReader.readNext()) != null) {
                                ClsModeloAdministrador nuevoAdminCSV = new ClsModeloAdministrador();
                                nuevoAdminCSV.setDni(linea[0]);
                                nuevoAdminCSV.setNombre(linea[1]);
                                nuevoAdminCSV.setApellido(linea[2]);
                                nuevoAdminCSV.setFechaNacimiento(linea[3]);
                                nuevoAdminCSV.setGenero(linea[4]);
                                nuevoAdminCSV.setPais(linea[5]);
                                nuevoAdminCSV.setCiudad(linea[6]);
                                nuevoAdminCSV.setEmail(linea[7]);

                                // Encripta el password antes de almacenarlo en la base de datos
                                String passwordsk = linea[8];
                                String hashedPassword = loginServlet.hashPassword(passwordsk);
                                nuevoAdminCSV.setPasswordHash(hashedPassword);

                                nuevoAdminCSV.setTelefono(Integer.parseInt(linea[9]));
                                nuevoAdminCSV.setRol(linea[10]);
                                nuevoAdminCSV.setEstado(Integer.parseInt(linea[11]));
                                nuevoAdminCSV.setFotoPerfil(linea[12]);

                                ClsModeloDaoAdministrador daoAdminCSV = new ClsModeloDaoAdministrador();
                                daoAdminCSV.registrarAdministrador(nuevoAdminCSV);
                            }
                        } catch (CsvValidationException ex) {
                            Logger.getLogger(UsuarioServlet.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            } catch (FileUploadException ex) {
            } catch (IOException ex) {
                Logger.getLogger(UsuarioServlet.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                // Redirige a la página de listado de administradores después de la importación
                String dniAdmin = adminAutenticado.getDni();
                nuevoAudi.setFKidAdmin(Integer.parseInt(dniAdmin));
                nuevoAudi.setAccion(action);
                daoAudi.agregarAuditoria(nuevoAudi);
                page = LISTAR_ADMIN_PAGE;
            }
        } else {
            page = LISTAR_ADMIN_PAGE;
            System.out.println("No se encontró el archivo CSV en la solicitud");
            // Manejar el caso en que no se encuentra el archivo CSV
        }

        return page;
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
