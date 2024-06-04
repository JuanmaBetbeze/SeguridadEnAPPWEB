package Seguridad_en_APP_Web.demo.utils;

import Seguridad_en_APP_Web.demo.model.CustomUser;
import Seguridad_en_APP_Web.demo.model.Rol;
import Seguridad_en_APP_Web.demo.service.CustomUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class createUsers implements CommandLineRunner {

    @Autowired
    CustomUserService customUserService;


    @Override
    public void run(String... args) throws Exception {
        this.createUsersInit();
        this.HashPasswords();


    }
    public  void createUsersInit(){
        customUserService.generateUser("CommonUser","password1234",Rol.COMMON);
        customUserService.generateUser("CommonUser2","password4321",Rol.COMMON);
        customUserService.generateUser("administrador","admin123",Rol.ADMIN);
    }
    public void HashPasswords(){

        List<CustomUser> customUserList=customUserService.getAllUsers();
        customUserList.forEach(customUser ->
                customUserService.changePassword(customUser.getUsername(),customUserService.hashPasswordWithMD5(customUser.getId(),customUser.getPassword()))
        );
    }
}