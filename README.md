# Social Network Application - Spring Boot & Thymeleaf

Ứng dụng mạng xã hội được xây dựng bằng Spring Boot và Thymeleaf, được thiết kế cho mục đích nghiên cứu và phân tích bảo mật với tích hợp SAST CI/CD.

![Application Architecture](docs/images/architecture.png)
*Kiến trúc tổng quan của ứng dụng*

## Tính năng

- Xác thực và phân quyền người dùng
- Các chức năng mạng xã hội
- Thông báo qua email
- Tải lên tệp tin
- RESTful API endpoints
- Tích hợp SAST (Static Application Security Testing)

## Công nghệ sử dụng

- **Backend**: Spring Boot 2.7.18
- **View Template**: Thymeleaf
- **Database**: MariaDB/MySQL
- **Security**: Spring Security
- **Authentication**: JWT (JSON Web Token)
- **Build Tool**: Gradle 8.7
- **Java Version**: 17
- **Logging**: Log4j2 2.14.1
- **Security Scanning**: SonarQube scanner, OWASP Dependency Check, CodeQL, Trivy

## Yêu cầu hệ thống

- Java 17 hoặc cao hơn
- Gradle 8.7 hoặc cao hơn
- MariaDB/MySQL database
- Docker & Docker Compose (tùy chọn)

## Cài đặt

### Sử dụng Gradle build project

1. Clone repository:
```bash
git clone <repository-url>
cd ALOUTE_Spring_Thymeleaf_RCE
```

2. Cấu hình database trong `src/main/resources/application.yaml`

3. Build project:
```bash
./gradlew clean build
```

4. Chạy ứng dụng:
```bash
./gradlew bootRun
```

Ứng dụng sẽ chạy tại: `http://localhost:8080`

### Sử dụng Docker build SonarQube

1. Build và chạy với Docker Compose:
```bash
cd path_to_docker_compose
docker-compose up -d
```

2. Truy cập ứng dụng:
- SonarQube: `http://localhost:9000`

## Cấu hình

Ứng dụng sử dụng Spring profiles cho các môi trường khác nhau:

- **dev**: Profile development (mặc định)
- **prod**: Profile production

### Biến môi trường

| Biến | Mô tả | Giá trị mặc định |
|------|-------|------------------|
| `SPRING_PROFILES_ACTIVE` | Spring profile đang hoạt động | dev |
| `DBMS_CONNECTION` | URL kết nối database | jdbc:mysql://localhost:3306/social_network_v1 |
| `MYSQL_USERNAME` | Username database | root |
| `MYSQL_ROOT_PASSWORD` | Password database | (trống) |
| `JWT_TOKEN` | JWT secret key | dev-secret-key-for-testing-only |
| `S3_ACCESS_KEY` | AWS S3 access key | (tùy chọn) |
...

## Phân tích bảo mật

Project này bao gồm nhiều công cụ phân tích bảo mật:

![Security Tools Overview](docs/images/security-tools-overview.png)
*Tổng quan các công cụ bảo mật được sử dụng*

### SonarQube

**Chức năng**: 
- Phân tích chất lượng code và phát hiện lỗ hổng bảo mật
- Quét code smells, bugs, vulnerabilities, security hotspots
- Đo lường code coverage, duplications, maintainability
- Cung cấp dashboard trực quan để theo dõi technical debt

**Cách chạy**:

1. Khởi động SonarQube server với Docker:
```bash
docker-compose up -d
```

2. Chạy phân tích (sau khi SonarQube server đã sẵn sàng):
```bash
./gradlew sonarqube -Dsonar.host.url=http://localhost:9000 -Dsonar.login=<your-token>
```

3. Hoặc cấu hình trong `build.gradle` và chạy:
```bash
./gradlew sonarqube
```

**Output**:
- **Console**: Hiển thị tiến trình quét và URL để xem kết quả
  ```
  ANALYSIS SUCCESSFUL
  You can browse http://localhost:9000/dashboard?id=com.nhoclahola%3Asocial-network-v1
  ```
