Register BYU
========

#############################
## Getting Started
#############################

You can open this project in any IDE you want.
Typical is IntelliJ. With intelliJ you can simply do File > Open.. then select the build.gradle file
it should pull in the whole project.
If your using eclipse you can go: File>Import>Gradle (If this is not an option,
install the gradle integration from marketplace via help). Follow the instructions after this.

#############################
## Building
#############################

Run the following to build.

    ./gradlew clean build
    
This will create a war file. 

########################
## Running Locally
########################

The best way to run locally is in your IDE. Most ide's are smart enough to figure it out. In IntelliJ all you have to do is run the `src/main/java/controllers/WebApplication.java` file. You can also just select the gradle "run" target. Of course you will also need to have an instance of mongodb running

To run directly (from a terminal or w/e), run the following:

    ./gradlew run
    
You should see some text say "Spring" like this:
    
    Starting app with System Args: []
      .   ____          _            __ _ _
     /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
    ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
     \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
      '  |____| .__|_| |_|_| |_\__, | / / / /
     =========|_|==============|___/=/_/_/_/
     :: Spring Boot ::        (v1.2.2.RELEASE)

If you have that then you are good to go!!!
You can now connect to your local server at localhost:8080

Of course the easiest way to run locally is just run `docker-compose up` but that assumes you have docker-compose installed. It also only works with the pre-built image. It does not run your local version of the code unless you add an overrides file.

############
## Docker
############

If you understand docker... you can build a docker image using `./gradlew docker -x test` and you can run the code against a local database using `docker-compose up`

###########
## Spring
###########
If you havent used spring before, below is a basic tutorial.
http://spring.io/guides/gs/rest-service/
