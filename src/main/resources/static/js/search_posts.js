function loadPosts(url, token) {
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
                const titlePosts = document.getElementById('title-posts');
                titlePosts.style.display = 'block';
                data.result.forEach(post => {
                    const postElement = createPostHtml(post);
                    postContainer.insertAdjacentHTML('beforeend', postElement);
                });
            }
            if (data.result.length < 10) {
                showEndMessage();
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
function setupLoadMoreButton(url, token, query) {
    const loadMoreButton = document.getElementById('load-more');
    if (loadMoreButton) {
        loadMoreButton.style.display = 'block'
        loadMoreButton.addEventListener('click', () => {
            currentIndex += 10;
            const newUrl = `${url}?query=${query}&index=${currentIndex}`;
            loadPosts(newUrl, token);
        });
    }
}

// Thêm event listener khi trang được load
function startLoad(url, query) {
    const token = localStorage.getItem('jwt');
    document.addEventListener('DOMContentLoaded', () => {
        currentIndex = 0; // Bắt đầu từ index 0
        const initialUrl = `${url}?query=${query}&index=${currentIndex}`;
        loadPosts(initialUrl, token);
        setupLoadMoreButton(url, token, query)
    });
}

function showEndMessage() {
    const postContainer = document.getElementById('postContainer');
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
            <p>There are no more posts</p>
        </div>
    `;
    postContainer.appendChild(endMessage);
}

function createPostHtml(post) {
    return `
            <div id="${post.postId}" class="card d-flex flex-row p-2 card shadow rounded-3 mb-4">
                <style>
                    /* Nút chung */
                    button {
                        background: none;
                        border: none;
                        padding: 8px;
                        cursor: pointer;
                        display: inline-flex;
                        align-items: center;
                        justify-content: center;
                    }
            
                    .already {
                        fill: red;
                    }
            
                    /* Nút yêu thích */
                    .heart-button:hover .heart-icon {
                        fill: red;
                    }
            
                    .heart-icon {
                        width: 24px;
                        height: 24px;
                    }
            
                    /* Nút trò chuyện */
                    .chat-button:hover .chat-icon {
                        fill: cyan;
                    }
            
                    .chat-icon {
                        width: 24px;
                        height: 24px;
                        fill: currentColor;
                    }
            
                    /* Hover màu chữ theo kiểu Tailwind */
                    .hover\\:text-red-400:hover {
                        color: #f87171; /* Màu đỏ */
                    }
            
                    .hover\\:text-cyan-400:hover {
                        color: #22d3ee; /* Màu xanh cyan */
                    }
                </style>
                <div class="d-flex ">
                    <img src="${post.user.avatarUrl ? post.user.avatarUrl : '/images/unknown_user.jpg'}" alt="avatar" alt="avatar" class="rounded-circle border-secondary" style="width: 2.5rem; height: 2.5rem; margin: 0.5rem;">
                </div>
                <div class="d-flex flex-column w-100">
                    <div class="d-flex justify-content-between">
                        <div class="d-flex align-items-center gap-2">
                            <a href="/profile/${post.user.userId}" class="font-weight-bold">
                                ${post.user.firstName} ${post.user.lastName}
                            </a>
                            <span class="text-secondary">@${post.user.username}</span>
                            <span>·</span>
                            <span class="text-secondary">${formatDateFromString(post.createdAt)}</span>
                        </div>
                        <a href="/posts/${post.postId}" class="fs-5">
                            <i class="bi bi-arrow-bar-right"></i>
                        </a>
                    </div>
                    <div class="min-h-12">
                        <p class="text-break">${post.caption}</p>
                    </div>
                    ${post.imageUrl ? `<img src="${post.imageUrl}" class="img-fluid rounded border w-100" alt="post image" onclick="event.stopPropagation()">` : ''}
                    ${post.videoUrl ? `<video class="img-fluid rounded border w-100" controls onclick="event.stopPropagation()">
                                        <source src="${post.videoUrl}">
                                        </video>` : ''}
                    <div class="d-flex justify-content-between mt-2">
                        <section class="d-flex gap-4">
                            <!-- Like Button -->
                            <div class="d-flex align-items-center">
                                <button
                                  title="Like this post"
                                  class="heart-button hover:text-red-400"
                                  onclick="likePost(event)"
                                  type="button"
                                  tabindex="0"
                                >
                                  <svg
                                    id="btn-${post.postId}"
                                    class="heart-icon"
                                    aria-hidden="true"
                                    viewBox="0 0 24 24"
                                    focusable="false"
                                    style="${post.liked ? 'fill: red;' : 'fill: black;'}"
                                  >
                                    <path
                                      d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54z"
                                    ></path>
                                  </svg>
                                </button>
                                <button type="button" class="btn btn-link p-0 text-danger" onclick="likePost()">
                                    <span id="count-liked-${post.postId}" class="hover-underline cursor-pointer" onclick="openLikedModal('${post.postId}')">${post.likedCount}</span>
                                </button>
                            </div>

                            <!-- Comment button -->
                            <div class="d-flex align-items-center">
                                <button
                                  class="chat-button hover:text-cyan-400"
                                  type="button"
                                  tabindex="0"
                                >
                                  <svg
                                    class="chat-icon"
                                    aria-hidden="true"
                                    viewBox="0 0 24 24"
                                    focusable="false"
                                  >
                                    <path
                                      d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2"
                                    ></path>
                                  </svg>
                                </button>
                                <button type="button" class="btn btn-link p-0 text-info">
                                    <span id="count-comment-${post.postId}" class="hover-underline cursor-pointer" onclick="handleOpenComment(event)">${post.commentCount}</span>
                                </button>
                            </div>
                        </section>
                        <button onclick="savePost(event)" type="button" class="btn btn-link p-0 text-info">
                            <i title="${post.saved ? 'Unsave this post' : 'Save this post'}" id="btn-save-${post.postId}" class="bi bi-bookmarks-fill" style="${post.saved ? 'color: blue;' : 'color: black;'}"></i>
                        </button>
                    </div>
                </div>
            </div>`;
}

function likePost(event) {
    let targetElement = event.currentTarget;
    for (let i = 0; i <= 4; i++) {
        targetElement = targetElement.parentNode;
    }
    // Lấy id của phần tử đó
    const postId = targetElement.id;
    let token = localStorage.getItem('jwt')
    fetch(`/api/posts/${postId}/like`, {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Error');
            }
            return response.json();
        })
        .then(data => {
            if (data.responseCode === 1000) {
                let likedButton = document.getElementById(`btn-${postId}`)
                let countLike = document.getElementById(`count-liked-${postId}`)
                if (data.result === 'liked') {
                    likedButton.style.fill = 'red';
                    countLike.textContent = (parseInt(countLike.textContent) + 1).toString();
                }
                else if (data.result === 'unliked') {
                    likedButton.style.fill = 'black';
                    countLike.textContent = (parseInt(countLike.textContent) - 1).toString();
                }
            } else {
                alert(data.message || 'An error occurred while sending the comment.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Failed to send comment. Please try again.');
        });
}

function savePost(event) {
    let targetElement = event.currentTarget;
    for (let i = 0; i <= 2; i++) {
        targetElement = targetElement.parentNode;
    }
    // Lấy id của phần tử đó
    const postId = targetElement.id;
    let token = localStorage.getItem('jwt')
    fetch(`/api/posts/${postId}/save`, {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Error');
            }
            return response.json();
        })
        .then(data => {
            if (data.responseCode === 1000) {
                let saveButton = document.getElementById(`btn-save-${postId}`)
                if (data.result === 'saved') {
                    saveButton.style.color = 'blue';
                    saveButton.title = 'Unsave this post'
                }
                else if (data.result === 'unsaved') {
                    saveButton.style.color = 'black';
                    saveButton.title = 'Save this post'
                }
            } else {
                alert(data.message || 'An error occurred while sending the comment.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Failed to send comment. Please try again.');
        });
}