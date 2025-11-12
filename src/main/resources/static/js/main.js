// Tạo event listener tùy chỉnh
const userEvent = new Event('userUpdated');

// Lắng nghe sự kiện userUpdated
document.addEventListener('userUpdated', () => {
    if (user) {
        connectWebSockeNotify(user.userId);
        let contentContainer = document.getElementById('content-container')
        if (contentContainer) {
            contentContainer.style.display = 'block';
            const token = localStorage.getItem('jwt');
            const url = "/api/chats/user-chat";
            currentIndex = 0; // Bắt đầu từ index 0
            const initialUrl = `${url}?index=${currentIndex}`;
            loadUserChats(initialUrl, token);
            // setupLoadMoreButton(url, token);
        }
        let userAvatar = document.getElementById('post-current-user');
        if (userAvatar)
            userAvatar.src = user.avatarUrl;
    }
});

// Hàm cập nhật user
function updateUser(newUser) {
    user = newUser;
    let createPostUserAvatar = document.getElementById('createPostUserAvatar');
    if (createPostUserAvatar && user.avatarUrl != null)
        createPostUserAvatar.src = user.avatarUrl
    document.dispatchEvent(userEvent); // Phát sự kiện userUpdated
}

$(document).ready(function () {
    // Hiển thị thông tin người dùng đăng nhập thành công
    $.ajax({
        type: 'GET',
        url: '/api/users/fromToken',
        contentType: 'application/json',
        beforeSend: function (xhr) {
            if (localStorage.jwt) {
                xhr.setRequestHeader('Authorization', 'Bearer ' + localStorage.jwt);
            }
        },
        success: function (data) {
            updateUser(data.result)
            // Cập nhật thông tin người dùng vào phần tử HTML
            $('#firstName').html(data.result.firstName);
            $('#lastName').html(data.result.lastName);
            $('#username').html('@' + data.result.username);
            if (data.result.avatarUrl != null)
                $('#avatar').html(document.getElementById('avatar').src = data.result.avatarUrl);
            if (data.result.role === 'ADMIN')
                document.getElementById('admin-link').classList.remove('d-none');
            document.body.classList.remove('d-none');
        },
        error: function (e) {
            var json = e.responseText;
            $('#feedback').html(json);
            window.location.href = "/login";
        }
    });
    // Login
    // $('#Login').click(function () {
    //     var email = document.getElementById('email').value;
    //     var password = document.getElementById('password').value;
    //     var basicInfo = JSON.stringify({
    //         email: email,
    //         password: password
    //     });
    //     $.ajax({
    //         type: "POST",
    //         url: "/auth/login",
    //         dataType: "json",
    //         contentType: "application/json; charset=utf-8",
    //         data: basicInfo,
    //         success: function(data) {
    //             localStorage.token = data.token;
    //             window.location.href = "/user/profile";
    //         },
    //         error: function() {
    //             alert("Login failed");
    //         }
    //     })
    // })
    // // Logout
    // $('#Logout').click(function () {
    //     localStorage.clear();
    //     window.location.href = "/login";
    // });
});