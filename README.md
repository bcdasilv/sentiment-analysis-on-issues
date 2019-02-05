# Senior-Project
This is my senior project for running sentiment analysis on commit messages for Jira projects.  This package includes the code that I wrote for obtaining the data from Jira to run the analysis.

There were a few APIs that I tried out for querying Jira, but [JRJC (Jira REST Java Client)](https://docs.atlassian.com/jira-rest-java-client-api/2.0.0-m31/jira-rest-java-client-api/apidocs/) was the one that worked best for me.  It is a bit wonky and weird to use, but I was able to get it work well enough.  

All the magic happens in `JRJC.java` and `SSWrapper.java`.  Here's a walkthrough of the code:

## `setup()`
The `setup()` method sets up the Jira client and creates a new CSV file to dump the data.  Notice that the `createWithBasicHttpAuthentication()` call requires your Jira username and password.  If you don't have a Jira account, signing up is free and easy.

I used [CSVPrinter](https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVPrinter.html) to print the data to a csv file.  This example outputs the data to a file called sample.csv.

## `main()`
`main()` will search Jira with a [JQL (Jira Query Language)](https://confluence.atlassian.com/jiracore/blog/2015/07/search-jira-like-a-boss-with-jql) search.  The code right now asks for the first 1000 issues for project HADOOP.

## `runSearch()`
`runSearch()` will take each issue returned by the search, use the issueClient to obtain more information about it, and then write the info to the CSV.  The code is pretty self-explanatory -- I had to write a bit of logic to format the data so that it would be easier to read in the CSV, but that's about it.  The `csvPrinter.flush()` call will write to the CSV after each individual issue has been analyzed. 

## sentiment scores
`runSearch()` makes two calls to `ssWrapper`, a helper class I made for calling the sentiment analysis library.  In the `SSWrapper` constructor, SentiStrength has to be initialized and you'll have to provide the path to where the SentiStrength data files are located.  The `process` method will process the inputted text and prepare it for scoring.  The `compute` call will actually compute the scores. Look [here](http://sentistrength.wlv.ac.uk/) for more information about the sentiment analysis library.  That page includes a word document that explains how to use the Java library.

## General Guidance
This code isn't perfect, but it got the job done for me. The most annoying part, by far, was how inconsistent JRJC would be for me. Sometimes I would be able to query for 1000 issues and it would finish within 7-8 minutes, yet other times it would take 20+ minutes and not even finish 200. There isn't much information for this library online and the error messages were quite cryptic, so I thought it was easier just to deal with the annoyances rather than look into fully resolving the issues. It's possible that the service doesn't like receiving such a large number of requests at once, causing it to error out. Anyway, if further investigation is needed, there are a lot of messages being logged that can be looked into.
