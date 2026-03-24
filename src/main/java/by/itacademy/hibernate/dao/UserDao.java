package by.itacademy.hibernate.dao;


import by.itacademy.hibernate.entity.*;

import static by.itacademy.hibernate.entity.QChat.chat;
import static by.itacademy.hibernate.entity.QCompany.company;
import static by.itacademy.hibernate.entity.QPayment.payment;
import static by.itacademy.hibernate.entity.QUser.user;
import static by.itacademy.hibernate.entity.QUserChat.userChat;

import by.itacademy.hibernate.entity.QUser;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import by.itacademy.hibernate.dto.PaymentFilter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;



@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDao {

    private static final UserDao INSTANCE = new UserDao();

    /**
     * Возвращает всех сотрудников
     */
    public List<User> findAll(Session session) {
    // CritariaAPI

//        var cb = session.getCriteriaBuilder();
//        var criteria = cb.createQuery(User.class);
//        var user = criteria.from(User.class);
//
//        criteria.select(user); // Why does it work without this line?
//
//        return session.createSelectionQuery(criteria).list();

    //CritariaAPI + queryDSL

        //return new JPAQuery<User>().select(QUser.user).from(QUser.user).fetch();  option + Enter -> add static import
        return new JPAQuery<User>(session).select(user)
                                   .from(user)
                                   .fetch();

    }

    /**
     * Возвращает всех сотрудников с указанным именем
     */
    public List<User> findAllByFirstName(Session session, String firstName) {

    // CriteriaAPI + hibernate-jpamodelgen

//        var cb = session.getCriteriaBuilder();
//        var criteria = cb.createQuery(User.class);
//        var user = criteria.from(User.class);
//
//        criteria.select(user).where(cb.equal(user.get(User_.personalInfo).get(PersonalInfo_.firstname), firstName));
//
//        return session.createSelectionQuery(criteria).list();

    //CriteriaAPI + queryDSL
        return new JPAQuery<User>(session).select(user)
                                   .from(user)
                                   .where(user.personalInfo.firstname.eq(firstName))
                                   .fetch();
  }

    /**
     * Возвращает первые {limit} сотрудников, упорядоченных по дате рождения (в порядке возрастания)
     */
    public List<User> findLimitedUsersOrderedByBirthday(Session session, int limit) {

        return new JPAQuery<User>(session).select(user)
                               .from(user)
                               .orderBy(new OrderSpecifier(Order.ASC, user.personalInfo.birthDate))
                               .limit(limit)
                               .fetch();
    }

    /**
     * Возвращает всех сотрудников компании с указанным названием
     */
    public List<User> findAllByCompanyName(Session session, String companyName) {
        return new JPAQuery<User>(session).select(user)
                               .from(company)
                               .join(company.users, user)
                               .where(company.name.eq(companyName))
                               .fetch();

    }

    /**
     * Возвращает все выплаты, полученные сотрудниками компании с указанными именем,
     * упорядоченные по имени сотрудника, а затем по размеру выплаты
     */
    public List<Payment> findAllPaymentsByCompanyName(Session session, String companyName) {
        // CriteriaAPI + hibernate-jpamodelgen

//        CriteriaBuilder cb = session.getCriteriaBuilder();
//
//        CriteriaQuery<Payment> criteria = cb.createQuery(Payment.class);
//        Root<Payment> payment = criteria.from(Payment.class);
//        Join<Payment, User> user = payment.join(Payment_.receiver);
//        Join<User, Company> company = user.join(User_.company);
//
//        criteria.select(payment).where(
//                        cb.equal(company.get(Company_.name), companyName)
//                 )
//                .orderBy(
//                        cb.asc(user.get(User_.personalInfo).get(PersonalInfo_.firstname)),
//                        cb.asc(payment.get(Payment_.amount))
//                );
//        return session.createSelectionQuery(criteria).list();

    //CriteriaAPI + queryDSL
        return new JPAQuery<Payment>(session).select(payment)
                .from(company)
                .join(company.users, user)
                .join(user.payments, payment)
                .where(company.name.eq(companyName))
                .orderBy(user.personalInfo.firstname.asc(), payment.amount.asc())
                .fetch();
    }

    /**
     * Возвращает среднюю зарплату сотрудника с указанными именем и фамилией
     */
    public Double findAveragePaymentAmountByFirstAndLastNames(Session session, PaymentFilter filter) {
//        List<Predicate> predicates = new ArrayList<>();
//        if(filter.getFirstName() != null){
//            predicates.add(user.personalInfo().firstname.eq(filter.getFirstName()));
//        }
//        if(filter.getLastName() != null){
//            predicates.add(user.personalInfo().lastname.eq(filter.getLastName()));
//        }
        var predicate = QPredicate.builder()
                .add(filter.getFirstName(),  e -> user.personalInfo.firstname.eq(e))
                .add(filter.getLastName(), user.personalInfo.lastname::eq)
                .buildAnd();

        return new JPAQuery<Double>(session).select(payment.amount.avg())
                .from(payment)
                .join(payment.receiver, user)
//                .where(user.personalInfo().firstname).eq(firstName)
//                        .and(user.personalInfo().lastname).eq(lastName)

//                .where(predicates.toArray(Predicate[]::new))

                .where(predicate)
                .fetchOne();
    }

    /**
     * Возвращает для каждой компании: название, среднюю зарплату всех её сотрудников. Компании упорядочены по названию.
     */
    public List<Tuple> findCompanyNamesWithAvgUserPaymentsOrderedByCompanyName(Session session) {
        return new JPAQuery<Tuple>(session).select(company.name, payment.amount.avg())
                .from(company)
                .join(company.users, user)
                .join(user.payments, payment)
                .groupBy(company.name)
                .orderBy(payment.amount.avg().asc(), company.name.asc())
                .fetch();

    }


    /**
     * Возвращает список: сотрудник (объект User), средний размер выплат, но только для тех сотрудников, чей средний размер выплат
     * больше среднего размера выплат всех сотрудников
     * Упорядочить по имени сотрудника
     */
    public List<Tuple> isItPossible(Session session) {
        return new JPAQuery<Tuple>(session).select(user, payment.amount.avg())
                .from(payment)
                .join(payment.receiver, user)
                .groupBy(user.id)
                .having(payment.amount.avg().gt(
                                new JPAQuery<Double>().select(payment.amount.avg())
                                                      .from(payment)
                ))
                .orderBy(user.personalInfo.firstname.asc())
                .fetch();
    }

    /**
     * Возвращает список: имя чата и количество участников в нем
     */
    public List<Tuple> findHowManyParticipantsChatsHave(Session session){
        return new JPAQuery<Tuple>(session).select(chat.name, user.count())
                .from(chat)
                .join(chat.userChats, userChat)
                .join(userChat.user, user)
                .groupBy(chat.id)
                .orderBy(user.count().asc())
                .fetch();
    }


    /**
     * Возвращает список: самого старого и самого молодого юзера
     */
    public List<User> findTheOldestAndTheYoungestUsers(Session session){
        return new JPAQuery<User>(session)
                .select(user)
                .from(user)
                .where(user.personalInfo.birthDate.eq(
                        JPAExpressions.select(user.personalInfo.birthDate.min()).from(user)
                ).or(user.personalInfo.birthDate.eq(
                        JPAExpressions.select(user.personalInfo.birthDate.max()).from(user)
                )))
                .orderBy(user.personalInfo.birthDate.asc())
                .fetch();

    }


    /**
     * Возвращает список: юзера и его статус в соответствии с его активностью в чатах:
     * состоит в > 3 чатах -> active
     *         В 1-3 чатах -> passive
     *         в 0 чатах -> inactive
     */
    public List<Tuple> findUserEngagement(Session session){
        QUser user = QUser.user;
        StringExpression engagementCategory = new CaseBuilder()
                .when(user.userChats.size().gt(3)).then("Active")
                .when(user.userChats.size().eq(0)).then("Inactive")
                .otherwise("Passive");

        return new JPAQuery<Tuple>(session).select(user, engagementCategory)
                .from(user)
                .fetch();
    }


    /**
     * Возвращает список юзеров и количество чатов, в которых они состоят
     */
    public List<Tuple> findHowManyChatsUsersAreMembersOf(Session session){
        return new JPAQuery<Tuple>(session).select(user, chat.count())
                .from(user)
                .leftJoin(user.userChats, userChat)
                .leftJoin(userChat.chat, chat)
                .groupBy(user.id)
                .fetch();
    }


    /**
     * Возвращает список юзеров, которые за все время заработали больше 1000
     */
    public List<Tuple> findWhoEarnedMoreThan1000(Session session){
        return new JPAQuery<Tuple>(session).select(user, payment.amount.sum())
                .from(user)
                .join(user.payments, payment)
                .groupBy(user.id)
                .having(payment.amount.sum().gt(1000.0))
                .fetch();
    }



        public static UserDao getInstance() {
        return INSTANCE;
    }
}