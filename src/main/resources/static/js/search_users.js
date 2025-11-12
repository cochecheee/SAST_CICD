function loadUsers(url, token) {
    fetch(url, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(data => {
            if (data.responseCode === 1000 && Array.isArray(data.result) && data.result.length > 0) {
                const titlePeople = document.getElementById('title-people');
                titlePeople.style.display = 'block';
                data.result.forEach(user => {
                    const userElement = createPostHtmlUsers(user);
                    userContainer.insertAdjacentHTML('beforeend', userElement);
                });
            }
            if (data.result.length < 5) {
                showEndMessageUsers();
                removeLoadMoreButtonUsers();
            }
        })
        .catch(error => console.error('Error fetching posts:', error));
}

function removeLoadMoreButtonUsers() {
    const loadMoreButton = document.getElementById('load-more-people');
    if (loadMoreButton) {
        loadMoreButton.remove();
    }
}

// Gán sự kiện nhấn nút "See more notifications"
function setupLoadMoreButtonUsers(url, token, query) {
    const loadMoreButton = document.getElementById('load-more-people');
    if (loadMoreButton) {
        loadMoreButton.style.display = 'block'
        loadMoreButton.addEventListener('click', () => {
            currentIndex += 5;
            const newUrl = `${url}?query=${query}&index=${currentIndex}`;
            loadUsers(newUrl, token);
        });
    }
}

// Thêm event listener khi trang được load
function startLoadUsers(url, query) {
    const token = localStorage.getItem('jwt');
    document.addEventListener('DOMContentLoaded', () => {
        currentIndex = 0; // Bắt đầu từ index 0
        const initialUrl = `${url}?query=${query}&index=${currentIndex}`;
        loadUsers(initialUrl, token);
        setupLoadMoreButtonUsers(url, token, query)
    });
}

function showEndMessageUsers() {
    const userContainer = document.getElementById('userContainer');
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
            <p>There are no more users</p>
        </div>
    `;
    userContainer.appendChild(endMessage);
}

function createPostHtmlUsers(user) {
    return `
            <div class="shadow">
                <div class="card card-custom">
                    <a href="/profile/${user.userId}" class="w-100 text-decoration-none">
                        <div class="d-flex align-items-center gap-3 p-2">
                            <div class="avatar-custom">
                                <img src="${user.avatarUrl ? user.avatarUrl : '/images/unknown_user.jpg'}" alt="avatar">
                            </div>
                            <div class="d-flex flex-column">
                                <h1 class="fs-5 fw-bold truncate">${user.firstName} ${user.lastName}</h1>
                                <h2 class="fs-6 text-muted truncate">${user.username}</h2>
                            </div>
                        </div>
                    </a>
                </div>`;
}