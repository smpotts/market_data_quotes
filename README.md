# Order Book

### Overview
This project builds a point in time order book and displays the 5 best bid and 5 best ask prices given any symbol and point in time timestamp.

#### Technologies
The project is written in Java (v. 11.0.8) using the Spring Boot framework (v. 2.4.5).

#### Relevant Terminology
Order Book:  a list of buy and sell orders for a security or instrument organized by price & time.

Quote: the most recent price that a buyer and seller agreed upon and at which some amount of the asset transacted. 

National Best Bid (NBB): the best bid(s) on the top of the book.

National Best Offer (NBO): the best offer(s) on the top of the book. 

### Run the Order Book
To run the order book, walk through the following steps
1. Go to the project run configurations, Edit Configurations in IntelliJ for example, and put the following class in the Main Class: "com.spotts.orderbook.OrderBookApplication".

[edit-configuration.png](https://postimg.cc/1gZsNXYW)
2. Run "mvn clean install" on the root directory of the repository to build the project.
