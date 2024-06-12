package Seguridad_en_APP_Web.demo.utils;

import Seguridad_en_APP_Web.demo.model.CustomUser;
import Seguridad_en_APP_Web.demo.model.Rol;
import Seguridad_en_APP_Web.demo.service.CustomUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
        customUserService.generateUser("CommonUser","password1234",Rol.COMMON,"Juan","Carlos",new GregorianCalendar(2000, Calendar.FEBRUARY, 11).getTime());
        customUserService.generateUser("CommonUser2","password4321",Rol.COMMON,"Pedro","Martinez",new GregorianCalendar(1978,Calendar.MARCH,25).getTime());
        customUserService.generateUser("administrador","admin123",Rol.ADMIN,"Ignacio","Suar",new GregorianCalendar(1986,Calendar.DECEMBER,12).getTime());
        customUserService.addCreditCardToUser("CommonUser","1506220433118732","Juan Carlos",321,50000.0);
        customUserService.addCreditCardToUser("CommonUser2","3265465723436634","Pedro Martinez",726,20000.0);
        customUserService.addCreditCardToUser("administrador","4627364176227353","Ignacio Suar",912,100000.0);
    }
    public void HashPasswords(){

        List<CustomUser> customUserList=customUserService.getAllUsers();
        customUserList.forEach(customUser ->
                customUserService.changePassword(customUser.getUsername(),customUserService.hashPasswordWithMD5(customUser.getId(),customUser.getPassword()))
        );
    }
}