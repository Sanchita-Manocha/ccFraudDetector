#ccFraudDetector
Detects the fraud credit card numbers for which price threshold exceeds over a 24 hour sliding window period.


## Table of contents
1) [Exercise](#Exercise)





##Exercise

Consider the following credit card fraud detection algorithm A credit card transaction is comprised of the following elements. hashed credit card number

timestamp - of format 'year-month-dayThour:minute:second' amount - of format 'dollars.cents'

Transactions are to be received as a comma separated string of elements. eg. 10d7ce2f43e35fa57d1bbf8b1e2, 2014-04-29T13:15:54, 10.00

A credit card will be identified as fraudulent if the sum of amounts for a unique hashed credit card number over a 24 hour sliding window period exceeds the price threshold. Write a method, which when given a sequence of transactions in chronological order, and a price threshold, returns the hashed credit card numbers that have been identified as fraudulent.