package Seguridad_en_APP_Web.demo.controller;
// src/main/java/com/example/demo/controller/UserController.java

import Seguridad_en_APP_Web.demo.model.CustomUser;
import Seguridad_en_APP_Web.demo.service.CustomUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@Controller
public class UserController {
    @Autowired
    private CustomUserService customUserService;

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("custom_user", new CustomUser());
        return "login";
    }
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpServletResponse response, Model model) {
        CustomUser user = customUserService.authenticate(username, password,response);
        if (user != null) {
            model.addAttribute("user", user);
            return "welcome";
        }
        model.addAttribute("error", "Invalid username or password");
        return "login";
    }

   /* @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("custom_user", new custom_user());
        return "register";
    }


    */
    /*@PostMapping("/register")
    public String register(@ModelAttribute("custom_user") custom_user customUser) {
        userService.saveUser(customUser);
        return "redirect:/login";
    }
     */

    /*@GetMapping("/welcome")
    public String welcome() {
        return "welcome";
    }
     */
}
