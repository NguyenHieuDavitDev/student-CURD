/* Chạy một lần sau khi container SQL Server đã sẵn sàng. */
IF DB_ID(N'QLSV') IS NULL
BEGIN
    CREATE DATABASE QLSV;
END
GO
