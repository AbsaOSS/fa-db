# How to do testing

In order to execute tests in this module you need to:
1. deploy all sql code from database folder into postgres instance of your choice
2. make sure you have data in your tables as tests expect populated tables (unfortunately as this point this is not automated)
3. set up connection to your database in DoobieTest trait

## Using local postgres instance

See module `database/` for more details about how to actually perform all those steps from above programmatically.
