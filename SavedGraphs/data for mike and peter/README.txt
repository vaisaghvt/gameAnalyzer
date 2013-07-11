There two datasets displayed:

1. Room Visit frequency:

a. Random walk room visit frequency : 
	This chart shows the ratio of room visits for a particular room / total number of visits averaged over all the random walks.Colors do not mean anything. The x axis is the room and y axis is the value. The rooms are first sorted by floor. And within a floor they are sorted first by degree (number of edges into room) and then by alphabetic order.

b. Average room visit frequency 1st attempt chart:

The x axis is again the room name and the y axis is the ratio 

	x_r = (x_indi/N_indi)/(x_random/N_random)
Where x_indi is the number of visits by an individual to the room x. N_indi is the total number of room visits by that individual, x_random is the number of visits to that room by random walk and, N_random is the total number of visits by random walk.

A value greater than 1 indicates that the room was visited more than a random walk would while a value less than 1 indicates the opposite. 

Again, colors do not mean anything and the same sorting as above is done.

the x_r in the graph is the average of all first attempts.
 

c. Unscaled room layout :

Shows a graph of the room layout. A line has been drawn to indicate floor seperation.


d. 1st attempt room visit frequency :

This is just an alternative representaiton of b. The red color indicates an r value of greater than 1.05 and the green color indicates a value of less than 0.95. The diameter of the room in this graph is equal to x_r*(unscaled diameter). 




2.Inter visit delta T:

Here delta t is the time between revisits to a room. The data is the sum (histogram) of all the 1st attempts. Two graphs show the pattern exists in different bin sizes. The same pattern can be seen for much higher bin sizes also.

The x axis goes till about 1800 seconds. This is because the most time taken by a player during exploration is close to 30 minutes or 1800 seconds.