package pl.seb.czech.library.visualTesting;

import org.springframework.stereotype.Component;
import pl.seb.czech.library.domain.*;
import pl.seb.czech.library.repositories.BookRepository;
import pl.seb.czech.library.repositories.RentRepository;
import pl.seb.czech.library.repositories.TitleInfoRepository;
import pl.seb.czech.library.repositories.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DataPreparer {
    private static boolean dbInit = false;
    private static List<User> userList = new ArrayList<>();
    private static List<TitleInfo> titleInfoList = new ArrayList<>();
    private static List<Book> bookList = new ArrayList<>();
    private static List<Rent> rentList = new ArrayList<>();

    private BookRepository bookRepository;
    private RentRepository rentRepository;
    private TitleInfoRepository titleInfoRepository;
    private UserRepository userRepository;


    public DataPreparer(BookRepository bookRepository, RentRepository rentRepository, TitleInfoRepository titleInfoRepository, UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.rentRepository = rentRepository;
        this.titleInfoRepository = titleInfoRepository;
        this.userRepository = userRepository;
    }

    public void prepareData() {
        if (!dbInit){
            initializeDB();
            dbInit = true;
        }
    }

    private void initializeDB() {
        TitleInfo it = new TitleInfo("It", "Stephen King", 1986, 12.99);
        TitleInfo braveNW = new TitleInfo("Brave New World", "Aldous Huxley", 1932, 9.99);
        TitleInfo year1984 = new TitleInfo("Nineteen Eighty-Four", "Georg Orwell", 1949, 8.99);
        TitleInfo plague = new TitleInfo("Plague", "Albert Camus", 1947, 7.99);
        TitleInfo catch22 = new TitleInfo("Catch 22", "Joseph Heller", 1961, 11.99);
        TitleInfo cuckoo = new TitleInfo("One Flew Over the Cuckoo’s Nest", "Ken Kesey", 1962, 11.49);
        titleInfoList = Arrays.asList(it, braveNW, year1984, plague, catch22, cuckoo);
        titleInfoRepository.saveAll(titleInfoList);

        int numOfBooksOfEachTitle = 10;

        for (TitleInfo t : titleInfoList) {
            List<Book> temp = new ArrayList<>();
            for (int i = 0; i < numOfBooksOfEachTitle; i++) {
                Book b = new Book(t, BookStatus.AVAILABLE);
                switch (i) {
                    case 0:
                        b.setBookStatus(BookStatus.RENTED);
                        break;
                    case 1:
                        b.setBookStatus(BookStatus.LOST_OR_DESTROYED);
                        break;
                }

                bookList.add(b);
                temp.add(b);
                bookRepository.save(b);
            }
            t.getBookList().addAll(temp);
            titleInfoRepository.save(t);
        }


        User user1 = new User("John", "Smith", LocalDate.of(1, 1, 1));
        User user2 = new User("Atilla", "Hun", LocalDate.of(2, 2, 2));
        User user3 = new User("Julius", "Caesar", LocalDate.of(3, 3, 3));
        User user4 = new User("Sun", "Tzu", LocalDate.of(4, 4, 4));
        User user5 = new User("Cornelius", "Sulla", LocalDate.of(5, 5, 5));
        User user6 = new User("Gaius", "Marius", LocalDate.of(6, 6, 6));


        userList = Arrays.asList(user1, user2, user3, user4, user5, user6);
        userRepository.saveAll(userList);

        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            Book book = bookList.get(i * (numOfBooksOfEachTitle));

            Rent rent = new Rent(user, book);
            rentRepository.save(rent);
            rentList.add(rent);

            user.getRents().add(rent);
            userRepository.save(user);

            book.setRent(rent);
            bookRepository.save(book);
        }
    }

    public void cleanUp() {
        try {
            titleInfoRepository.deleteAll(titleInfoList);
            bookRepository.deleteAll(bookList);
            userRepository.deleteAll(userList);
            rentRepository.deleteAll(rentList);

            titleInfoList.clear();
            bookList.clear();
            userList.clear();
            rentList.clear();

            dbInit = false;

        } catch (Exception e) {

        }


    }


    public static boolean isDbInit() {
        return dbInit;
    }

    public static List<User> getUserList() {
        return userList;
    }

    public static List<TitleInfo> getTitleInfoList() {
        return titleInfoList;
    }

    public static List<Book> getBookList() {
        return bookList;
    }

    public static List<Rent> getRentList() {
        return rentList;
    }
}