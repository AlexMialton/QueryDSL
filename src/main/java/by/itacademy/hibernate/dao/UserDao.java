package by.itacademy.hibernate.dao;


import by.itacademy.hibernate.entity.*;

import static by.itacademy.hibernate.entity.QCompany.company;
import static by.itacademy.hibernate.entity.QPayment.payment;
import static by.itacademy.hibernate.entity.QUser.user;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import by.itacademy.hibernate.dto.PaymentFilter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.Session;

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
        return new JPAQuery<User>().select(user)
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
        return new JPAQuery<User>().select(user)
                                   .from(user)
                                   .where(user.personalInfo().firstname.eq(firstName))
                                   .fetch();
  }

    /**
     * Возвращает первые {limit} сотрудников, упорядоченных по дате рождения (в порядке возрастания)
     */
    public List<User> findLimitedUsersOrderedByBirthday(Session session, int limit) {

        return JPAQuery<User>().select(user)
                               .from(user)
                               .orderBy(new OrderSpecifier(Order.ASC, user.personalInfo().birthDate))
                               .limit(limit)
                               .fetch();
    }

    /**
     * Возвращает всех сотрудников компании с указанным названием
     */
    public List<User> findAllByCompanyName(Session session, String companyName) {
        return JPAQuery<User>().select(user)
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
        return JPAQuery<Payment>().select(payment)
                .from(user)
                .join(company.users)
                .join(user.payments)
                .where(company.name.eq(companyName))
                .orderBy(user.personalInfo().firstname.asc(), payment.amount.asc())
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
                .add(user.personalInfo().firstname,  e -> user.personalInfo().firstname.eq(e))
                .add(user.personalInfo().lastname, user.personalInfo().lastname::eq)
                .buildAnd();

        return JPAQuery<Double>().select(payment.amount.avg())
                .from(payment)
                .join(payment.receiver(), user)
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
        return new JPAQuery<Tuple>().select(company.name, payment.amount.avg())
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
        return JPAQuery<Tuple>().select(user, payment.amount.avg())
                .from(user)
                .join(payment.receiver(), user)
                .groupBy(user.id)
                .having(payment.amount.avg().gt(
                                new JPAQuery<Double>().select(payment.amount.avg())
                                                      .from(payment)
                ))
                .orderBy(user.personalInfo().firstname.asc())
                .fetch();
    }

    public static UserDao getInstance() {
        return INSTANCE;
    }
}