- **Web Dashboard** (http://localhost:9000):
  - Overview: Tổng quan về bugs, vulnerabilities, code smells
  - Issues: Danh sách chi tiết các vấn đề theo độ nghiêm trọng (Blocker, Critical, Major, Minor, Info)
  - Security Hotspots: Các điểm cần review về bảo mật
  - Measures: Các metrics chi tiết (coverage, duplications, complexity)
  - Code: Xem code với annotations về issues

![SonarQube Dashboard](docs/images/sonarqube-dashboard.png)
*SonarQube Dashboard hiển thị kết quả phân tích*

### OWASP Dependency Check

**Chức năng**:
- Quét các dependencies (thư viện bên thứ 3) để phát hiện lỗ hổng đã biết (CVEs)
- Kiểm tra với National Vulnerability Database (NVD)
- Phát hiện các phiên bản thư viện có lỗ hổng bảo mật
- Tạo báo cáo chi tiết với CVSS scores và mức độ nghiêm trọng

**Cách chạy**:
```bash
./gradlew dependencyCheckAnalyze
```

Với cấu hình bổ sung:
```bash
./gradlew dependencyCheckAnalyze --info
```

**Output**:
- **Console**: 
  ```
  Checking for updates
  Analyzing dependencies...
  Analysis complete
  ```
- **HTML Report**: `build/reports/dependency-check-report.html`
  - Summary: Tổng số dependencies và số lượng vulnerabilities
  - Vulnerability Details:
    - CVE ID (ví dụ: CVE-2021-44228)
    - Severity (Critical, High, Medium, Low)
    - CVSS Score
    - Description của lỗ hổng
    - Affected versions
    - References và links đến thông tin chi tiết
- **JSON/XML Report**: Có thể cấu hình để xuất thêm các định dạng khác
- **Suppression File**: `dependency-check-suppressions.xml` để bỏ qua false positives

**Ví dụ kết quả**:
```
Found 15 vulnerabilities in 45 dependencies
- Critical: 2 (Log4j2 RCE CVE-2021-44228)
- High: 5
- Medium: 6
- Low: 2
```

![OWASP Dependency Check Report](docs/images/dependency-check-report.png)
*OWASP Dependency Check HTML Report*

### CodeQL

**Chức năng**:
- Static analysis engine của GitHub để phát hiện security vulnerabilities
- Phân tích semantic code (không chỉ pattern matching)
- Phát hiện các lỗi như: SQL Injection, XSS, Path Traversal, Command Injection
- Hỗ trợ custom queries để tìm các vấn đề cụ thể
- Tích hợp sẵn trong GitHub Actions

**Cách chạy**:

1. **Trên GitHub Actions** (tự động):
   - Cấu hình trong `.github/workflows/codeql.yml`
   - Tự động chạy khi push/PR

2. **Local với CodeQL CLI**:
```bash
# Tạo CodeQL database
codeql database create codeql-db --language=java --command="./gradlew clean build -x test"

# Chạy analysis
codeql database analyze codeql-db --format=sarif-latest --output=results.sarif

# Hoặc với custom queries
codeql database analyze codeql-db java-security-and-quality.qls --format=sarif-latest --output=results.sarif
```

**Output**:
- **GitHub Security Tab**: Hiển thị alerts trực tiếp trên repository
  - Security Overview dashboard
  - Alerts được nhóm theo severity và category
  - Code scanning alerts với location chính xác

![CodeQL Security Alerts](docs/images/codeql-alerts.png)
*CodeQL alerts trong GitHub Security Tab*
  
- **SARIF File** (`results.sarif`):
  ```json
  {
    "runs": [{
      "results": [{
        "ruleId": "java/sql-injection",
        "level": "error",
        "message": "SQL injection vulnerability",
        "locations": [{
          "physicalLocation": {
            "artifactLocation": {"uri": "src/main/java/..."},
            "region": {"startLine": 45}
          }
        }]
      }]
    }]
  }
  ```

- **Console output**:
  ```
  Running queries...
  Interpreting results...
  Found 12 results:
  - SQL Injection: 3 instances
  - Cross-site Scripting (XSS): 2 instances
  - Insecure Randomness: 1 instance
  - Log Injection: 6 instances
  ```

**Chuyển đổi sang SonarQube format**:
```bash
python scripts/convert_codeql_to_sonar.py results.sarif sonar-results.json
```

### Trivy

**Chức năng**:
- Container image vulnerability scanner
- Quét OS packages và application dependencies
- Phát hiện misconfigurations trong IaC (Infrastructure as Code)
- Quét filesystem và git repositories
- Hỗ trợ nhiều ngôn ngữ và package managers

**Cách chạy**:

1. **Quét Docker image**:
```bash
trivy image social-network-v1:latest
```

2. **Quét filesystem/project**:
```bash
trivy fs .
```

3. **Quét với output format cụ thể**:
```bash
trivy image --format json --output results.json social-network-v1:latest
```

4. **Quét chỉ HIGH và CRITICAL**:
```bash
trivy image --severity HIGH,CRITICAL social-network-v1:latest
```

**Output**:

- **Console (Table format)**:
  ```
  social-network-v1:latest (alpine 3.18.0)
  
  Total: 23 (HIGH: 8, CRITICAL: 2)
  
  ┌─────────────────┬────────────────┬──────────┬───────────────────┬───────────────┐
  │    Library      │ Vulnerability  │ Severity │ Installed Version │ Fixed Version │
  ├─────────────────┼────────────────┼──────────┼───────────────────┼───────────────┤
  │ log4j-core      │ CVE-2021-44228 │ CRITICAL │ 2.14.1            │ 2.17.1        │
  │ spring-core     │ CVE-2022-22965 │ HIGH     │ 5.3.18            │ 5.3.20        │
  └─────────────────┴────────────────┴──────────┴───────────────────┴───────────────┘
  ```

- **JSON Output** (`results.json`):
  ```json
  {
    "Results": [{
      "Target": "Java",
      "Vulnerabilities": [{
        "VulnerabilityID": "CVE-2021-44228",
        "PkgName": "org.apache.logging.log4j:log4j-core",
        "InstalledVersion": "2.14.1",
        "FixedVersion": "2.17.1",
        "Severity": "CRITICAL",
        "Title": "Remote code execution in Log4j",
        "Description": "Apache Log4j2 JNDI features...",
        "References": ["https://nvd.nist.gov/vuln/detail/CVE-2021-44228"]
      }]
    }]
  }
  ```

- **Summary**:
  ```
  Total: 23 vulnerabilities
  - CRITICAL: 2
  - HIGH: 8
  - MEDIUM: 10
  - LOW: 3
  ```

![Trivy Scan Results](docs/images/trivy-results.png)
*Trivy scan results cho Docker image*

**Tích hợp CI/CD**:
```bash
# Fail build nếu có CRITICAL vulnerabilities
trivy image --exit-code 1 --severity CRITICAL social-network-v1:latest
```

## CI/CD Pipeline

Project sử dụng GitHub Actions để tự động chạy các công cụ phân tích bảo mật. File workflow: `.github/workflows/ci.yml`

### Quy trình CI/CD

Pipeline được trigger khi:
- Push code lên branch `main`
- Tạo Pull Request vào branch `main`

### Các bước trong Pipeline

#### 1. **Setup & Checkout**
```yaml
- Checkout code với full history (fetch-depth: 0)
- Setup JDK 17
- Cache Gradle dependencies
- Grant execute permission cho gradlew
```

#### 2. **CodeQL Analysis**
```yaml
Bước 1: Initialize CodeQL
  - Khởi tạo CodeQL database
  - Ngôn ngữ: Java

Bước 2: Build for CodeQL
  - ./gradlew build -x test --no-daemon

Bước 3: Perform CodeQL Analysis
  - Phân tích code với CodeQL queries
  - Xuất kết quả SARIF format

Bước 4: Extract JSON from SARIF
  - Chuyển đổi SARIF sang JSON
  - Tạo file: codeql-results.json

Bước 5: Convert to Sonar format
  - Script: scripts/convert_codeql_to_sonar.py
  - Output: sonar-issues.json (để import vào SonarQube)

Bước 6: Upload artifacts
  - codeql-results.json
  - Retention: 30 days
```

**Output tại bước này**:
- CodeQL results được hiển thị trong **GitHub Security Tab**
- SARIF file chứa tất cả vulnerabilities tìm được
- JSON file để tích hợp với SonarQube

#### 3. **Build & Test**
```yaml
- ./gradlew clean build --no-daemon -x test
- Continue-on-error: true (không fail pipeline nếu build lỗi)
```

#### 4. **OWASP Dependency-Check**
```yaml
Bước 1: Cache Dependency-Check Data
  - Cache NVD database (~/.gradle/dependency-check-data)
  - Key: OS + build.gradle hash

Bước 2: Debug NVD API Key
  - Verify NVD_API_KEY secret exists

Bước 3: Run OWASP Dependency-Check
  - Command: ./gradlew dependencyCheckAnalyze
  - Options:
    * --no-daemon
    * --info (verbose logging)
    * -Dnvd.api.key="${NVD_API_KEY}"
    * -Dformats=HTML,JSON,XML
  - Output: build/reports/dependency-check-report.*

Bước 4: Verify Reports
  - Check tất cả format: HTML, JSON, XML
  - Display file sizes

Bước 5: Upload artifacts
  - All report formats
  - Retention: 30 days
```

**Output tại bước này**:
- **HTML Report**: Xem trực quan trong browser
- **JSON Report**: Để tích hợp với SonarQube
- **XML Report**: Format chuẩn cho các tools khác

#### 5. **SonarQube Scan**
```yaml
Bước 1: Cache SonarQube packages
  - ~/.sonar/cache

Bước 2: Verify SonarQube Connection
  - Test connection với SONAR_HOST_URL
  - Verify SONAR_TOKEN
  - Check API endpoint: /api/system/status

Bước 3: SonarQube Scan với tất cả reports
  - Command: ./gradlew sonar
  - Parameters:
    * -Dsonar.projectKey=sast-social
    * -Dsonar.host.url=${SONAR_URL}
    * -Dsonar.token=${SONAR_TOKEN}
    * -Dsonar.sources=src/main/java
    * -Dsonar.java.binaries=build/classes/java/main
  
  - Import Dependency-Check results:
    * -Dsonar.dependencyCheck.htmlReportPath=...
    * -Dsonar.dependencyCheck.jsonReportPath=...
    * -Dsonar.dependencyCheck.xmlReportPath=...
  
  - Import CodeQL results:
    * -Dsonar.externalIssuesReportPaths=sonar-issues.json

Bước 4: Upload SonarQube Results
  - sonar-issues.json
  - build/sonar/report-task.txt

Bước 5: Check Quality Gate
  - Action: sonarsource/sonarqube-quality-gate-action
  - Timeout: 300 seconds
  - Status: PASSED/FAILED

Bước 6: Display Results
  - Quality Gate status
  - Dashboard URL
  - Troubleshooting tips
```

**Output tại bước này**:
- **Console**: Analysis progress và dashboard URL
- **SonarQube Dashboard**: 
  - Overview metrics (bugs, vulnerabilities, code smells)
  - Dependency-Check vulnerabilities
  - CodeQL issues
  - Quality Gate status
- **Artifacts**: report-task.txt với metadata

#### 6. **Docker Build & Trivy Scan**
```yaml
Bước 1: Build Docker Image
  - docker build -t ${DOCKER_USERNAME}/aloute-spring:latest .

Bước 2: Scan with Trivy (SARIF format)
  - Action: aquasecurity/trivy-action@master
  - Format: sarif
  - Output: trivy-results.sarif
  - Severity: CRITICAL,HIGH
  - Ignore unfixed: true

Bước 3: Scan with Trivy (JSON format)
  - Format: json
  - Output: trivy-results.json
  - Severity: ALL levels

Bước 4: Scan with Trivy (Table format)
  - Format: table
  - Output: trivy-results.txt
  - Human-readable format

Bước 5: Upload Trivy Results
  - All formats (SARIF, JSON, Table)
  - Retention: 30 days

Bước 6: Login to Docker Hub
  - Username: DOCKER_USERNAME secret
  - Password: DOCKER_PASSWORD secret

Bước 7: Push Docker Image
  - docker push ${DOCKER_USERNAME}/aloute-spring:latest
```

**Output tại bước này**:
- **SARIF**: Để import vào GitHub Security
- **JSON**: Structured data cho automation
- **Table**: Human-readable vulnerability list
- **Docker Hub**: Image được push lên registry

#### 7. **Upload All Security Reports**
```yaml
- Combine tất cả artifacts:
  * CodeQL results
  * Dependency-Check reports
  * SonarQube results
  * Trivy scan results
- Single artifact: all-security-reports-{run_number}
- Retention: 30 days
```

#### 8. **Final Status Summary**
```yaml
- Display summary của tất cả security scans:
  * CodeQL: PASSED/FAILED
  * SonarQube Quality Gate: PASSED/FAILED/SKIPPED
  * Trivy Scan: PASSED/FAILED
- List tất cả artifacts đã upload
```

### Cấu hình Secrets

Các secrets cần thiết trong GitHub repository:

| Secret Name | Mô tả | Required |
|-------------|-------|----------|
| `SONAR_TOKEN` | Authentication token cho SonarQube | Yes |
| `SONAR_HOST_URL` | URL của SonarQube server | Yes |
| `NVD_API_KEY` | API key cho National Vulnerability Database | Yes |
| `DOCKER_USERNAME` | Docker Hub username | Yes |
| `DOCKER_PASSWORD` | Docker Hub password/token | Yes |

### Xem kết quả Pipeline

1. **GitHub Actions Tab**:
   - Workflow runs history
   - Logs của từng step
   - Artifacts download

2. **GitHub Security Tab**:
   - CodeQL alerts
   - Dependency alerts
   - Security overview

3. **SonarQube Dashboard**:
   - URL: `${SONAR_HOST_URL}/dashboard?id=sast-social`
   - Tổng hợp tất cả issues từ các tools

4. **Artifacts** (download từ workflow run):
   - `codeql-json-results-{run_number}`
   - `dependency-check-reports-{run_number}`
   - `sonarqube-results-{run_number}`
   - `trivy-scan-results-{run_number}`
   - `all-security-reports-{run_number}` (combined)

### Flow Diagram

```
Push/PR → Setup Environment
           ↓
       CodeQL Scan → Upload to GitHub Security
           ↓
       Build Project
           ↓
   OWASP Dependency-Check → Generate Reports (HTML/JSON/XML)
           ↓
     SonarQube Scan ← Import Dependency-Check + CodeQL results
           ↓
    Quality Gate Check
           ↓
    Docker Build → Trivy Scan → Upload to Docker Hub
           ↓
   Upload All Artifacts
           ↓
    Display Summary
```

![CI/CD Pipeline Flow](docs/images/cicd-pipeline-flow.png)
*CI/CD Pipeline workflow chi tiết*

![GitHub Actions Workflow](docs/images/github-actions-workflow.png)
*GitHub Actions workflow execution*

### Troubleshooting CI/CD

**SonarQube không nhận được Dependency-Check results**:
```bash
1. Kiểm tra Dependency-Check plugin đã install trong SonarQube
2. Vào Administration > Marketplace > search "Dependency-Check"
3. Install plugin và restart SonarQube server
4. Re-run pipeline
```

**NVD API rate limit**:
```bash
1. Đăng ký NVD API key tại: https://nvd.nist.gov/developers/request-an-api-key
2. Add vào GitHub Secrets với tên NVD_API_KEY
3. Re-run pipeline (sẽ nhanh hơn và không bị rate limit)
```

**CodeQL analysis failed**:
```bash
1. Check build logs có error không
2. Ensure Java 17 đang được sử dụng
3. Verify Gradle build success trước khi CodeQL analyze
```

**Quality Gate failed**:
```bash
1. Xem chi tiết issues trong SonarQube dashboard
2. Fix các Critical/High severity issues
3. Commit và push lại để trigger pipeline
```

## Cấu trúc Project

```
src/
├── main/
│   ├── java/
│   │   └── com/nhoclahola/socialnetworkv1/
│   │       ├── configuration/      # Cấu hình Spring
│   │       ├── controller/         # REST Controllers
│   │       ├── dto/                # Data Transfer Objects
│   │       ├── entity/             # JPA Entities
│   │       ├── exception/          # Exception Handlers
│   │       ├── mapper/             # MapStruct Mappers
│   │       ├── repository/         # JPA Repositories
│   │       ├── security/           # Security Configuration
│   │       └── service/            # Business Logic
│   └── resources/
│       ├── application.yaml        # Cấu hình chính
│       ├── log4j2.xml             # Cấu hình logging
│       ├── static/                 # CSS, JS, Images
│       │   ├── css/               # Bootstrap & custom CSS
│       │   ├── js/                # JavaScript files
│       │   └── images/            # Static images
│       └── templates/              # Thymeleaf templates
│           ├── admin/             # Admin pages
│           ├── auth/              # Authentication pages
│           ├── user/              # User pages
│           └── fragments/         # Reusable fragments
└── test/
    └── java/                       # Unit & Integration tests
```

## Dependencies chính
### Spring Boot Dependencies
- Spring Boot Starter Web
- Spring Boot Starter Thymeleaf
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- Spring Boot Starter Mail
- Spring Boot Starter Validation

### Database
- MariaDB Java Client 3.0.11

### Security & Authentication
- Spring Security
- JWT (io.jsonwebtoken:jjwt 0.9.1)

### Logging
- Log4j2 API 2.14.1
- Log4j2 Core 2.14.1
- Log4j2 SLF4J Implementation 2.14.1

### Utilities
- Lombok
- MapStruct 1.5.5.Final

## Công cụ Build & CI/CD

- Gradle 8.7+
- SonarQube 25.10
- OWASP Dependency Check 12.1.9
- Docker & Docker Compose

## Scripts

Project bao gồm các scripts hỗ trợ:

- `gradlew` / `gradlew.bat` - Gradle wrapper
- `convert-imports.bat` - Script chuyển đổi imports
- `convert-jakarta-to-javax.ps1` - Chuyển đổi Jakarta sang Javax
- `scripts/convert_codeql_to_sonar.py` - Chuyển đổi CodeQL sang SonarQube format

## Gradle Tasks

Các Gradle tasks phổ biến:

```bash
# Build project
./gradlew build

# Run application
./gradlew bootRun

# Run tests
./gradlew test

# Clean build
./gradlew clean

# SonarQube analysis
./gradlew sonarqube

# OWASP Dependency Check
./gradlew dependencyCheckAnalyze

# Build Docker image
./gradlew bootBuildImage
```

## Troubleshooting

### Database Connection Issues
- Kiểm tra MySQL/MariaDB đã chạy
- Xác minh thông tin kết nối trong `application.yaml`
- Đảm bảo database `social_network_v1` đã được tạo

### Build Issues
- Xóa thư mục `build/` và rebuild
- Kiểm tra Java version: `java -version`
- Xóa Gradle cache: `./gradlew clean`

### Docker Issues
- Kiểm tra Docker đã chạy: `docker ps`
- Rebuild containers: `docker-compose down && docker-compose up -d --build`

## Disclaimer

⚠️ **QUAN TRỌNG**: Ứng dụng này chỉ được sử dụng cho mục đích giáo dục và nghiên cứu bảo mật. 

- Ứng dụng cố ý chứa các lỗ hổng bảo mật để minh họa các vấn đề bảo mật thường gặp
- KHÔNG sử dụng code này trong môi trường production
- KHÔNG triển khai trên các server công khai
- Người bảo trì không chịu trách nhiệm về bất kỳ hành vi lạm dụng nào của phần mềm này
- Chỉ sử dụng trong môi trường isolated/sandbox cho mục đích học tập và kiểm thử
