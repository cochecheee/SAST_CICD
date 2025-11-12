function loadNotifications(url, token) {
    fetch(url, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(data => {
            // Kiểm tra nếu có bài đăng
            if (data.responseCode === 1000 && Array.isArray(data.result) && data.result.length > 0) {
                data.result.forEach(notification => {
                    const postElement = createNotificationHtml(notification);
                    notificationContainer.insertAdjacentHTML('beforeend', postElement);
                });
            } else {
                // console.error('Không có bài đăng hoặc lỗi API.');
                // Nếu không có dữ liệu, loại bỏ event listener
                showEndMessage(); // Hiển thị thông báo "This is the end..."
                removeLoadMoreButton();
            }
        })
        .catch(error => console.error('Error fetching posts:', error));
}

function removeLoadMoreButton() {
    const loadMoreButton = document.getElementById('load-more');
    if (loadMoreButton) {
        loadMoreButton.remove();
    }
}

// Gán sự kiện nhấn nút "See more notifications"
function setupLoadMoreButton(url, token) {
    const loadMoreButton = document.getElementById('load-more');
    if (loadMoreButton) {
        loadMoreButton.addEventListener('click', () => {
            currentIndex += 10;
            const newUrl = `${url}?index=${currentIndex}`;
            loadNotifications(newUrl, token);
        });
    }
}

// Thêm event listener khi trang được load
function startLoad(url) {
    const token = localStorage.getItem('jwt');
    document.addEventListener('DOMContentLoaded', () => {
        currentIndex = 0; // Bắt đầu từ index 0
        const initialUrl = `${url}?index=${currentIndex}`;
        loadNotifications(initialUrl, token);
        setupLoadMoreButton(url, token);
    });
}

function showEndMessage() {
    const endMessage = document.createElement('div');
    endMessage.id = 'endMessage';
    endMessage.style.textAlign = 'center';
    endMessage.style.padding = '20px';
    endMessage.style.fontSize = '18px';
    endMessage.style.color = '#888';
    endMessage.style.fontWeight = 'bold';
    endMessage.style.borderTop = '1px solid #ccc';
    endMessage.style.marginTop = '20px';

    endMessage.innerHTML = `
        <div>
            <p>There are no more notifications...</p>
        </div>
    `;
    notificationContainer.appendChild(endMessage);
}

function createNotificationHtml(notification) {
    return `
            <a href="/posts/${notification.post.postId}" class="list-group-item notification-card d-flex justify-content-between align-items-center">
                <div class="d-flex align-items-center">
                    <div class="status-indicator bg-cyan-500 rounded-circle me-3"
                         style="width: 10px; height: 10px;"></div>
                    <div class="avatar me-3">
                        <img src="${notification.triggerUser.avatarUrl ? notification.triggerUser.avatarUrl : '/images/unknown_user.jpg'}" alt="User Avatar">
                    </div>
                    <div>
                        <h5><strong>@${notification.triggerUser.username}</strong> ${notification.notificationType === "LIKE" ? "liked your post" : "commented on your post"} ${notification.post.caption}</strong></h5>
                        <p class="text-muted">${formatDateFromString(notification.createdAt)}</p>
                    </div>
                </div>
                <button class="btn mark-read-btn" data-bs-toggle="tooltip" data-bs-placement="top" title="Mark as read">
                    <i class="bi bi-envelope-check"></i>
                </button>
            </a>`;
}

// Tải posts khi trang được load
// document.addEventListener('DOMContentLoaded', loadPosts);