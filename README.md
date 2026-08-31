# The Book Oasis

A RESTful API for managing Mr Dewey's bookshop stock, built with Java and Spring Boot.

## Running it

Requires JDK 21 or later. No database setup needed.

    ./mvnw spring-boot:run

The API is available at `http://localhost:8080`.

Run the tests with:

    ./mvnw test

## Endpoints

| Method | Path | Description | Success | Not found |
|---|---|---|---|---|
| POST | `/api/books` | Add a new book | 201 + `Location` | |
| GET | `/api/books/{id}` | Retrieve one book | 200 | 404 |
| GET | `/api/books` | View all books, paginated | 200 | |
| PUT | `/api/books/{id}` | Amend a book | 200 | 404 |
| DELETE | `/api/books/{id}` | Remove a book | 204 | 404 |

### Pagination

The listing endpoint is paginated so Mr Dewey sees a handful at a time.
It defaults to 10 books per page sorted by title, and page size is capped at 100.

    GET /api/books?page=0&size=5&sort=publicationYear,desc

The response includes the page contents plus `page`, `size`, `totalElements`,
`totalPages`, `first` and `last` so a client can build paging controls.

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

`requests.http` in the project root contains runnable examples of every endpoint.

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

**Constructor injection throughout.** Dependencies are final, the class
cannot be constructed in an invalid state, and services can be unit tested
without a Spring context.

**H2 in-memory database.** No installation required, so the project runs
first time on any machine. Data does not survive a restart, which is fine
for review purposes.

## Testing

- `BookRepositoryTest` uses `@DataJpaTest` to verify the JPA mapping against a real database.
- `BookServiceTest` uses Mockito to cover the business logic, including the not-found paths.

## What I would do next

Given more time, in this order:

1. **Request validation.** Reject blank titles and implausible publication
   years at the API boundary with a clear 400 response.
2. **Consistent error responses.** A `@RestControllerAdvice` returning a
   structured error body rather than empty 404s.
3. **Controller tests** with `@WebMvcTest` to verify status codes, headers
   and JSON shape.
4. **Flyway migrations** instead of `hibernate.ddl-auto`, so schema changes
   are versioned and reviewable.
5. **A persistent database** and container packaging for real deployment.