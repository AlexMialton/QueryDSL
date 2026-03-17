package by.itacademy.hibernate.dao;


import by.itacademy.hibernate.entity.Payment;
import by.itacademy.hibernate.entity.User;
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
        return session.createSelectionQuery("""
                                                  SELECT u
                                                  FROM User u
                                                  """,
                                                 User.class)
                                                 .list();
    }

    /**
     * Возвращает всех сотрудников с указанным именем
     */
    public List<User> findAllByFirstName(Session session, String firstName) {

        return session.createSelectionQuery("""
                                                  SELECT u
                                                  FROM User u
                                                  WHERE u.personalInfo.firstname = :name
                                                  """,
                                                  User.class)
                                                 .setParameter("name", firstName)
                                                 .list();
    }

    /**
     * Возвращает первые {limit} сотрудников, упорядоченных по дате рождения (в порядке возрастания)
     */
    public List<User> findLimitedUsersOrderedByBirthday(Session session, int limit) {

        return session.createSelectionQuery("""
                                                  SELECT u
                                                  FROM User u
                                                  ORDER BY u.personalInfo.birthDate
                                                  LIMIT :limit
                                                  """,
                                                  User.class)
                                                  .setParameter("limit", limit)
                                                  .list();
    }

    /**
     * Возвращает всех сотрудников компании с указанным названием
     */
    public List<User> findAllByCompanyName(Session session, String companyName) {
        return session.createSelectionQuery("""
                                                  SELECT u
                                                  FROM User u
                                                  WHERE u.company.name = :companyName
                                                  """,
                                                  User.class)
                                                  .setParameter("companyName", companyName)
                                                  .list();
    }

    /**
     * Возвращает все выплаты, полученные сотрудниками компании с указанными именем,
     * упорядоченные по имени сотрудника, а затем по размеру выплаты
     */
    public List<Payment> findAllPaymentsByCompanyName(Session session, String companyName) {
        return session.createSelectionQuery("""
                                                  SELECT p
                                                  FROM Payment p
                                                  WHERE p.receiver.company.name = :companyName
                                                  ORDER BY p.receiver.personalInfo.firstname, p.amount
                                                  """,
                                                  Payment.class)
                                                  .setParameter("companyName", companyName)
                                                  .list();
    }

    /**
     * Возвращает среднюю зарплату сотрудника с указанными именем и фамилией
     */
    public Double findAveragePaymentAmountByFirstAndLastNames(Session session, String firstName, String lastName) {
        List<Double> result = session.createSelectionQuery("""
                                                  SELECT AVG(p.amount)
                                                  FROM Payment p
                                                  WHERE p.receiver.personalInfo.firstname = :firstName
                                                  AND p.receiver.personalInfo.lastname = :lastName
                                                  """,
                                                  Double.class)
                                                 .setParameter("firstName", firstName)
                                                 .setParameter("lastName", lastName)
                                                 .list();

        return result.get(0);
    }

    /**
     * Возвращает для каждой компании: название, среднюю зарплату всех её сотрудников. Компании упорядочены по названию.
     */
    public List<Object[]> findCompanyNamesWithAvgUserPaymentsOrderedByCompanyName(Session session) {
        return session.createSelectionQuery("""
                                                  SELECT c.name, AVG(p.amount)
                                                  FROM User u
                                                  JOIN Payment p ON u.id = p.receiver.id
                                                  JOIN Company c ON c.id = u.company.id
                                                  GROUP BY c.name
                                                  ORDER BY c.name
                                                  """,
                                                  Object[].class)
                                                 .list();
    }

    /**
     * Возвращает список: сотрудник (объект User), средний размер выплат, но только для тех сотрудников, чей средний размер выплат
     * больше среднего размера выплат всех сотрудников
     * Упорядочить по имени сотрудника
     */
    public List<Object[]> isItPossible(Session session) {
        return session.createSelectionQuery("""
                                                  SELECT u, AVG(p.amount)
                                                  FROM User u
                                                  JOIN Payment p ON u.id = p.receiver.id
                                                  GROUP BY u
                                                  HAVING AVG(p.amount) > (SELECT AVG(p.amount)
                                                              FROM Payment p)
                                                  """,
                                                  Object[].class)
                                                 .list();
    }

    public static UserDao getInstance() {
        return INSTANCE;
    }
}