package Seguridad_en_APP_Web.demo.service;


import Seguridad_en_APP_Web.demo.model.CustomUser;
import Seguridad_en_APP_Web.demo.model.Rol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class CustomUserService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public CustomUser authenticate(String username, String password,HttpServletResponse response) {
        // Vulnerable a inyección SQL
        String sql = "SELECT * FROM SeguridadAPPWeb.custom_user WHERE username = '" + username + "' AND password = '" + password + "'";
        System.out.println("Generated SQL: " + sql);
        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                CustomUser user = new CustomUser();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRol(Rol.valueOf(rs.getString("rol")));
                String isAdmin = "false";
                if (user.getRol() == Rol.ADMIN) {
                    isAdmin = "true";
                }
                Cookie isAdminCookie = new Cookie("isAdmin", isAdmin);
                isAdminCookie.setPath("/login");
                isAdminCookie.setMaxAge(3600);
                response.addCookie(isAdminCookie);

                return user;
            }
            return null;
        });
    }
}
