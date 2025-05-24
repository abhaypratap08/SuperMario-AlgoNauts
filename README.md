# Java SuperMario ⭐

<div>

![GitHub last commit](https://img.shields.io/github/last-commit/74C17N3P7UN3/SuperMario?style=for-the-badge)
![GitHub release](https://img.shields.io/github/v/release/74C17N3P7UN3/SuperMario?include_prereleases&style=for-the-badge&color=blue)
![GitHub downloads (all assets, all releases)](https://img.shields.io/github/downloads/74C17N3P7UN3/SuperMario/total?style=for-the-badge&color=blue)

![GitHub license](https://img.shields.io/badge/license-mit-blue?style=for-the-badge)
![GitHub repo size](https://img.shields.io/github/repo-size/74C17N3P7UN3/SuperMario?style=for-the-badge)
![GitHub pages](https://img.shields.io/github/deployments/74C17N3P7UN3/SuperMario/github-pages?style=for-the-badge&label=javadoc)

</div>

## How to run the game

### 🕹️ Java application
1. Download the latest `.jar executable` from here: [/releases](https://github.com/74C17N3P7UN3/SuperMario/releases/latest)
2. Download `OpenJDK 21` from here: [https://jdk.java.net/21](https://jdk.java.net/21)
3. Extract the JDK and go into the `openjdk-21.0.2\bin` folder
4. Open the command prompt and insert the command below

```
java.exe -jar <path_to_jar> [external_server_url]
```

### 💽 PHP/SQL server
If you want to be able to save your scores, you will need to host a web server.\
The easiest way, is to setup [XAMPP](https://www.apachefriends.org/download.html) for hosting the PHP server and the SQL database.\
Make sure to download the *web folder* from the [source code](https://github.com/74C17N3P7UN3/SuperMario/archive/refs/heads/main.zip) too before moving on.

* Once installed, open the program and start the `Apache` and `MySQL` modules
* Then click on `📂 explorer` in the right panel to open XAMPP's root folder
* Go into the `htdocs` folder and delete its contents before pasting the files contained in the *web folder*
* Back to the program, click on `📟 shell` in the right panel and type `mysql -u root`
* Finally, paste the contents of the [`commands.sql`](https://github.com/74C17N3P7UN3/SuperMario/blob/main/web/commands.sql) file in the terminal and close it

## ✍️ Authors

* [@74C17N3P7UN3](https://github.com/74C17N3P7UN3) - Leonardo
* [@TheInfernalNick](https://github.com/TheInfernalNick) - Nicolò
* [@TOSAT0](https://github.com/TOSAT0) - Michele
