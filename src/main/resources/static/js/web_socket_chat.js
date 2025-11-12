// Already connect in notification

// let socket = new WebSocket('/ws');
// let stompClient = null;
// let subscription;
// let jwtToken = localStorage.getItem('jwt');

// function connectWebSocket() {
//     stompClient = Stomp.over(socket); // Sử dụng WebSocket để tạo client Stomp
//
//     stompClient.connect({}, function(frame) {
//         console.log('Connected: ' + frame);
//     });
// }
// connectWebSocket();
function subscribeToUserTopic(chatId) {
    if (notifyStompClient && notifyStompClient.connected) {
        // Hủy đăng ký kênh cũ nếu đã có
        unsubscribeClient();
        let currentUser = user;
        // Đăng ký để nhận tin nhắn từ server
        notifySubscription = notifyStompClient.subscribe(`/user/${chatId}/chat/private`, function(message) {
            const chatMessagesDiv = document.getElementById('chat-messages');
            let msg = JSON.parse(message.body)
            const chatContainer = document.getElementById('chat-container');
            const isCurrentUser = msg.user.userId === currentUser.userId;
            const messageDiv = document.createElement('div');
            messageDiv.classList.add('p-2');
            if (isCurrentUser) {
                // If message is from current user, display as outgoing message
                messageDiv.classList.add('text-end');
                messageDiv.innerHTML = `
                                    <div id="${msg.messageId}">
                                        <div class="bg-primary text-white rounded p-2 d-inline-block">
                                            <p class="mb-0">${msg.content}</p>
                                        </div>
                                        <small class="text-muted d-block">${formatTimestamp(msg.timestamp)}</small>
                                    </div>
                                `;
            }
            else {
                // If message is from another user, display as incoming message
                messageDiv.innerHTML = `
                                <div id="${msg.messageId}" class="d-flex">
                                    <div class="avatar-custom-2">
                                        <img src="${msg.user.avatarUrl || '/images/unknown_user.jpg'}" alt="avatar" class="rounded-circle me-2">
                                    </div>
                                    <div>
                                        <div class="bg-light rounded p-2">
                                            <p class="mb-0">${msg.content}</p>
                                        </div>
                                        <small class="text-muted">${formatTimestamp(msg.timestamp)}</small>
                                    </div>
                                </div>
                                `;
            }
            // Append message to chat container
            chatMessagesDiv.appendChild(messageDiv);
            chatContainer.scrollTop = chatContainer.scrollHeight;
        }, { "Authorization": `Bearer ${notifyJwtToken}` });
        // Lấy phần tử cha chứa input và nút gửi
        const chatInputContainer = document.querySelector('.chat-input');

        // Xoá các phần tử hiện tại
        chatInputContainer.innerHTML = '';

        // Tạo lại phần tử chatInput và sendButton
        chatInputContainer.innerHTML = `
                <textarea id="chatInput" class="form-control" rows="1" placeholder="Type your message..."></textarea>
                <button id="sendButton" class="btn btn-primary">
                    <i class="bi bi-send"></i>
                </button>
            `;
        function sendMessage() {
            const messageContent = document.getElementById('chatInput').value.trim(); // Lấy giá trị từ textarea theo id
            if (messageContent && notifyStompClient.connected) { // Kiểm tra nội dung tin nhắn và kết nối
                notifyStompClient.send(`/app/chat/${chatId}`, {}, JSON.stringify({ 'content': messageContent }));
                console.log(chatId);
                console.log('Đã gửi tin nhắn: ' + messageContent);
                document.getElementById('chatInput').value = ''; // Xóa nội dung textarea sau khi gửi
            } else {
                console.log('Vui lòng nhập tin nhắn hoặc kiểm tra kết nối WebSocket');
            }
        };

        // Lắng nghe sự kiện nhấn nút gửi
        document.getElementById('sendButton').addEventListener('click', sendMessage);
    }
}

// Hàm unsubscribe
function unsubscribeClient() {
    if (notifySubscription) {
        console.log(notifySubscription)
        notifySubscription.unsubscribe();
    }
}