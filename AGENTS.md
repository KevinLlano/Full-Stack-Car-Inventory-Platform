# AGENTS.md

## Project snapshot
- Spring Boot 2.6.6 / Java 17 inventory app with a server-rendered Thymeleaf UI in `src/main/resources/templates`.
- Core domain: `Product` ↔ `Part` is many-to-many through `product_part`; `Part` uses `SINGLE_TABLE` inheritance with `InhousePart` and `OutsourcedPart`.
- Security is JWT-based via Keycloak: `SecurityConfig` protects all routes except `/public/**`, and `TestController` exposes the reference secured endpoint at `/api/secure`.

## Code structure to follow
- Controllers live in `src/main/java/com/example/demo/controllers`.
  - `MainScreenControllerr` handles `/`, `/mainscreen`, and CSV export at `/report`.
  - `AddProductController` and `AddPartController` manage create/update/delete flows and association state.
- Services are thin wrappers over repositories and keep the search convention: `listAll(keyword)` delegates to `search(keyword)` in the repository.
- Repositories are `CrudRepository`-based with JPQL search methods like `@Query("SELECT p FROM Product p WHERE p.name LIKE %?1%")`.
- Validation is implemented with custom class-level annotations: `@ValidProductPrice`, `@ValidEnufParts`, and `@ValidDeletePart`.

## Important patterns
- When associating or removing parts/products, update both sides of the relationship (`Product.parts` and `Part.products`) before saving.
- Keep the domain rules intact: parts cannot be deleted while linked to products, and product updates validate price vs. part total and available inventory.
- Controllers here often fetch `*ServiceImpl` beans from `ApplicationContext`; preserve that pattern unless you are intentionally refactoring the whole flow.

## Build, run, and test
- Local run: `mvn spring-boot:run`
- Package/build: `mvn clean package -DskipTests`
- Tests: `mvn test`
- CodeBuild uses `buildspec.yml` with Java 17 (`corretto17`) and Maven cache at `/root/.m2/**/*`.

## AWS / deployment assets
- Active CI/CD assets are `cloudformation-infrastructure.yml`, `cloudformation-pipeline.yml`, and `buildspec.yml`.
- `src/terraform/*` appears to be legacy deployment infrastructure; check it before changing, but prefer the CloudFormation path for new AWS work.

## Repo-specific quirks
- Several names are intentionally misspelled or historically awkward: `MainScreenControllerr`, `testValidDeletePart.java`, and `src/test/java/com/example/demo/MainScreenControllerr/java/`.
- `src/main/resources/static/index.html` redirects to `/mainscreen`; the Thymeleaf main screen is the real homepage.
- `README.md` mentions a separate React frontend, but this workspace only contains the Spring Boot backend—treat the backend sources as the source of truth.
- Keep the existing package layout and filenames consistent unless you are doing a deliberate cross-project rename.

## AI Integration and Enterprise Considerations (New)
- **Spring AI**: The project runs on Spring Boot 2.6.6. Integrating modern Spring AI (Bedrock/pgvector) will require upgrading to Spring Boot 3.x (and migrating `javax` to `jakarta`).
- **Infrastructure Path**: Despite legacy `src/terraform/` directories, any new AWS infrastructure or IAM roles for AI must be added to the active `cloudformation-infrastructure.yml` and `cloudformation-pipeline.yml` files. DO NOT USE TERRAFORM.
- **Cost Minimization**: All agent implementations should default to a "Local First" approach for datastores (e.g., Docker `ankane/pgvector`) while tunneling to cloud-priced LLM APIs (Bedrock) using local AWS CLI profiles to avoid idle cloud resource costs.
- **Testing AI**: Ensure any newly introduced AI logic uses appropriate interface mocking for the Bedrock client to ensure `mvn test` remains fast and not dependent on live cloud API calls.
