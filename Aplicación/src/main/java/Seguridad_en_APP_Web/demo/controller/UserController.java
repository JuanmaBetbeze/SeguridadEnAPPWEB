package Seguridad_en_APP_Web.demo.controller;
// src/main/java/com/example/demo/controller/UserController.java

import Seguridad_en_APP_Web.demo.model.CustomUser;
import Seguridad_en_APP_Web.demo.model.Rol;
import Seguridad_en_APP_Web.demo.service.CustomUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class UserController {
    @Autowired
    private CustomUserService customUserService;

    @GetMapping("/login")
    public String showLoginForm(HttpServletRequest request, HttpServletResponse response,Model model)  {
        // Invalidate the session if it exists
        if (request.getSession(false) != null) {
            request.getSession().invalidate();
        }
        model.addAttribute("custom_user", new CustomUser());
        Cookie cookie = new Cookie("isAdmin", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "loginprueba";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                        HttpServletResponse response, HttpServletRequest request, Model model) {
        CustomUser user = customUserService.authenticate(username, password, response,request);
        if (user != null) {
            request.getSession().setAttribute("user", user);
            request.getSession().setAttribute("isAdmin", user.getRol() == Rol.ADMIN ? "true" : "false");
            return "redirect:/welcome";
        }
        model.addAttribute("error", "Invalid username or password");
        return "loginprueba";
    }
    @GetMapping("/welcome")
    public String showWelcomePage(HttpServletRequest request, Model model) {
        CustomUser user = (CustomUser) request.getSession().getAttribute("user");
        user=customUserService.findUserByName(user.getUsername());
        if (user == null) {
            return "redirect:/login";
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("isAdmin".equals(cookie.getName())) {
                    boolean isAdmin = "true".equals(cookie.getValue());
                    if (isAdmin){
                        customUserService.updateUserRole(user.getUsername(),Rol.ADMIN);
                        user.setRol(Rol.ADMIN);
                    }
                }
            }
        }
        customUserService.setCreditCard(user);
        model.addAttribute("user", user);
        return "welcomeprueba";
    }

    @GetMapping("/updateCommonUser")
        public String updateCommonUser(HttpServletResponse response,HttpServletRequest request) {
        customUserService.updateUserRole("usuarioNormal", Rol.COMMON);
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("isAdmin".equals(cookie.getName())) {
                    response.resetBuffer();
                }
            }
        }
        return "redirect:/login";

    }


    @GetMapping("/welcome/admin")
    public String showAdminPage(HttpServletRequest request, Model model) {
        CustomUser user = (CustomUser) request.getSession().getAttribute("user");
        user=customUserService.findUserByName(user.getUsername());
        if (user == null || user.getRol() != Rol.ADMIN) {
            return "redirect:/login";
        }
        model.addAttribute("users", customUserService.getAllUsers());
        return "adminprueba";
    }

    @GetMapping("/cuenta")
    public String showMiCuenta(HttpServletRequest request, Model model) {
        CustomUser user = (CustomUser) request.getSession().getAttribute("user");
        user=customUserService.findUserByName(user.getUsername());
        if (user == null ) {
            return "redirect:/login";
        }
        customUserService.setCreditCard(user);
        model.addAttribute("user", user);
        return "micuenta";
    }

    @GetMapping("/user/transfer")
    public String showTransferPage(HttpServletRequest request, Model model) {
        CustomUser user = (CustomUser) request.getSession().getAttribute("user");
        user=customUserService.findUserByName(user.getUsername());
        if (user == null ) {
            return "redirect:/login";
        }
        customUserService.setCreditCard(user);
        model.addAttribute("user", user);
        return "transferenciaprueba";
    }

    @PostMapping("/user/transfer")
    public String transferMoney(@RequestParam("username") String username,
                                @RequestParam("monto") String monto,
                                @RequestParam("destinatario") String destinatario,
                                @RequestParam("cvv") String cvv,
                                Model model) {
        try {

            int cvvInt = Integer.parseInt(cvv);
            double amount = Double.parseDouble(monto);
            customUserService.transferMoney(username, destinatario, cvvInt, amount);
            CustomUser user=customUserService.findUserByName(username);
            model.addAttribute("user",user);
            // CREAR MENSAJE DE TRANSFERENCIA CORRECTA
            return "redirect:/welcome";
        } catch (RuntimeException e) {

            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("monto", monto);
            model.addAttribute("destinatario", destinatario);
            CustomUser user=customUserService.findUserByName(username);
            model.addAttribute("user",user);
            return "transferenciaprueba";
        }
    }

}
