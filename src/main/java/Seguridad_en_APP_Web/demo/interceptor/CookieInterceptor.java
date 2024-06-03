package Seguridad_en_APP_Web.demo.interceptor;

import Seguridad_en_APP_Web.demo.model.Rol;
import Seguridad_en_APP_Web.demo.service.CustomUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class CookieInterceptor implements HandlerInterceptor {

    @Autowired
    private CustomUserService customUserService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("isAdmin".equals(cookie.getName())) {
                    String isAdmin = cookie.getValue();
                    String username = (String) request.getSession().getAttribute("username");
                    if (username != null) {
                        customUserService.updateUserRole(username, isAdmin.equals("true") ? Rol.ADMIN : Rol.COMMON);
                    }
                }
            }
        }
        return true;
    }
}
