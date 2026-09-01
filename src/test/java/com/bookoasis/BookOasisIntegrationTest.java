package com.bookoasis;

import com.bookoasis.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookOasisIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void clearTheShelves() {
        bookRepository.deleteAll();
    }

    @Test
    void supportsTheFullLifecycleOfABook() throws Exception {
        // Add a new book
        MvcResult created = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Atomic Habits",
                                  "author": "James Clear",
                                  "publicationYear": 2018
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        String location = created.getResponse().getHeader("Location");

        // Retrieve it back
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Atomic Habits"))
                .andExpect(jsonPath("$.publicationYear").value(2018));

        // Mr Dewey corrects the year
        mockMvc.perform(put(location)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Atomic Habits",
                                  "author": "James Clear",
                                  "publicationYear": 2019
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicationYear").value(2019));

        // The change persisted
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicationYear").value(2019));

        // Remove it
        mockMvc.perform(delete(location))
                .andExpect(status().isNoContent());

        // It is gone
        mockMvc.perform(get(location))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        assertThat(bookRepository.count()).isZero();
    }

    @Test
    void pagesThroughTheStock() throws Exception {
        createBook("Atomic Habits", "James Clear", 2018);
        createBook("Clean Code", "Robert C. Martin", 2008);
        createBook("Design Patterns", "Erich Gamma", 1994);
        createBook("Enterprise Integration Patterns", "Gregor Hohpe", 2003);
        createBook("The Pragmatic Programmer", "Andrew Hunt", 1999);

        // First page of two, sorted by title by default
        mockMvc.perform(get("/api/books?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Atomic Habits"))
                .andExpect(jsonPath("$.content[1].title").value("Clean Code"))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));

        // Last page holds the remaining book
        mockMvc.perform(get("/api/books?page=2&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.last").value(true));

        // Sorting by year, newest first
        mockMvc.perform(get("/api/books?sort=publicationYear,desc&size=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].publicationYear").value(2018));
    }

    private void createBook(String title, String author, int year) throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "author": "%s",
                                  "publicationYear": %d
                                }
                                """.formatted(title, author, year)))
                .andExpect(status().isCreated());
    }

    @Test
    void rejectsAnInvalidBookWithoutStoringIt() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "   ",
                              "author": "James Clear",
                              "publicationYear": 20018
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.length()").value(2));

        assertThat(bookRepository.count()).isZero();
    }
}