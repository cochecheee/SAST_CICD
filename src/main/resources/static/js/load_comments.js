function clearAndAddComment(postId) {
    currentIndex = 0;
    const commentContainer = document.getElementById('comment-container');
    if (commentContainer) {
        commentContainer.innerHTML = '';
    }
    let token = localStorage.getItem("jwt");
    fetch(`/api/comments/posts/${postId}?index=${currentIndex}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(data => {
            if (data.responseCode === 1000 && Array.isArray(data.result) && data.result.length >= 0) {
                let comments = data.result;
                if (comments.length === 5) {
                    const loadMoreDiv = document.createElement('div');
                    loadMoreDiv.id = 'load-more';
                    loadMoreDiv.classList.add('text-center', 'mt-4');
                    loadMoreDiv.innerHTML = `
                            <button class="">
                                <p class="text-center text-primary cursor-pointer fw-bold" id="loadMoreComments">Load more older comments</p>
                            </button>
                        `;

                    loadMoreDiv.querySelector('button').addEventListener('click', function () {
                        loadMoreMessages(postId);
                    });
                    commentContainer.appendChild(loadMoreDiv);
                } else {
                    const noMoreMessage = document.createElement('div');
                    noMoreMessage.id = 'load-more';
                    noMoreMessage.classList.add('text-center', 'mt-4');
                    noMoreMessage.innerHTML = `
                            <i class="fw-bold">There are no earlier comments</i>
                        `;
                    commentContainer.insertBefore(noMoreMessage, commentContainer.firstChild);
                }

                comments.forEach(comment => {
                    const commentDiv = createCommentHtml(comment);
                    commentContainer.appendChild(commentDiv);
                });

            } else {
                showEndMessage();
                removeLoadMoreButton();
            }
        })
        .catch(error => console.error('Error fetching comments:', error));
}

function loadMoreMessages(postId) {
    currentIndex += 5;
    const commentContainer = document.getElementById('comment-container');
    let token = localStorage.getItem("jwt");
    fetch(`/api/comments/posts/${postId}?index=${currentIndex}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(data => {
            const loadMoreDiv = document.getElementById('load-more');
            loadMoreDiv.remove();
            if (data.responseCode === 1000 && Array.isArray(data.result) && data.result.length >= 0) {
                let comments = data.result;
                comments.reverse().forEach(comment => {
                    const messageDiv = createCommentHtml(comment);
                    commentContainer.insertBefore(messageDiv, commentContainer.firstChild);
                });

                if (comments.length === 5) {
                    const loadMoreDiv = document.createElement('div');
                    loadMoreDiv.id = 'load-more';
                    loadMoreDiv.classList.add('text-center', 'mt-4');
                    loadMoreDiv.innerHTML = `
                            <button class="">
                                <p class="text-center text-primary cursor-pointer fw-bold" id="loadMoreComments">Load more older comments</p>
                            </button>
                        `;
                    // Thêm sự kiện click để tải thêm tin nhắn cũ
                    loadMoreDiv.querySelector('button').addEventListener('click', function () {
                        loadMoreMessages(postId);
                    });
                    commentContainer.insertBefore(loadMoreDiv, commentContainer.firstChild);
                } else {
                    const noMoreComments = document.createElement('div');
                    noMoreComments.id = 'load-more';
                    noMoreComments.classList.add('text-center', 'mt-4');
                    noMoreComments.innerHTML = `
                            <i class="fw-bold">There are no earlier comments</i>
                        `;
                    commentContainer.insertBefore(noMoreComments, commentContainer.firstChild);
                }
            } else {
                showEndMessage();
                removeLoadMoreButton();
            }
        })
        .catch(error => console.error('Error fetching posts:', error));
}

function createCommentHtml(comment) {
    const htmlString = `
        <div id="${comment.commentId}" class="container mt-3">
            <div class="d-flex gap-3">
                <!-- Avatar -->
                <a href="/profile/${comment.user.userId}">
                    <img src="${comment.user.avatarUrl || '/images/unknown_user.jpg'}" alt="avatar" class="avatar" style="width: 2rem; height: 2rem;">
                </a>
                <!-- Comment Content -->
                <div>
                    <div class="comment-box" style="padding: 10px; font-size: 0.875rem;">
                        <a href="/profile/${comment.user.userId}">
                            <h3 class="fw-semibold mb-1" style="font-size: 0.95rem;">
                                <span>${comment.user.firstName}</span>
                                <span>${comment.user.lastName}</span>
                            </h3>
                        </a>
                        <p class="whitespace-pre-line mb-2" style="font-size: 0.875rem;">${comment.content}</p>
                    </div>
                    <!-- Like and Date Section -->
                    <div class="d-flex gap-3 align-items-center">
                        <div class="d-flex align-items-center">
                            <!-- Like Button -->
                            <button class="btn btn-link text-cyan cursor-pointer p-0" onclick="likeComment(event)">
                                <i title="${comment.liked ? 'Unlike this comment' : 'Like this comment'}" id="btn-like-${comment.commentId}" class="bi bi-hand-thumbs-up-fill" style="font-size: 1rem; ${comment.liked ? 'color: blue;' : 'color: black;'}""></i>
                            </button>
                            <span id="count-liked-${comment.commentId}" class="cursor-pointer" onclick="showLikedUsers()" style="font-size: 0.875rem;">${comment.likedCount || 0}</span> <!-- Liked count -->
                        </div>
                        <span class="text-muted" style="font-size: 0.75rem;">${formatDateFromString(comment.createdAt)}</span> <!-- Timestamp -->
                    </div>
                </div>
            </div>
        </div>`;

    // Sử dụng DOMParser để chuyển chuỗi HTML thành DOM node
    const parser = new DOMParser();
    const doc = parser.parseFromString(htmlString, 'text/html');
    return doc.body.firstChild; // Trả về node đầu tiên trong body
}

function likeComment(event) {
    let targetElement = event.currentTarget;
    for (let i = 0; i <= 4; i++) {
        targetElement = targetElement.parentNode;
    }
    // Lấy id của phần tử đó
    const commentId = targetElement.id;
    let token = localStorage.getItem('jwt')
    fetch(`/api/comments/like/${commentId}`, {
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
                let likedButton = document.getElementById(`btn-like-${commentId}`)
                let countLike = document.getElementById(`count-liked-${commentId}`)
                if (data.result === 'liked') {
                    likedButton.style.color = 'blue';
                    likedButton.title = 'Unlike this comment';
                    countLike.textContent = (parseInt(countLike.textContent) + 1).toString();
                }
                else if (data.result === 'unliked') {
                    likedButton.style.color = 'black';
                    likedButton.title = 'Like this comment';
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