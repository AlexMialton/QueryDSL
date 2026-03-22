package by.itacademy.hibernate;

import by.itacademy.hibernate.convertor.BirthdayConvertor;
import by.itacademy.hibernate.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import by.itacademy.hibernate.entity.User;

public class HIbernateRunner {
    public static void main (String[] rgs){
        SessionFactory sessionFactory = HibernateUtil.buildSessionFactory();
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        var user = session.find(User.class, 1L);

        session.getTransaction().commit();


    }
}
