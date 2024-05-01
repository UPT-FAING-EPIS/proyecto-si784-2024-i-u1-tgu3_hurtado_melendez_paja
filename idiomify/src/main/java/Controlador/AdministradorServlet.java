/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package Controlador;

import Modelo.ClsModeloAdministrador;
import ModeloDAO.ClsModeloDaoAdministrador;
import ModeloDAO.ClsModeloDaoCurso;
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
import java.io.PrintWriter;
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
import javax.servlet.jsp.PageContext;
//PARA ENCRIPTAR
import Controlador.LoginServlet;
import Modelo.ClsModeloAuditoria;
import ModeloDAO.ClsModeloDaoAuditoria;
import static java.lang.System.out;

/**
 *
 * @author LENOVO
 */
@WebServlet(name = "AdministradorServlet", urlPatterns = {"/AdministradorServlet"})
public class AdministradorServlet extends HttpServlet {
      LoginServlet loginServlet = new LoginServlet();
      ClsModeloDaoAuditoria daoAudi = new ClsModeloDaoAuditoria();
      ClsModeloAuditoria nuevoAudi= new ClsModeloAuditoria();
      

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    System.out.println("Estamos en el Get");
    String action = request.getParameter("accion");
    String page = "index.jsp";
    
    //SOLUCION ERROR
    final String ERROR_PAGE = "error.jsp";

    if (action != null) {



        switch (action) {
            case "listarAdmin":
                page = listarAdministradores(request, response);
                break;
                
            case "agregarAdmin":
                System.out.println("Estamos en agregarAdmin");
                page = "admin/administradores/agregarAdministradores.jsp";
                break;
                
            case "editarAdmin":
                page = "admin/administradores/editarAdministradores.jsp";
                break;
                
            case "exportarCsv":
                page = exportarCsv(request, response);
                break;
                
            case "exportarPdf":
                page = exportarPdf(request, response);
                break;
            
            
            default:
                page = ERROR_PAGE;
                break;
        }
    }

    RequestDispatcher dispatcher = request.getRequestDispatcher(page);
    dispatcher.forward(request, response);
}

private static final String LISTAR_ADMIN_PAGE = "admin/administradores/listarAdministradores.jsp";

private String listarAdministradores(HttpServletRequest request, HttpServletResponse response) {
    return LISTAR_ADMIN_PAGE;
}

private String exportarCsv(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/csv");
    response.setHeader("Content-Disposition", "attachment; filename=ListadoAdministradores.csv");

    try (CSVWriter writer = new CSVWriter(response.getWriter())) {
        // Escribir la fila de encabezado del archivo CSV
        writer.writeNext(new String[] {
                "DNI", "Nombre", "Apellido", "Fecha de Nacimiento", "Género",
                "País", "Ciudad", "Email", "Teléfono", "Rol", "Estado", "Foto de Perfil"
        });

        // Obtener los datos de los administradores
        ClsModeloDaoAdministrador daoAdmin = new ClsModeloDaoAdministrador();
        List<ClsModeloAdministrador> administradores = daoAdmin.obtenerTodosAdministradores();

        // Escribir los datos de los administradores al archivo CSV
        for (ClsModeloAdministrador admin : administradores) {
            String[] rowData = {
                    admin.getDni(), admin.getNombre(), admin.getApellido(),
                    admin.getFechaNacimiento(), admin.getGenero(), admin.getPais(),
                    admin.getCiudad(), admin.getEmail(), String.valueOf(admin.getTelefono()),
                    admin.getRol(), String.valueOf(admin.getEstado()), admin.getFotoPerfil()
            };
            writer.writeNext(rowData);
        }
    } catch (IOException ex) {
        Logger.getLogger(AdministradorServlet.class.getName()).log(Level.SEVERE, null, ex);
    }

    return null; // La página no cambia después de exportar CSV
}

private String exportarPdf(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // Lógica para exportar PDF
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
        String imageUrl = "https://i.ibb.co/JsQy6xm/idiomifylogo.png";
        Image logo = Image.getInstance(new URL(imageUrl));
        documentAdmins.add(logo);
        documentAdmins.add(new Paragraph("Idiomify tu aplicación para conocer nuevos idiomas"));
        documentAdmins.add(new Paragraph("             "));

        // Tabla con encabezados
        PdfPTable tableAdmins = new PdfPTable(13);
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

        // Rellenar la tabla con la información de los administradores
        for (ClsModeloAdministrador admin : administradores) {
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

        // Agregar la tabla al documento
        documentAdmins.add(tableAdmins);

        // Pie de página
        documentAdmins.add(new Paragraph("        "));
        documentAdmins.add(new Paragraph("Fecha de creación: " + new Date()));
        String nombreUsuario = "Idiomify con Amor :)";
        documentAdmins.add(new Paragraph("De: " + nombreUsuario));

    } catch (DocumentException | IOException e) {
        e.printStackTrace();
    } finally {
        if (documentAdmins != null && documentAdmins.isOpen()) {
            documentAdmins.close();
        }
        if (writerAdmins != null) {
            writerAdmins.close();
        }
    }

    return null; // La página no cambia después de exportar PDF
}


    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */





    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                    String action = request.getParameter("accion");
        String page = "index.jsp"; // Página predeterminada
        HttpSession session = request.getSession();
        
        //SOLUCION ERROR
        final String ERROR_PAGE = "error.jsp";

        // Recupera el atributo adminAutenticado desde la sesión
        ClsModeloAdministrador adminAutenticado = (ClsModeloAdministrador) session.getAttribute("adminAutenticado");
    if (action != null) {
        switch (action) {
            case "registrarAdministrador":
                page = registrarAdministrador(request, adminAutenticado, ERROR_PAGE);
                break;

            case "actualizarAdministrador":
                page = "admin/administradores/listarAdmnistradores.jsp"; // Redirigir a la página de listado
                break;
               
            case "autenticarAdministrador":
                page = autenticarAdministrador(request, session, ERROR_PAGE);
                break;

            case "editarAdministrador":
                page = editarAdministrador(request, adminAutenticado, ERROR_PAGE);
                break;

            case "insertarAdministradorCSV":
                page = insertarAdministradorCSV(request, adminAutenticado, ERROR_PAGE);
                break;

            default:
                // Lógica para manejar otros casos o errores
                page = ERROR_PAGE;
                break;
        }
    }


        RequestDispatcher dispatcher = request.getRequestDispatcher(page);
        dispatcher.forward(request, response);
    }

    //SOLUCION ERROR
    protected static final String PARAM_EMAIL = "email";
    protected static final String PARAM_PASSWORD = "password";
    protected static final String ADMIN_INDEX_PAGE = "admin/index.jsp";


    // Método para registrar un nuevo administrador
