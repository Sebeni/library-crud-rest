package pl.seb.czech.library.service;

import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import pl.seb.czech.library.domain.Book;
import pl.seb.czech.library.domain.BookStatus;
import pl.seb.czech.library.domain.TitleInfo;
import pl.seb.czech.library.domain.User;
import pl.seb.czech.library.repositories.BookRepository;
import pl.seb.czech.library.repositories.TitleInfoRepository;
import pl.seb.czech.library.service.exceptions.DataNotFoundException;

import java.time.LocalDate;
import java.util.Optional;


@AllArgsConstructor
@Service
public class BookService {
    private BookRepository bookRepository;
    private TitleInfoRepository titleInfoRepository;
    private TitleInfoService titleInfoService;

    public Book addNewBook(String title, String authorName, Integer publicationYear) throws DataNotFoundException {
        Optional<TitleInfo> titleInfo = titleInfoRepository.findByTitleAndAuthorAndPublicationYear(title, authorName, publicationYear);
        if (titleInfo.isPresent()) {
            TitleInfo current = titleInfo.get();
            return addNewBook(current);
        } else {
            throw new DataNotFoundException("book", title, authorName, publicationYear.toString());
        }
    }

    public Book addNewBook(TitleInfo titleInfo) {
        Book bookToAdd = new Book(titleInfo, BookStatus.AVAILABLE);
        return saveBook(bookToAdd);
    }
    
    public Book addNewBook(Long titleInfoId) {
        return addNewBook(titleInfoService.findById(titleInfoId));
    }

    Book changeBookStatusById(Long id, BookStatus changedStatus) throws DataNotFoundException {
        Book book = findById(id);
        return changeBookStatusByBook(book, changedStatus);
    }

    Book changeBookStatusByBook(Book book, BookStatus changedStatus) {
        book.setBookStatus(changedStatus);
        return bookRepository.save(book);
    }
    
    public Book changeBookStatusByIdFromController(Long id, BookStatus changedStatus) {
        Book book = findById(id);
        if(!book.getBookStatus().equals(BookStatus.RENTED) && !changedStatus.equals(BookStatus.RENTED)) {
            return changeBookStatusById(id, changedStatus);
        } else if (book.getBookStatus().equals(BookStatus.RENTED)) {
            throw new IllegalArgumentException("Can't change rented status");
        } else if (changedStatus.equals(BookStatus.RENTED)) {
            throw new IllegalArgumentException("Can't change to rented status");
        } else {
            throw new IllegalArgumentException(changedStatus + "does not exist");
        }
    }

    public void deleteById(Long id) {
        Book bookToDelete = findById(id);
        if (bookToDelete.getBookStatus() != BookStatus.RENTED) {
            bookRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Book is rented so it can't be deleted");
        }
    }

    public Book findById(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new DataNotFoundException("book", id.toString()));
    }

    public User findWhoRented(Long bookId) {
        Book book = findById(bookId);
        if (book.getBookStatus().equals(BookStatus.RENTED)) {
            return book.getRent().getUser();
        } else {
            throw new DataNotFoundException("user", bookId.toString());
        }
    }
    
    public LocalDate findWhenReturned(Long bookId) {
        Book book = findById(bookId);
        if (book.getBookStatus().equals(BookStatus.RENTED)) {
            return book.getRent().getDueDate();
        } else {
            throw new DataNotFoundException("This book is not rented");
        }
    }
    
    public Book saveBook(Book bookToSave){
        return bookRepository.save(bookToSave);
    }

}
