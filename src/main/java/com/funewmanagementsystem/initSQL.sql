create database SBA_ASSIGNMENT

       /**
       insert admin to db
        */
INSERT INTO system_account (
    account_name,
	account_email,
    account_password,
    account_role
)
VALUES (
    'admin',
	'admin@gmail.com',
    '$2a$10$E8yZZmh/JCV5q3GASBoGR.a.zOsRG0uAsNzN4fUF.JjgwzuwVMcyy',
    'ADMIN'
);