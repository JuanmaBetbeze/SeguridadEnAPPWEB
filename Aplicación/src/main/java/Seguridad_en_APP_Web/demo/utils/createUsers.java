package Seguridad_en_APP_Web.demo.utils;

import Seguridad_en_APP_Web.demo.model.CustomUser;
import Seguridad_en_APP_Web.demo.model.Rol;
import Seguridad_en_APP_Web.demo.service.CustomUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
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
        customUserService.generateUser("CommonUser","password1234",Rol.COMMON,"Juan","Carlos",new Date(2000, Calendar.JANUARY,15));
        customUserService.generateUser("CommonUser2","password4321",Rol.COMMON,"Pedro","Martinez",new Date(1978,Calendar.MARCH,25));
        customUserService.generateUser("administrador","admin123",Rol.ADMIN,"Ignacio","Suar",new Date(1986,Calendar.DECEMBER,12));
        customUserService.addCreditCardToUser("CommonUser","1506220433118732","Juan Carlos",321,50000);
        customUserService.addCreditCardToUser("CommonUser2","3265465723436634","Pedro Martinez",752,20000);
        customUserService.addCreditCardToUser("administrador","4627364176227353","Ignacio Suar",235,100000);
    }
    public void HashPasswords(){

        List<CustomUser> customUserList=customUserService.getAllUsers();
        customUserList.forEach(customUser ->
                customUserService.changePassword(customUser.getUsername(),customUserService.hashPasswordWithMD5(customUser.getId(),customUser.getPassword()))
        );
    }
}