package by.itacademy.hibernate.utils;


import by.itacademy.hibernate.entity.*;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

@UtilityClass
public class TestDataImporter {

    public void importData(SessionFactory sessionFactory) {
        @Cleanup Session session = sessionFactory.openSession();

        Company microsoft = saveCompany(session, "Microsoft");
        Company apple = saveCompany(session, "Apple");
        Company google = saveCompany(session, "Google");

        User billGates = saveUser(session, "Bill", "Gates",
                LocalDate.of(1955, Month.OCTOBER, 28), microsoft);
        User steveJobs = saveUser(session, "Steve", "Jobs",
                LocalDate.of(1955, Month.FEBRUARY, 24), apple);
        User sergeyBrin = saveUser(session, "Sergey", "Brin",
                LocalDate.of(1973, Month.AUGUST, 21), google);
        User timCook = saveUser(session, "Tim", "Cook",
                LocalDate.of(1960, Month.NOVEMBER, 1), apple);
        User dianeGreene = saveUser(session, "Diane", "Greene",
                LocalDate.of(1955, Month.JANUARY, 1), google);

        savePayment(session, billGates, 100);
        savePayment(session, billGates, 300);
        savePayment(session, billGates, 500);

        savePayment(session, steveJobs, 250);
        savePayment(session, steveJobs, 600);
        savePayment(session, steveJobs, 500);

        savePayment(session, timCook, 400);
        savePayment(session, timCook, 300);

        savePayment(session, sergeyBrin, 500);
        savePayment(session, sergeyBrin, 500);
        savePayment(session, sergeyBrin, 500);

        savePayment(session, dianeGreene, 300);
        savePayment(session, dianeGreene, 300);
        savePayment(session, dianeGreene, 300);


        Chat chat1 = saveChat(session, "work");
        Chat chat2 = saveChat(session, "Apple");
        Chat chat3 = saveChat(session, "project one");
        Chat chat4 = saveChat(session, "project two");
        Chat chat5 = saveChat(session, "Microsoft");
        Chat chat6 = saveChat(session, "Tim's bday");


        saveUserChat(session, dianeGreene, chat1);
        saveUserChat(session, steveJobs, chat1);
        saveUserChat(session, timCook, chat1);

        saveUserChat(session, dianeGreene, chat2);
        saveUserChat(session, sergeyBrin, chat2);

        saveUserChat(session, sergeyBrin, chat3);
        saveUserChat(session, timCook, chat3);
        saveUserChat(session, steveJobs, chat3);

        saveUserChat(session, timCook, chat4);
        saveUserChat(session, steveJobs, chat4);

        saveUserChat(session, dianeGreene, chat5);
        saveUserChat(session, timCook, chat5);
        saveUserChat(session, steveJobs, chat5);

        saveUserChat(session, sergeyBrin, chat6);
        saveUserChat(session, steveJobs, chat6);
        saveUserChat(session, timCook, chat6);
        saveUserChat(session, dianeGreene, chat6);

    }

    private Company saveCompany(Session session, String name) {
        session.beginTransaction();
        Company company = Company.builder()
                .name(name)
                .build();
        session.persist(company);
        session.getTransaction().commit();

        return company;
    }

    private User saveUser(Session session,
                          String firstName,
                          String lastName,
                          LocalDate birthday,
                          Company company) {
        session.beginTransaction();
        User user = User.builder()
                .username(firstName + lastName)
                .personalInfo(PersonalInfo.builder()
                        .firstname(firstName)
                        .lastname(lastName)
                        .birthDate(new Birthday(birthday))
                        .build())
                .company(company)
                .build();
        session.persist(user);
        session.getTransaction().commit();

        return user;
    }

    private void savePayment(Session session, User user, Integer amount) {
        session.beginTransaction();
        Payment payment = Payment.builder()
                .receiver(user)
                .amount(amount)
                .build();
        session.persist(payment);
        session.getTransaction().commit();
    }

    private Chat saveChat(Session session, String name) {
        session.beginTransaction();
        Chat chat = Chat.builder()
                .name(name)
                .build();
        session.persist(chat);
        session.getTransaction().commit();

        return chat;
    }

    private void saveUserChat(Session session, User user, Chat chat) {
        session.beginTransaction();
        UserChat userChat = UserChat.builder()
                .chat(chat)
                .user(user)
                .build();
        session.persist(userChat);
        session.getTransaction().commit();
    }
}