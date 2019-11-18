package de.ddkfm.githubcloner

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class Args(parser: ArgParser) {
    val username by parser.storing(
            "-u", "--username",
            help = "github username")

    val password by parser.storing(
            "-p", "--password",
            help = "github password")

    val repos by parser.storing(
            "-r", "--repos",
            help = "location for all repos")
            .default("./repos")

    val query by parser.storing(
            "-q", "--query",
            help = "query for the github search api")
            .default("created:>2019-01-01")//default: all projects created after 01-01-2019
    val pages by parser.storing(
            "-P", "--pages",
            help = "size of paging") { toInt() }
            .default(1000)
    val language by parser.storing(
            "-L", "--language",
            help = "programming language")
            .default("")
    val threads by parser.storing(
            "-t", "--threads",
            help = "amount of threads used for cloning the repos") { toInt() }
            .default(20)
}