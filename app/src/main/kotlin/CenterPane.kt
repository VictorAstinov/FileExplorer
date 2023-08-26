import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextArea
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox

fun createCenterPane(fileName : String) : Pane {
    var centrePane = Pane().apply {
        prefWidth = 100.0
        background = Background(BackgroundFill(Color.valueOf("#ffff00"), null, null))
        isFocusTraversable = false
    }
    val extension = fileName.substringAfterLast('.', "")

    if (files.isFileReadable(fileName)) {
        if (extension == "png" || extension == "jpg" || extension == "bmp") {
            centrePane.children.add(ImageView(fileName).apply {
                isCache = true
                // isPreserveRatio = true
                fitWidthProperty().bind(centrePane.widthProperty())
                fitHeightProperty().bind(centrePane.heightProperty())
                isFocusTraversable = false
            })
        }
        else if (extension == "txt" || extension == "md") {
            val text = files.getTextFileContents(fileName)
            val content = TextArea(text).apply {
                isWrapText = true
                isEditable = false
                prefWidthProperty().bind(centrePane.widthProperty())
                prefHeightProperty().bind(centrePane.heightProperty())
                isFocusTraversable = false
            }
            val pane = ScrollPane(content).apply {
                vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
                isFocusTraversable = false
            }
            centrePane.children.add(pane)
        }
        else if (files.isDirectory(fileName)) {
            centrePane.children.add(ImageView(resources.folderIcon).apply {
                isCache = true
                fitWidthProperty().bind(centrePane.widthProperty())
                fitHeightProperty().bind(centrePane.heightProperty())
                isFocusTraversable = false
            })
        }
        // any unsupported file
        else {
            centrePane.children.add(ImageView(resources.unsupportedFile).apply {
                isCache = true
                fitWidthProperty().bind(centrePane.widthProperty())
                fitHeightProperty().bind(centrePane.heightProperty())
                isFocusTraversable = false
            })
        }
    }
    else {
        // unreadable file or file does not exist for some reason
        println("File error: File is not readable or does not exist")
        centrePane = StackPane().apply {
            prefWidth = 100.0
            background = Background(BackgroundFill(Color.ANTIQUEWHITE, null, null))
            children.add(VBox().apply {
                children.add(Label("File Cannot be read"))
                alignment = Pos.CENTER
            })
            alignment = Pos.CENTER
        }
    }

    return centrePane
}