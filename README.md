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
To run the order book, walk through the following steps:
1. Go to the project run configurations, and choose "OrderBookApplication" as the Main Class.

[![run-configs.png](https://i.postimg.cc/g2DSQxz4/run-configs.png)](https://postimg.cc/v44t6H66)

2. Next, open the OrderBookController class and put a symbol and timestamp String as arguments into the pointInTimeResults method. These are the inputs the Order Book will use to see what was on the book for that symbol and timestamp.

[![controller-args.png](https://i.postimg.cc/TYSgwGkG/controller-args.png)](https://postimg.cc/rK1KZ6XY)

NOTE: The timestamp needs to be in the format "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'".

3. Run "mvn clean install" on the root directory of the repository to build the project.
4. After that succeeds, Run 'OrderBookApplication' to build the Spring Boot application. When the application has started, the last line of the console will look like this: 
```2021-04-25 09:16:15.598  INFO 15646 --- [           main] c.spotts.orderbook.OrderBookApplication  : Started OrderBookApplication in 39.238 seconds```

5. Open a new browser window and visit: http://localhost:8080
The point in time results will display in the webpage like this:

[![results.png](https://i.postimg.cc/LsqHsKbd/results.png)](https://postimg.cc/94h3xS21)

#### Future Enhancements
With more time, here are some future enhancements I would add to the project:
1. Reading the quotes from a stream instead of a .csv file
2. Be able to interact with the REST API more and have the user input values from the webpage
3. Change the output results from a formatted String to an Object
4. Parse the input Timestamps differently

#### Final Thoughts
This project presented a number of interesting and challenging problems, and I learned a lot along the way. Thanks for reading!