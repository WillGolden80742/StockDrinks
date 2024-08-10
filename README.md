# StockDrinks

[![GitHub license](https://img.shields.io/github/license/WillGolden80742/StockDrinks)](https://github.com/WillGolden80742/StockDrinks/blob/master/LICENSE)

StockDrinks is an Android application designed to help users track their daily drink intake. It allows users to add drinks, categorize them, and view summaries of their consumption. The app also includes features for searching and managing drinks, and for calculating quantities based on simple mathematical expressions.

## Features

-   **Daily Drink Tracking**: Log the drinks you consume each day.
-   **Categorization**: Organize drinks into different categories.
-   **Quantity Calculation**: Calculate drink quantities using basic math operations.
-   **Drink Management**: Add, edit, and remove drinks from your database.
-   **Search Functionality**: Easily find drinks by name.
-   **Data Caching**: Utilize caching for improved performance.

## Project Structure

The project follows a standard Android architecture with the following key components:

-   **Activities**:
    -   `dailyDrinks`: Main activity to display and manage daily drink records.
    -   `dailyDrinksList`: Activity to view and edit the list of drinks for a specific day.
    -   `formDailyDrinks`: Activity to add or edit a daily drink record.
    -   `formDrinks`: Activity to add, edit, or remove drinks from the database.
-   **Adapters**:
    -   `DailyDrinksAdapter`: Adapts `DailyDrinks` objects to the list view in `dailyDrinks`.
    -   `DrinksAdapter`: Adapts `Drink` objects to list views in various activities.
-   **Controllers**:
    -   `Cache`: Handles caching of data.
    -   `DailyDrinks`: Represents a collection of drinks consumed on a specific day.
    -   `Drink`: Represents a single drink item with details like name, quantity, category, etc.
    -   `JSON`: Utility class for JSON serialization and deserialization.

## Setup and Installation

1.  **Clone the repository**:
    ```bash
    git clone [https://github.com/WillGolden80742/StockDrinks.git](https://github.com/WillGolden80742/StockDrinks.git)
    ```
2.  **Open in Android Studio**: Open the project in Android Studio.
3.  **Build and run**: Build the project and run it on an Android emulator or device.

## Usage

1.  **Add Drinks**: Use the `formDrinks` activity to add new drinks to the database.
2.  **Log Daily Intake**: Go to `formDailyDrinks` to log the drinks you consume each day. You can search for drinks, specify quantities, and add them to your daily log.
3.  **View Daily Summaries**: The `dailyDrinks` activity displays a list of your daily drink records. You can view summaries of your drink intake for each day.
4.  **Edit or Delete**: You can edit or delete drink records and individual drinks from their respective activities.

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues.

## License

This project is licensed under the [LICENSE NAME] License - see the [LICENSE](LICENSE) file for details.
