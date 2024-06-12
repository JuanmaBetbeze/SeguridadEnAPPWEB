package Seguridad_en_APP_Web.demo.service;

import Seguridad_en_APP_Web.demo.model.CreditCard;
import Seguridad_en_APP_Web.demo.model.CustomUser;
import Seguridad_en_APP_Web.demo.model.Rol;
import Seguridad_en_APP_Web.demo.repository.CreditCardRepository;
import Seguridad_en_APP_Web.demo.repository.CustomUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class CustomUserService {
    @Autowired
    private Validator validator;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CustomUserRepository customUserRepository;

    @Autowired
    private CreditCardRepository creditCardRepository;



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
        CustomUser userOptional = customUserRepository.findByUsername(username);
        if (userOptional!=null) {
            userOptional.setRol(newRole);
            customUserRepository.save(userOptional);
        }
    }
    public List<CustomUser> getAllUsers() {
        return customUserRepository.findAll();
    }
    public void generateUser(String username,String password,Rol rol,String nombre,String apellido,Date fecha){
        CustomUser user= new CustomUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setRol(rol);
        user.setNombre(nombre);
        user.setApellido(apellido);
        user.setFechaNacimiento(fecha);
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
        CustomUser customUser= customUserRepository.findByUsername(username);
        if(customUser!=null){
            customUser.setPassword(newPassword);
            customUserRepository.save(customUser);
        }
    }
    public void addCreditCardToUser(String userName, String cardNumber, String cardHolderName,int cvv,double montoDispo) {
        CustomUser user = customUserRepository.findByUsername(userName);
        if(user==null) throw new RuntimeException("User not found");
        CreditCard creditCard = new CreditCard();
        creditCard.setCardNumber(cardNumber);
        creditCard.setCardHolderName(cardHolderName);
        creditCard.setCustomUser(user);
        creditCard.setCvv(cvv);
        creditCard.setMontoDisponible(montoDispo);

        Set<ConstraintViolation<CreditCard>> violations = validator.validate(creditCard);
        if (!violations.isEmpty()) {
            throw new RuntimeException("Invalid credit card data");
        }
        creditCardRepository.save(creditCard);
        user.setCreditCard(creditCard);
        customUserRepository.save(user);
    }


    public void transferMoney(String usernameINI,String usernameFIN,int cvv,double amount){
        CustomUser user1 = customUserRepository.findByUsername(usernameINI);
        CustomUser user2 = customUserRepository.findByUsername(usernameFIN);
        if(user1==null || user2==null)throw new RuntimeException("User not found");
        else {
            CreditCard card1=user1.getCreditCard();
            if (card1.getCvv()!=cvv){
                throw new RuntimeException("Cvv incorrect");
            }
            else {
                CreditCard card2= user2.getCreditCard();
                if (amount>card1.getMontoDisponible()){
                    throw new RuntimeException("You don't have this amount to transfer");
                }
                else {
                    card1.setMontoDisponible(card1.getMontoDisponible()-amount);
                    card2.setMontoDisponible(card2.getMontoDisponible()+amount);
                    creditCardRepository.save(card1);
                    creditCardRepository.save(card2);
                    user1.setCreditCard(card1);
                    customUserRepository.save(user1);
                    user2.setCreditCard(card2);
                    customUserRepository.save(user2);
                }
            }
        }
    }
    public void setCreditCard(CustomUser customUser){
        customUser.setCreditCard(creditCardRepository.findByCustomUserId(customUser.getId()));
    }
    public CustomUser findUserByName(String nombreUsuario){
        CustomUser user = customUserRepository.findByUsername(nombreUsuario);

        if (user==null){
            throw new RuntimeException("User not found");
        }
        else {
            setCreditCard(user);
            return user;
        }
    }
}