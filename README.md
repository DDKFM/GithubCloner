# Github Cloner

# build
## Linux
./mnvw clean package
## Windows
mvnw.cmd clean package

# run

java -jar target/GithubCloner-1.0-jar-with-dependencies.jar --username <username> --password <password>


# additional parameters

--repos <location for repos>

--query <search query>

--pages <page size>

--language <programming language>

--threads <max threads for git cloning>