private String registrarAdministrador(HttpServletRequest request, ClsModeloAdministrador adminAutenticado, String errorPage) {
    String page;
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
    String password = request.getParameter(PARAM_PASSWORD); // Asegúrate de encriptar la contraseña antes de enviarla a la base de datos
    String rol = request.getParameter("rol");
    int estado = 1; // Puedes establecer el estado inicial según tus necesidades // Puedes manejar la imagen de perfil si es necesario
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

    // Llama al método registrarUsuario de ClsModeloDaoUsuario
    ClsModeloDaoAdministrador daoAdmin = new ClsModeloDaoAdministrador();
    boolean exitoRegistro = daoAdmin.registrarAdministrador(nuevoAdmin);

    if (exitoRegistro) {
        String dniAdmin = adminAutenticado.getDni();
        nuevoAudi.setFKidAdmin(Integer.parseInt(dniAdmin));
        nuevoAudi.setAccion("registrarAdministrador");
        daoAudi.agregarAuditoria(nuevoAudi);
        // El registro fue exitoso
        // Puedes redirigir a una página de éxito o a la página de inicio de sesión
        page = ADMIN_INDEX_PAGE;
        System.out.println("llego a  exito");
    } else {
        // El registro falló, puedes redirigir a una página de error o mostrar un mensaje
        page = errorPage;
        System.out.println("aqui error despues del esxito");
    }
    return page;
}

// Método para autenticar un administrador
private String autenticarAdministrador(HttpServletRequest request, HttpSession session, String errorPage) {
    String page;
    String correo = request.getParameter(PARAM_EMAIL);
    String clave = request.getParameter(PARAM_PASSWORD);

    String captchaText = request.getParameter("captcha");

    System.out.println("Email: " + correo);
    System.out.println("Password: " + clave);

    String captchaTextFromSession = (String) session.getAttribute("captchaText");
    System.out.println("Captcha from Session: " + captchaTextFromSession);

    if (captchaText == null || captchaTextFromSession == null || !captchaText.equalsIgnoreCase(captchaTextFromSession)) {
        // CAPTCHA incorrecto
        // Aquí puedes manejar cómo deseas responder a un captcha incorrecto
        // Puedes redirigir a una página de error, imprimir un mensaje, etc.
        page = "landing/loginadmin_2.jsp?mensaje=captcha";
        return page;
    }

    // Llama al método autenticarAdministrador de ClsModeloDaoAdministrador
    ClsModeloDaoAdministrador daoAdminAu = new ClsModeloDaoAdministrador();

    ClsModeloAdministrador adminAutenticado = daoAdminAu.autenticarAdministrador(correo, clave);

    if (adminAutenticado != null) {

        // La autenticación fue exitosa
        // Puedes almacenar la información del administrador en la sesión si es necesario
        session.setAttribute("adminAutenticado", adminAutenticado);
        page = ADMIN_INDEX_PAGE; // Redirige a la página del perfil del administrador
        System.out.println("Autenticación exitosa");
    } else {
        // La autenticación falló, puedes redirigir a una página de error o mostrar un mensaje
        System.out.println("Autenticación fallida");
        page = "landing/loginadmin_1.jsp?mensaje=correopassword";
    }
    return page;
}

// Método para editar un administrador
private String editarAdministrador(HttpServletRequest request, ClsModeloAdministrador adminAutenticado, String errorPage) {
    String page;
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
    String password2 = request.getParameter(PARAM_PASSWORD); // Asegúrate de encriptar la contraseña antes de enviarla a la base de datos
    String rol2 = request.getParameter("rol");
    int estado2 = 1; // Puedes establecer el estado inicial según tus necesidades // Puedes manejar la imagen de perfil si es necesario
    String fotoPerfil2 = "fotos/fotodefault.png";
    System.out.println("dni del post editar:" + dni2);
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
        nuevoAudi.setAccion("editarAdministrador");
        daoAudi.agregarAuditoria(nuevoAudi);
        // El registro fue exitoso
        // Puedes redirigir a una página de éxito o a la página de inicio de sesión
        page = ADMIN_INDEX_PAGE;
        System.out.println("llego a  exito");
    } else {
        // El registro falló, puedes redirigir a una página de error o mostrar un mensaje
        page = errorPage;
        System.out.println("aqui error despues del esxito");
    }
    return page;
}

// Método para insertar administradores desde un archivo CSV
private String insertarAdministradorCSV(HttpServletRequest request, ClsModeloAdministrador adminAutenticado, String errorPage) {
    String page;
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
            nuevoAudi.setAccion("insertarAdministradorCSV");
            daoAudi.agregarAuditoria(nuevoAudi);
            page = "admin/administradores/listarAdmnistradores.jsp";
        }
    } else {
        page = errorPage;
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
