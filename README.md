# Kasiria

Final project for `MOBI6059001 - Mobile Programming` course.

Kasiria is a simple Indonesian mobile POS application built for native android using Java. Feature include a simple but friendly user interface, product and transaction management, receipt printing with DantSu's [ESCPOS-ThermalPrinter-Android](https://github.com/DantSu/ESCPOS-ThermalPrinter-Android) library, and PDF receipt generation with [Documentero](https://documentero.com/) API.

## Features

1. **Firebase User Authentication**
   - Secure sign-in and sign-up functionality
   - Password recovery via Firebase authentication services

2. **Product and Transaction Management**
   - Create, read, update, and delete (CRUD) operations for products and transactions

3. **Transaction Receipt Printing**
   - Print receipts directly using the ESCPOS-ThermalPrinter-Android library
   - Generate and download PDF receipts via Documentero API
