package com.example.vega_visual

import java.sql.*

fun connectToDatabase(): Connection? {
    try {

        val url = "jdbc:postgresql://localhost:5432/bb"
        val user = "1234"
        val password = "1234"

        val connection = DriverManager.getConnection(url, user, password)
        return connection
    } catch (e: SQLException) {
        e.printStackTrace()
        return null
    }
}

fun fetchProducts(connection: Connection): List<Product> {
    val products = mutableListOf<Product>()
    try {
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM Products")
        while (resultSet.next()) {
            val id = resultSet.getInt("ID")
            val name = resultSet.getString("Name")
            val description = resultSet.getString("Description")
            val url = resultSet.getString("URL")
            val category = resultSet.getString("Category")
            val brand = resultSet.getString("Brand")
            val image = resultSet.getString("Image")
            products.add(Product(id, name, description, url, category, brand, image))
        }
    } catch (e: SQLException) {
        e.printStackTrace()
    }
    return products
}

fun createPricesTable(connection: Connection) {
    try {
        val statement = connection.createStatement()
        statement.execute("""
            CREATE TABLE Prices (
                ID INT AUTO_INCREMENT PRIMARY KEY,
                Product_ID INT,
                Value DECIMAL(10,2),
                DateTime DATETIME,
                FOREIGN KEY (Product_ID) REFERENCES Products(ID)
            )
        """)
    } catch (e: SQLException) {
        e.printStackTrace()
    }
}

fun insertPrice(connection: Connection, price: Price) {
    try {
        val preparedStatement = connection.prepareStatement("""
            INSERT INTO Prices (Product_ID, Value, DateTime)
            VALUES (id, 300, null)
        """)
        preparedStatement.setInt(1, price.productId)
        preparedStatement.setDouble(2, price.value)
        preparedStatement.setTimestamp(3, Timestamp.valueOf(price.dateTime))
        preparedStatement.executeUpdate()
    } catch (e: SQLException) {
        e.printStackTrace()
    }
}