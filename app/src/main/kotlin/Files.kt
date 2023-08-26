package files

import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.io.path.isDirectory
import kotlin.io.path.isReadable


// goal is to get current directory contents, and return them as a list
// note the list is sorted ALPHABETICALLY, not LEXICOGRAPHICALLY
fun getDirectoryContents(currentDirectory : String) : List<String> {
    val dir = Paths.get(currentDirectory)
    // println(currentDirectory)
    // use max_depth = 1 to show the contents to the directory, 0 for just the file itself
    // remove max_depth to go over entire file tree rooted at currentDirectory
    // drop the first element as it is the current directory
    // map to remove the path until the current directory
    val files : List<String> = Files.walk(dir, 1).map{if (it.isDirectory()) it.toString() + "\\" else it.toString()}.toList().drop(1).map{it.substring(currentDirectory.length)}.map{it}
    //val temp = files.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER, {it}))
    // temp.forEach{ println(it) }
    return files.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER, {it}))
}

fun isDirectory(file : String) : Boolean {
    return Paths.get(file).isDirectory()
}

fun isFileReadable(file : String) : Boolean {
    return Paths.get(file).isReadable()
}

fun getTextFileContents(file : String) : String {
    val bufferedReader = File(file).bufferedReader()
    return bufferedReader.use{it.readText()}
}

fun displayFileErrorMessage(operation : String, file : String, error : Exception = Exception("Error")) : Unit {
    println(error.message)
    Alert(Alert.AlertType.ERROR).apply {
        with (dialogPane.scene.window as Stage) {
            icons.add(Image(resources.appIcon))
        }
        isResizable = false
        title = "File Explorer - Error"
        headerText = "Error: Unable to $operation $file"
        // contentText = error.message
        dialogPane.content = TextArea(error.message).apply {
            isWrapText = true
            isEditable = false
        }
        dialogPane.prefWidth = 430.0
        dialogPane.prefHeight = 180.0
        showAndWait()
    }
}

fun deleteFile(f : String, currentDirectory : String) : Boolean {

    val path = Paths.get(currentDirectory + f)
    val isDir = isDirectory(currentDirectory + f)
    // println(currentDirectory + f)

    val alert = Alert(Alert.AlertType.CONFIRMATION).apply {
        isResizable = false
        title = "File Explorer - Delete ${if (isDir) "Folder" else "File"}"
        headerText = "Are you sure you want to delete this ${if (isDir) "folder" else "file"}?"
        graphic = ImageView(resources.recycleIcon).apply {
            isCache = true
        }
        with (dialogPane.scene.window as Stage) {
            icons.add(Image(resources.appIcon))
        }
        dialogPane.content = VBox().apply {
            val size = Files.size(path)
            val time : LocalDateTime = Files.getLastModifiedTime(path).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
            children.add(Label(f))
            if (!isDir) {
                children.add(Label("Size: ${if (size < 1000) "$size B" else (size / 1000).toString() + " KB"}"))
            }
            children.add(Label("Date Modified: ${time.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))}"))
        }

        showAndWait()
    }
    // displayFileErrorMessage(SecurityException("Error"), f) for testing
    if (alert.result == ButtonType.OK) {
        try {
            Files.delete(path)
            return true
        }
        catch (e : SecurityException) {
            println("Security Error")
            displayFileErrorMessage("delete", f, e)
        }
        catch (e : IOException) {
            println("IO Error")
            displayFileErrorMessage("delete", f, e)
        }
    }
    return false
}

fun isValidFileName(name : String) : Boolean {
    val illegalChars = listOf('/', '\n', '\r', '\t', '\u0000', '`', '?', '*', '\\', '<', '>', '|', '\"', ':' )

    // check if file is named . or .. -> illegal, although this will probably cause an IO exception anyway
    // string cant be empty
    if (name == "." || name == ".." || name == "") {
        return false
    }

    for (c in name) {
        if (c in illegalChars) {
            println("Illegal character in file name")
            return false
        }
    }

    return true
}


// renames, but still in directory
fun renameFile(file : String, currentDirectory: String) : String? {
    val path = Paths.get(currentDirectory + file)
    val alert = TextInputDialog().apply {
        with (dialogPane.scene.window as Stage) {
            icons.add(Image(resources.appIcon))
        }

        title = "File Explorer - Rename"
        headerText = "Rename ${file}?"
        contentText = "New name:"
        isResizable = false

        dialogPane.prefWidth = 300.0
        dialogPane.prefHeight = 150.0

        showAndWait()
    }

    // println(alert.result)
    if (alert.result != null) {
        if (isValidFileName(alert.result)) {
            try {
                Files.move(path, path.resolveSibling(alert.result))
                return alert.result
            }
            catch (e : SecurityException) {
                println("Security Error")
                displayFileErrorMessage("rename", file, e)
            }
            catch (e : IOException) {
                println("IO Error")
                displayFileErrorMessage("rename", file, e)
            }
        }
        else {
            displayFileErrorMessage("rename", file)
        }
    }
    return null
}

fun moveFile(file : String, currentDirectory: String, stage: Stage, defaultDir : String = "${System.getProperty("user.dir")}\\test\\") : String? {
    val path = Paths.get(currentDirectory + file)
    val alert = DirectoryChooser().apply {
        title = "File Explorer - Move"
        initialDirectory = File(defaultDir)

    }
    val dir : File = alert.showDialog(stage) ?: return null
    // println(dir.absolutePath)
    /*
    if (!isValidDestination(dir.absolutePath)) {
        displayFileErrorMessage("move", file)
        return null
    } */
    try {
        Files.move(path, Paths.get(dir.absolutePath + "\\" + file))
        return dir.absolutePath
    }
    // these should catch any illegal moves
    catch (e : FileAlreadyExistsException) {
        println("Existing File Error")
        displayFileErrorMessage("move", file, e)
    }
    catch(e : SecurityException) {
        println("Security Error")
        displayFileErrorMessage("move", file, e)
    }
    catch (e : IOException) {
        println("IO Error")
        displayFileErrorMessage("move", file, e)
    }
    return null
}

fun isSystemRoot(file : String) : Boolean {
    return File(file).parent == null
}


