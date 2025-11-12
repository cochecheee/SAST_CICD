$(document).ready(function () {
    $('#loginForm').submit(function (event) {
        event.preventDefault();

        const email = $('#email').val().trim();
        const password = $('#password').val().trim();
        let isValid = true;

        // Reset errors
        $('#loginError').text('');

        // Validate email and password length
        if (email.length < 6) {
            $('#loginError').text('Email must be at least 6 characters.');
            isValid = false;
        }
        if (password.length < 6) {
            $('#loginError').text('Password must be at least 6 characters.');
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Perform AJAX request
        $.ajax({
            url: '/auth/login',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ email: email, password: password }),
            success: function (response) {
                localStorage.setItem("jwt", response.jwt);
                window.location.href = '/';
            },
            error: function (error) {
                $('#loginError').text( 'Your email or password does not match.');
            }
        });
    });

    $(document).ready(function () {
        $('#registerForm').submit(function (event) {
            event.preventDefault();

            const username = $('#username').val().trim();
            const firstName = $('#firstName').val().trim();
            const lastName = $('#lastName').val().trim();
            const email = $('#email').val().trim();
            const password = $('#password').val().trim();
            const rePassword = $('#re-password').val().trim();
            const genderValue = $('input[name="gender"]:checked').val();
            let gender;

            // Map gender values
            if (genderValue === "male") gender = true;
            else if (genderValue === "female") gender = false;
            else gender = null;

            let errorMessage = '';

            // Validate username, email, password
            if (username.length < 6) {
                errorMessage = 'Username must be at least 6 characters.';
            } else if (firstName.length === 0 || firstName.length > 40) {
                errorMessage = 'First Name must not be empty and up to 40 characters.';
            } else if (lastName.length === 0 || lastName.length > 40) {
                errorMessage = 'Last Name must not be empty and up to 40 characters.';
            } else if (email.length < 6) {
                errorMessage = 'Email must be at least 6 characters.';
            } else if (password.length < 6) {
                errorMessage = 'Password must be at least 6 characters.';
            } else if (password !== rePassword) {
                errorMessage = 'Passwords do not match.';
            }

            // Show error if exists
            if (errorMessage) {
                $('#registerError').text(errorMessage);
                return;
            }

            // Perform AJAX request
            $.ajax({
                url: '/auth/register',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({
                    username: username,
                    firstName: firstName,
                    lastName: lastName,
                    email: email,
                    password: password,
                    gender: gender
                }),
                success: function () {
                    alert('Register successfully, check your email to verify')
                    window.location.href = '/login';
                },
                error: function (error) {
                    $('#registerError').text(error.responseJSON.message || 'An error occurred during registration. Please try again.');
                }
            });
        });
    });

    $('#resetPasswordForm').submit(function (event) {
        event.preventDefault();

        const email = $('#email').val().trim();
        const password = $('#password').val().trim();
        const rePassword = $('#re-password').val().trim();
        let isValid = true;

        // Reset errors
        $('#resetError').text('');

        // Validate email and password length
        if (email.length < 6) {
            $('#resetError').text('Email must be at least 6 characters.');
            isValid = false;
        }
        if (password.length < 6) {
            $('#resetError').text('Password must be at least 6 characters.');
            isValid = false;
        }
        if (password !== rePassword) {
            $('#resetError').text('Password must be at least 6 characters.');
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Perform AJAX request
        $.ajax({
            url: '/auth/reset-password',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ email: email, newPassword: password }),
            success: function (response) {
                alert('You have request to reset the password, check your email to confirm');
                window.location.href = '/login';
            },
            error: function (error) {
                $('#resetError').text( 'The account with this email doesn\'t exists!');
            }
        });
    });
});