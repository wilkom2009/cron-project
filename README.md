# cron-project
## Project description
This project is a tutorial project to highlight the use of [Spring Batch](https://docs.spring.io/spring-batch/reference/) app to process files from Amazon S3 and save the processed data from the files into a database.

## Dependencies

This project is bootstraped using:

* [Spring Boot Maven Plugin](https://docs.spring.io/spring-boot/docs/3.2.1/maven-plugin/reference/html/)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/3.2.1/reference/htmlsingle/index.html#using.devtools) 
* [Spring Batch](https://docs.spring.io/spring-boot/docs/3.2.11/reference/htmlsingle/index.html#howto.batch) 
* [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) 

To read from [Amazon S3](https://docs.aws.amazon.com/s3/) and send mail notification using [Amamzon SES](https://docs.aws.amazon.com/ses/), there is a need of SDK :

* [aws-java-sdk-s3 1.12.261](https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk-s3/1.12.261) 
* [aws-java-sdk-ses 1.12.529](https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk-ses/1.12.529) 


