package Seguridad_en_APP_Web.demo.service;


import Seguridad_en_APP_Web.demo.model.CustomUser;
import Seguridad_en_APP_Web.demo.model.Rol;
import Seguridad_en_APP_Web.demo.repository.CustomUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class CustomUserService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CustomUserRepository customUserRepository;

    public CustomUser authenticate(String username, String password, HttpServletResponse response, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        final String[] isAdmin = {null};
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("isAdmin".equals(cookie.getName())) {
                    isAdmin[0] = cookie.getValue();
                }
            }
        }
        String sql = "SELECT * FROM custom_user WHERE username = '" + username + "' AND password = '" + password + "'";
        System.out.println("Generated SQL: " + sql);
        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                CustomUser user = new CustomUser();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRol(Rol.valueOf(rs.getString("rol")));

                // Almacenar el nombre de usuario en la sesión
                request.getSession().setAttribute("username", username);
                if(Objects.equals(isAdmin[0], "true")){
                    if(!Objects.equals(user.getRol().toString(),"ADMIN" )){
                       this.updateUserRole(user.getUsername(), Rol.ADMIN);
                    }
                }
                if (user.getRol() == Rol.ADMIN) {
                    isAdmin[0] = "true";
                }
                else {
                    isAdmin[0] = "false";
                }
                Cookie isAdminCookie = new Cookie("isAdmin",isAdmin[0]);
                isAdminCookie.setPath("/");
                isAdminCookie.setMaxAge(3600);
                response.addCookie(isAdminCookie);

                return user;
            }
            return null;
        });
    }

    public void updateUserRole(String username, Rol newRole) {
        Optional<CustomUser> userOptional = customUserRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            CustomUser user = userOptional.get();
            user.setRol(newRole);
            customUserRepository.save(user);
        }
    }
    public List<CustomUser> getAllUsers() {
        return customUserRepository.findAll();
    }
}