# Questions

Here are 2 questions related to the codebase. There's no right or wrong answer - we want to understand your reasoning.

## Question 1: API Specification Approaches

When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded everything directly. 

What are your thoughts on the pros and cons of each approach? Which would you choose and why?

**Answer:**
``` 
The OpenAPI approach for the Warehouse API has several advantages:
- **Consistency**: The API contract is clearly defined in a single source of truth (the YAML file), which can be easily shared and understood by all stakeholders.
- **Automation**: Code generation can speed up development and reduce boilerplate, allowing developers to focus on business logic rather than repetitive coding tasks.
- **Documentation**: The OpenAPI spec can be used to automatically generate API documentation, making it easier for clients to understand how to interact with the API.
However, it also has some drawbacks:
- **Flexibility**: It may be less flexible when it comes to handling complex business logic or custom behaviors that don't fit neatly into the generated code structure.
- **Learning Curve**: Developers may need to learn how to work with the OpenAPI tools and understand the generated code, which can add overhead, especially for smaller projects or teams not familiar with the approach.
On the other hand, hand-coding the Product and Store endpoints allows for more direct control over the implementation and can be quicker for simple endpoints or when the API contract is not expected to change frequently. However, it can lead to inconsistencies in API design and documentation if not managed carefully, and it may require more effort to maintain as the project grows.
In this case, I would choose the OpenAPI approach for the Warehouse API due to its benefits in consistency, automation, and documentation, especially since it seems to be a critical part of the system. For the Product and Store endpoints, I would consider hand-coding them if they are relatively simple and not expected to change often, but I would also evaluate the potential for future growth and complexity before making a final decision.       
```

---

## Question 2: Testing Strategy

Given the need to balance thorough testing with time and resource constraints, how would you prioritize tests for this project? 

Which types of tests (unit, integration, parameterized, etc.) would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```
In prioritizing tests for this project, I would focus on a mix of unit and integration tests to ensure both the correctness of individual components and the overall system behavior.
1. **Unit Tests**: I would prioritize unit tests for the core business logic in the domain layer, as these are critical for ensuring that the fundamental operations of the system work correctly. This includes testing the use cases and any complex logic related to inventory management, store synchronization, and product handling.
2. **Integration Tests**: I would also prioritize integration tests for the REST API endpoints,
especially for the Warehouse API, to ensure that the endpoints correctly interact with the database and other components. This would include testing the full request-response cycle, including validation, error handling, and database interactions.
3. **Parameterized Tests**: For scenarios that involve multiple input combinations, such as the search
and filter functionality for warehouses, I would use parameterized tests to efficiently cover a wide range of cases without duplicating test code.
To ensure effective test coverage over time, I would:
- **Use Code Coverage Tools**: Regularly monitor code coverage metrics to identify untested areas of the codebase and prioritize adding tests for those areas.
- **Adopt a Test-Driven Development (TDD) Approach**: Encourage developers to write tests before implementing new features or fixing bugs, which can help ensure that testing is an integral part of the development process.
- **Continuous Integration**: Set up a CI pipeline that runs all tests on every commit, providing immediate feedback on test results and preventing regressions from being merged into the main branch.
- **Regular Test Reviews**: Periodically review the test suite to ensure that tests remain relevant and effective, and to identify any gaps in coverage as the codebase evolves. This can also help in refactoring tests to improve maintainability and readability over time.      
```
