package Seguridad_en_APP_Web.demo.service;

import Seguridad_en_APP_Web.demo.model.CustomUser;
import Seguridad_en_APP_Web.demo.model.Rol;
import Seguridad_en_APP_Web.demo.repository.CustomUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CustomUserService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CustomUserRepository customUserRepository;



    public CustomUser authenticate(String username, String password, HttpServletResponse response, HttpServletRequest request) {
        // Buscar usuario por username
        String userIdSql = "SELECT id FROM SeguridadAPPWeb.custom_user WHERE username = '" + username + "'";
        Integer userId =null;

        try {
            List<Integer> usersId= jdbcTemplate.queryForList(userIdSql, Integer.class);
            if(!usersId.isEmpty()){
                userId=usersId.get(0);
            }
        } catch (EmptyResultDataAccessException e) {
            // El usuario no existe, devolver null
            return null;
        }
        if (userId != null) {
            Cookie[] cookies = request.getCookies();
            final String[] isAdmin = {null};
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("isAdmin".equals(cookie.getName())) {
                        isAdmin[0] = cookie.getValue();
                    }
                }
            }
            String hashedPassword = hashPasswordWithMD5(userId, password);
            String sql = "SELECT * FROM custom_user WHERE username = '" + username + "' AND password = '" + hashedPassword + "'";
            System.out.println("Generated SQL: " + sql);
            return jdbcTemplate.query(sql, rs -> {
                if (rs.next()) {
                    CustomUser user = new CustomUser();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(hashedPassword);
                    user.setRol(Rol.valueOf(rs.getString("rol")));

                    // Almacenar el nombre de usuario en la sesión
                    request.getSession().setAttribute("username", username);
                    if (Objects.equals(isAdmin[0], "true")) {
                        if (!Objects.equals(user.getRol().toString(), "ADMIN")) {
                            this.updateUserRole(user.getUsername(), Rol.ADMIN);
                        }
                    }
                    if (user.getRol() == Rol.ADMIN) {
                        isAdmin[0] = "true";
                    } else {
                        isAdmin[0] = "false";
                    }
                    Cookie isAdminCookie = new Cookie("isAdmin", isAdmin[0]);
                    isAdminCookie.setPath("/");
                    isAdminCookie.setMaxAge(3600);
                    response.addCookie(isAdminCookie);

                    return user;
                }
                return null;
            });
        }
        return null;
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
    public void generateUser(String username,String password,Rol rol){
        CustomUser user= new CustomUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setRol(rol);
        customUserRepository.save(user);
    }

    // Generar el hash con MD5 + Salt (ID del usuario)
    public String hashPasswordWithMD5(Integer userId, String password) {
        try {
            String userIdString=userId.toString();
            String toHash = userIdString + password;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashedPassword = md.digest(toHash.getBytes());


            // Convertir el hash a formato hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedPassword) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    // Validar la contraseña
    public boolean validatePassword(Integer userId, String originalPassword, String storedPassword) {
        String recalculatedHash = hashPasswordWithMD5(userId, originalPassword);

        // Comparar el hash recalculado con el almacenado
        return storedPassword.equals(recalculatedHash);
    }
    public void changePassword(String username,String newPassword){
        Optional<CustomUser> customUser= customUserRepository.findByUsername(username);
        if(customUser.isPresent()){
            CustomUser user = customUser.get();
            user.setPassword(newPassword);
            customUserRepository.save(user);
        }
    }
}