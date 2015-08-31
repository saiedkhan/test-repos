package Controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.List;

import Logic.UserDAO;
import Models.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.google.gson.Gson;

@WebServlet(name = "ServletUsers", urlPatterns = {"/ServletUsers"})
@MultipartConfig (location = "C:\\Users\\Saied\\IdeaProjects\\scientific-associations\\web\\uploaded-files", fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 *5)
public class ServletUsers extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        boolean ajax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        UserDAO userDAO = new UserDAO();
        if (ajax) {
            // Handle ajax (JSON) response.
            User user = userDAO.getUser(request.getParameter("nationalId"));
            System.out.println("wooooooooooooha!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println(request.getParameter("nationalId"));

            String json = new Gson().toJson(user);

            response.setContentType("application/json");  // Set content type of the response so that jQuery knows what it can expect.
            response.setCharacterEncoding("UTF-8"); // You want world domination, huh?
            response.getWriter().write(json);       // Write response body.

        } else {
            // Handle regular (JSP) response.
            User user = makeUser(request);
            try {
                SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
                Session session = sessionFactory.openSession();
                session.beginTransaction();

                session.save(user);

                session.getTransaction().commit();
                session.close();

            } catch (Exception e){
                e.printStackTrace();
                request.getRequestDispatcher("/failed.jsp").forward(request, response);
            }

            List users = userDAO.getAllUsers();
            request.setAttribute("users", users);
            request.getRequestDispatcher("/users.jsp").forward(request, response);
        }

    }

    public User makeUser(HttpServletRequest request) throws IOException, ServletException{
        User user = new User();
        user.setfName(request.getParameter("fName"));
        user.setlName(request.getParameter("lName"));
        user.setUserName(request.getParameter("userName"));
        user.setPassword(request.getParameter("password"));
        user.setAssociationNumber(Integer.parseInt(request.getParameter("associationNumber")));
        user.setBirthYear(request.getParameter("birthYear"));
        user.setPositionTitle(request.getParameter("positionTitle"));
        user.setEmail(request.getParameter("email"));
        user.setStudentId(request.getParameter("studentId"));
        user.setNationalId(request.getParameter("nationalId"));
        user.setPhone(request.getParameter("phone"));

        String directory = "/uploaded-files/";
        Part photo = request.getPart("photo");
        if(photo != null){
            photo.write(photo.getSubmittedFileName());
            user.setPhoto(directory + photo.getSubmittedFileName());
        }

        return user;
    }
}

