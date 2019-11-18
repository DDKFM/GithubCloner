package de.ddkfm.githubcloner

import com.xenomachina.argparser.ArgParser
import de.vandermeer.asciitable.AsciiTable
import de.vandermeer.asciithemes.u8.U8_Grids
import org.eclipse.jgit.api.Git
import org.kohsuke.github.GHDirection
import org.kohsuke.github.GHRepositorySearchBuilder
import org.kohsuke.github.GitHub
import java.io.File
import java.util.concurrent.Executors

fun main(args : Array<String>) {
    ArgParser(args).parseInto(::Args).run {

        println("Github Cloner Parameters")
        val table = AsciiTable().apply {
            addRule()
            addRow("username", username)
            addRule()
            addRow("password", password.replace(".".toRegex(), "*"))
            addRule()
            addRow("repos", repos)
            addRule()
            addRow("query", query)
            addRule()
            addRow("pages", pages)
            addRule()
            addRow("language", language)
            addRule()
            addRow("threads", threads)
            addRule()
        }
        table.context.grid = U8_Grids.borderDoubleLight()
        println(table.render())
        val github = GitHub.connectUsingPassword(username, password)

        println("search in github api")
        val repos = github
                .searchRepositories()
                .q(query)
                .language(language)
                .sort(GHRepositorySearchBuilder.Sort.STARS)
                .order(GHDirection.DESC)
                .list()
                .withPageSize(pages)

        println("${repos.totalCount} entries found")
        println("start cloning repos with $threads Threads")

        val threadPool = Executors.newFixedThreadPool(threads)
        repos.forEach { repo ->
            threadPool.execute {
                cloneRepository("${this.repos}/${repo.fullName}.git", repo.htmlUrl.toString())
                println(repo.htmlUrl.toString())
            }
        }
        threadPool.shutdown()
        while (!threadPool.isTerminated) { }
        println("repos cloned")
    }
}

fun cloneRepository(location : String, gitUrl : String) {
    val locationFile = File(location)
    if(!locationFile.exists()) {
        val git = Git.cloneRepository()
                .setURI(gitUrl)
                .setDirectory(File(location))
                .setBare(true)
                .call()
        println("$gitUrl cloned to $location")
    } else {
        println("$gitUrl already exists: skip")
    }
}