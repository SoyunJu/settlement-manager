plugins {
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.owasp.dependencycheck") version "9.0.9"
    java
}

group = "com.settlement"
version = System.getenv("APP_VERSION") ?: "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_17

val jwtVersion: String by project
val bucket4jVersion: String by project
val redissonVersion: String by project
val querydslVersion: String by project
val poiVersion: String by project
val opencsvVersion: String by project
val testcontainersVersion: String by project
val springdocVersion: String by project
val lombokVersion: String by project
val mapstructVersion: String by project
val owaspDependencyCheckVersion: String by project

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("me.paulschwarz:spring-dotenv:4.0.0")

    // PostgreSQL
    runtimeOnly("org.postgresql:postgresql")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:$jwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jwtVersion")

    // Rate Limiting (Phase 2에서 Redis 연동 설정)
    implementation("com.bucket4j:bucket4j-core:$bucket4jVersion")

    // Redisson (분산 락, Phase 8에서 활용)
    implementation("org.redisson:redisson-spring-boot-starter:$redissonVersion")

    // QueryDSL (Jakarta)
    implementation("com.querydsl:querydsl-jpa:$querydslVersion:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:$querydslVersion:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // Excel / CSV (Phase 7 Spring Batch 파일 추출)
    implementation("org.apache.poi:poi-ooxml:$poiVersion")
    implementation("com.opencsv:opencsv:$opencsvVersion")

    // Springdoc (Swagger UI)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")

    // Lombok
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    // MapStruct
    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.batch:spring-batch-test")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
}

// QueryDSL Q클래스 생성 경로
sourceSets {
    main {
        java {
            srcDir("build/generated/sources/annotationProcessor/java/main")
        }
    }
}

dependencyCheck {
    failBuildOnCVSS = 7.0f
    formats = listOf("HTML", "JSON")
    nvd.apiKey = System.getenv("NVD_API_KEY") ?: ""
}

tasks.withType<Test> {
    useJUnitPlatform()
}