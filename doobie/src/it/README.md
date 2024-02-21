### How to do testing

In order to execute tests in this module you need to:
- deploy all sql code from database folder into postgres instance of your choice 
- make sure you have data in your tables as tests expect populated tables (unfortunately as this point this is not automated)
- set up connection to your database in DoobieTest trait