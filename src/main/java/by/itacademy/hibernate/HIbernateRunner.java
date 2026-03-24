package by.itacademy.hibernate;

import by.itacademy.hibernate.convertor.BirthdayConvertor;
import by.itacademy.hibernate.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import by.itacademy.hibernate.entity.User;
import by.itacademy.hibernate.entity.UserChat;
import org.hibernate.graph.GraphSemantic;

import java.util.Map;

public class HIbernateRunner {
    public static void main (String[] rgs){
        SessionFactory sessionFactory = HibernateUtil.buildSessionFactory();
        Session session = sessionFactory.openSession();
        session.beginTransaction();

//        var user = session.find(User.class, 1L);
//
//        System.out.println(user.getCompany());
//        System.out.println(user.getPayments());
//       ___________________________________________________________
//        var users = session.createSelectionQuery("select u from User u join fetch u.payments" // solving N+1 problem with join fetch
//                                                    , User.class).list();
//        for(User u:users){
//            System.out.println(u.getPayments());
//       ___________________________________________________________

//        session.enableFetchProfile("withCompanyAndPayments");
//        var user = session.find(User.class, 1L);
//        System.out.println(user.getCompany().getName());
//        _________________________________________________________

        var userGraph = session.createEntityGraph(User.class);
        userGraph.addAttributeNodes("company", "userChats");
        var userChatsGraph = userGraph.addSubgraph("userChat", UserChat.class);
        userChatsGraph.addAttributeNodes("chat");

        Map<String, Object> properties = Map.of(
                GraphSemantic.LOAD.getJakartaHintName(), userGraph);
        var user = session.find(User.class, 1L, properties);
        System.out.println(user.getCompany().getName());
        System.out.println(user.getUserChats().size());

        var users = session.createSelectionQuery("select u from User u", User.class)
                        .setHint(GraphSemantic.LOAD.getJakartaHintName(), userGraph)
                        .list();
        users.forEach(e -> System.out.println(e.getUserChats().size()));
        users.forEach(e -> System.out.println(e.getCompany().getName()));


        session.getTransaction().commit();


    }
}
