package pragmatech.digital.workshops.lab2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pragmatech.digital.workshops.lab2.service.BookService;

@Controller
public class BookCatalogController {

  private final BookService bookService;

  public BookCatalogController(BookService bookService) {
    this.bookService = bookService;
  }

  @GetMapping("/books")
  public String showBookCatalog(Model model) {
    model.addAttribute("books", bookService.getAllBooks());
    return "books";
  }
}
