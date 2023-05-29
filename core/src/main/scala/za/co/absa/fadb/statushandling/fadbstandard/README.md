# Intro

This is a list of convention status codes to return from functions - the primary usage intention is in Postgres DB 
functions, particularly in combination with fa-db library.

The expected usage is that each function returns at least two values - `status` and `status_text`.

Status is one of the values below. Status text is a human readable explanation of what happened. Status text should not 
have a syntactical meaning.

Exceptions from the rule above:
* Immutable functions returning simple value
* Function returning a (large) set of values, basically a query, where the obvious result would be no records for _“not found”_, otherwise a general ok `status` and `status_text`  on each line, just increasing the size of returned data.
* _“Private”_ functions (not callable from outside of DB (no grants), name starting with underscore), if it’s more convenient for the usage of the function. It’s still recommended to use the `status` and `status_text` even there.

General principle of status (code):
* The codes are two digit
* They are divided into sections
* There are fixed values with a preferred meaning, try to keep to it
* In each range there are “open” values to use in other or sub-cases of the general range meaning. Their meaning is specific the each function and depends on the contract between the function and the app calling it
* Function should list all the possible returned statuses in it’s header/inline documentation together with their used meaning
* When a status code suggests a not-OK state (values 20-99) the eventual other returned field values beyond `status` and `status_text` are undefined (probably will be NULL, but it should not be taken as a fact)
* The possible returned status codes and their meaning should be described in the function comments

# Codes

The codes to be double digit

## OK/Correct outcome

### 10-19

| 10 | general ok   |
| 11 | data created |
| 12 | data updated |
| 14 | no op needed |
| 15 | data deleted |

Rest unspecified OK status, to be agreed in contract between DB and app

## Server misconfiguration

### 20-29

Specific to the database application, should be shared over all functions in that DB.

## Data conflict

### 30-39

| 30 | general data conflict |
| 31 | referenced data does not allow execution of the request |

Rest of the codes meaning depends on agreement in contact between DB and app

## Data not found

### 40-49

| 40 | requested data not found |
| 41 | master record not found  |
| 42 | detail record not found  |

Rest of the codes meaning depends on agreement in contact between DB and app

## Error in data

### 50-89

| 50-59 | generally incorrect data (when obvious from context)                                     |
| 60-69 | missing value (usually NULL)                                                             |
| 70-79 | value out of range                                                                       |
| 80-89 | wrong content of complex types (json, xml, hstore), like missing key, undesired key etc. |

## Free range for other errors

### 90-99

Rest of the codes meaning depends on agreement in contact between DB and app
