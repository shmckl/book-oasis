# The Book Oasis

A RESTful API for managing Mr Dewey's bookshop stock, built with Java and Spring Boot.

## Running it

Requires JDK 21 or later. No database setup needed.

    ./mvnw spring-boot:run

The API is available at `http://localhost:8080`.

Run the tests with:

    ./mvnw test

## Endpoints

| Method | Path | Description | Success | Not found | Invalid |
|---|---|---|---|---|---|
| POST | `/api/books` | Add a new book | 201 + `Location` | | 400 |
| GET | `/api/books/{id}` | Retrieve one book | 200 | 404 | |
| GET | `/api/books` | View all books, paginated | 200 | | |
| PUT | `/api/books/{id}` | Amend a book | 200 | 404 | 400 |
| DELETE | `/api/books/{id}` | Remove a book | 204 | 404 | |

### Pagination

The listing endpoint is paginated so Mr Dewey sees a handful at a time.
It defaults to 10 books per page sorted by title, and page size is capped at 100.

    GET /api/books?page=0&size=5&sort=publicationYear,desc

The response includes the page contents plus `page`, `size`, `totalElements`,
`totalPages`, `first` and `last` so a client can build paging controls.

### Validation

Book requests are validated at the API boundary. Titles and authors must not be
blank and are capped at 255 characters, and the publication year must be present
and between 1450 and 2100 so an obvious typo is caught rather than stored.

### Errors

All errors share one response format. Validation failures list every problem
found, not just the first:

    {
      "timestamp": "2026-08-31T18:04:11.238Z",
      "status": 400,
      "message": "Validation failed",
      "fieldErrors": [
        { "field": "title", "message": "Title is required" },
        { "field": "publicationYear", "message": "Publication year must be 2100 or earlier" }
      ]
    }

A request for a book that does not exist returns the same shape without
`fieldErrors`:

    {
      "timestamp": "2026-08-31T22:10:52.114Z",
      "status": 404,
      "message": "Book not found with id 999"
    }

A malformed or missing request body returns 400 with a generic message rather
than the parser's internal detail.

### Example

    POST /api/books
    Content-Type: application/json

    {
      "title": "Atomic Habits",
      "author": "James Clear",
      "publicationYear": 2018
    }

Returns `201 Created` with a `Location` header of `/api/books/1` and:

    {
      "id": 1,
      "title": "Atomic Habits",
      "author": "James Clear",
      "publicationYear": 2018
    }

`requests.http` in the project root contains runnable examples of every endpoint,
including the failure cases.

## Design decisions

**Layering.** Controller handles HTTP, service holds business logic and the
transaction boundary, repository handles persistence. Each layer only knows
about the one below it.

**DTOs rather than exposing the entity.** `BookRequest` and `BookResponse`
keep the API contract separate from the database schema, so a column change
is not a breaking API change. The request has no `id` because clients do not
choose identifiers.

**`PagedResponse` rather than returning Spring's `Page`.** Spring's page
serialisation is not a stable public contract, so the API returns its own
paging structure instead of leaking framework internals.

**PUT rather than PATCH for the amend.** The client sends the full book and
the server replaces it. PATCH would need rules for distinguishing an absent
field from a null one, which is more complexity than this case needs.

**Validation at the boundary.** Constraints live on the request DTO and are
rejected before reaching the service layer, so business logic can assume its
input is well-formed. The database `nullable = false` constraints remain as a
last line of defence.

**Domain exceptions rather than `Optional` returns.** The service throws
`BookNotFoundException` and a single `@RestControllerAdvice` maps it to a 404,
so the service layer holds no HTTP concerns and every error response shares
one format.

**Constructor injection throughout.** Dependencies are final, the class
cannot be constructed in an invalid state, and services can be unit tested
without a Spring context.

**H2 in-memory database.** No installation required, so the project runs
first time on any machine. Data does not survive a restart, which is fine
for review purposes.

## Testing

- `BookRepositoryTest` uses `@DataJpaTest` to verify the JPA mapping against a real database.
- `BookServiceTest` uses Mockito to cover the business logic, including the not-found paths.
- `BookControllerTest` uses `@WebMvcTest` to verify status codes, headers, JSON shape
  and that invalid requests never reach the service.

## What I would do next

Given more time, in this order:

1. **A full integration test** with `@SpringBootTest` running a create, read,
   update and delete cycle through the whole stack, to prove the layers work
   together rather than only in isolation.
2. **Flyway migrations** instead of `hibernate.ddl-auto`, so schema changes
   are versioned and reviewable.
3. **Search and filtering** on the listing endpoint, by author or title, which
   is the natural next thing a bookshop owner asks for.
4. **A persistent database** and container packaging for real deployment.