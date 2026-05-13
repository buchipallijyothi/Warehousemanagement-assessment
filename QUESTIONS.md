# Questions

Here are 2 questions related to the codebase. There's no right or wrong answer - we want to understand your reasoning.

## Question 1: API Specification Approaches

When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded everything directly. 

What are your thoughts on the pros and cons of each approach? Which would you choose and why?

**Answer:**
``` [...] with your specific project experience:Answer: API Specification Approaches Analysis1. Design-First Approach (Used in Warehouse API)In this approach, we defined the OpenAPI YAML contract first and used code generation tools to create our server stubs and data models.Pros:Strict Contract Enforcement: Serves as a single source of truth, ensuring consistent error responses and naming conventions.Parallel Workstreams: Frontend or integration consumers can mock the API endpoints immediately based on the YAML file without waiting for backend logic.Reduced Human Error: Eliminates manual data-mapping boilerplate code, reducing typos in fields.Cons:Development Friction: Modifying a field requires updating the YAML file and running a project rebuild, which slows down rapid iterations.Tooling Overhead: Requires configuring build plugins (like Maven/Gradle openapi-generator) which can introduce configuration complexity.2. Code-First Approach (Used in Product and Store APIs)In this approach, we directly implemented the Java controllers and domain entities using native framework abstractions.Pros:High Velocity: Allows for rapid prototyping and immediate code execution, which is crucial under strict time constraints.IDE Refactoring Friendliness: Standard IDE refactoring tools work seamlessly across all files without dealing with generated code boundaries.Cons:Documentation Drift: The implementation details can quickly diverge from any external documentation if changes are not manually updated.Inconsistency Risk: Opens the door for styling variances (e.g., naming patterns, HTTP response code logic) between different endpoints.My Recommendation and JustificationFor the context of this Hackathon Assessment, the Code-First approach used for the Product and Store endpoints is the superior choice.Why: Hackathons prioritize velocity, working features, and the ability to pivot requirements rapidly. Code-First completely removes build-tooling friction and allows us to deliver fully functional endpoints faster. We can easily mitigate the lack of documentation by integrating runtime documentation tools like Springdoc OpenAPI / Swagger UI to automatically generate interactive docs straight from our code without slowing down our development speed.However, if this project were transitioning to a large-scale corporate production system with multiple distributed teams, I would standardise on the Design-First approach to safeguard cross-team integration contracts.

```

---

## Question 2: Testing Strategy

Given the need to balance thorough testing with time and resource constraints, how would you prioritize tests for this project? 

Which types of tests (unit, integration, parameterized, etc.) would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt

```
