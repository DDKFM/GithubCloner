import com.beust.klaxon.*
import java.io.File
import java.io.FilenameFilter
import java.io.IOException
import java.net.URL
import java.util.stream.Collectors

/**
 * Created by maxsc on 13.11.2017.
 */
fun main(args : Array<String>) {
    var currentFetchedRepos = 0
    val maxRepos = currentFetchedRepos + 1000
    val gitLinks : MutableList<String> = mutableListOf()
    val keyword = "created:>2017-01-01"
    val sort = "stars"
    val order = "desc"
    val accessToken = ""
    val language = ""
    val pages = "1000"
    while(currentFetchedRepos < maxRepos) {
        val url = URL("https://api.github.com/search/repositories?access_token=$accessToken&q=$keyword&language=$language&per_page=$pages&sort=$sort?order=$order&fork=false") //repositories?access_token=548de33a5fb666f3f3dd737a4f065ff9ae829332&since=$currentFetchedRepos")
        var connection = url?.openConnection()
        val remaining : Int = Integer.parseInt(connection.getHeaderField("X-RateLimit-Remaining"))
        if(remaining <= 2) {
            Thread.sleep(10)
        }
        val bytes: ByteArray = connection.getInputStream().readBytes()
        val content = StringBuilder(String(bytes))
        val parser: Parser = Parser()
        val responseAsJSON : JsonObject= parser.parse(content) as JsonObject
        val json : JsonArray<JsonObject> = responseAsJSON.array<JsonObject>("items") as JsonArray<JsonObject>
        var list : List<String> = json.value
                .stream()
                .filter { !(it.boolean("fork") as Boolean) }//no forks allowed
                .map { it.string("html_url") }
                .collect(Collectors.toList())
                as List<String>
        gitLinks.addAll(list)

        println("Size: ${json.size}")
        currentFetchedRepos += json.size
    }
    val maxThreads = 20
    var currentThreads = 0

    for(repo in gitLinks) {
        if(currentThreads <= maxThreads) {
            var thread = Thread( {
                cloneRepository(repo)
            })
            thread.start()
            println("Thread ${currentThreads + 1} started")
            currentThreads++
        } else {
            cloneRepository(repo)
        }
    }
}

fun cloneRepository(repo : String) {
    val name = repo.substring(repo.lastIndexOf("/"))

    if(!File("./repos/$name/").exists()) {
        println("Fetch $repo")
        "git clone $repo".runCommand(File("./repos"))
        println("$repo fetched")
    }

    val filter : FilenameFilter = FilenameFilter { dir, name -> !name.endsWith(".git")
    }//all files that not ends with .git
    val files = File("./repos/$name").listFiles(filter)
    if(files != null)
        for(file in files) {
            val result : Boolean = file.deleteRecursively()
            println(file.toString() + ": $result")
        }
}
fun String.runCommand(workingDir: File): String? {
    try {
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
        proc.waitFor()
        return proc.inputStream.bufferedReader().readText()
    } catch(e: IOException) {
        e.printStackTrace()
        return null
    }
}