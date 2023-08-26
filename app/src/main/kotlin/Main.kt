import javafx.application.Application
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.io.File


val rootDirectory = "${System.getProperty("user.dir")}\\test\\"
class Main : Application() {

    // so I don't have to pass these around into event handlers and such
    private val root = BorderPane()
    private var currentDirectory = rootDirectory
    private var stageReference = Stage();

    private fun goIntoDirectory() : Unit {
        // in case of selection in an empty directory
        if (isLeftPaneEmpty(root.left as ListView<String>)) {
            return
        }
        val item = getLeftPaneItem(root.left as ListView<String>)
        if (isDirectorySelected(root.left as ListView<String>, currentDirectory)) {
            currentDirectory += item
            root.left = createLeftPane(currentDirectory, files.getDirectoryContents(currentDirectory), root, processLeftPaneKeyEvent(), processMouseClick())
        }
    }

    private fun goIntoParentDirectory() : Unit {
        // don't do anything if we're in the home directory, remove this if we want to go deeper
        if (currentDirectory == rootDirectory) {
            return
        }
        val parent = File(currentDirectory).parent
        if (parent != null) {
            // to catch edge case of root directory, should never be encountered in testing
            currentDirectory = parent + if (files.isSystemRoot(parent)) "" else "\\"
            root.left = createLeftPane(currentDirectory, files.getDirectoryContents(currentDirectory), root, processLeftPaneKeyEvent(), processMouseClick())
        }
    }

    // this might be changed if we should go into directories above home
    private fun goIntoHomeDirectory() : Unit {
        currentDirectory = rootDirectory
        root.left = createLeftPane(rootDirectory, files.getDirectoryContents(rootDirectory), root, processLeftPaneKeyEvent(), processMouseClick())
    }

     private fun processLeftPaneKeyEvent() : EventHandler<KeyEvent> {
        return EventHandler {
            //println(it.code)
            when (it.code) {
                KeyCode.ENTER -> goIntoDirectory()
                KeyCode.SPACE -> goIntoParentDirectory()
                else -> {} // do nothing
            }
            // it.consume()
        }
    }

    private fun processMouseClick() : EventHandler<MouseEvent> {

        return EventHandler{ input : MouseEvent? ->
            with (input as MouseEvent) {
                if (input.button.equals(MouseButton.PRIMARY) && input.clickCount == 2) {
                    goIntoDirectory()
                }
            }
        }
    }

    private val deleteAction = EventHandler<ActionEvent> {
        if (!isLeftPaneEmpty(root.left as ListView<String>)) {
            val item = getLeftPaneItem(root.left as ListView<String>)
            val success = files.deleteFile(item, currentDirectory)
            // update view
            val name : String? = if(success) null else item
            root.left = createLeftPane(currentDirectory, files.getDirectoryContents(currentDirectory), root, processLeftPaneKeyEvent(), processMouseClick(), name)
        }
    }

    private val renameAction = EventHandler<ActionEvent> {
        if (!isLeftPaneEmpty(root.left as ListView<String>)) {
            var name = getLeftPaneItem(root.left as ListView<String>)
            val success = files.renameFile(name, currentDirectory)
            name = success ?: name
            root.left = createLeftPane(currentDirectory, files.getDirectoryContents(currentDirectory), root, processLeftPaneKeyEvent(), processMouseClick(), name)
        }
    }

    private val moveAction = EventHandler<ActionEvent> {
        if (!isLeftPaneEmpty(root.left as ListView<String>)) {
            val item = getLeftPaneItem(root.left as ListView<String>)
            val newLocation = files.moveFile(item, currentDirectory, stageReference)
            // println(newLocation)
            if (newLocation != null) {
                // to catch edge case of root directory, should never be encountered in testing
                currentDirectory = newLocation + if (files.isSystemRoot(newLocation)) "" else "\\"
                root.left = createLeftPane(currentDirectory, files.getDirectoryContents(currentDirectory), root, processLeftPaneKeyEvent(), processMouseClick(), item)
            }
        }
    }

    init {
        // create top panel
        val topPane = VBox().apply {
            prefHeight = 30.0
            background = Background(BackgroundFill(Color.valueOf("#00ffff"), null, null))
            setOnMouseClicked { println("top pane clicked") }

            children.addAll(
                    MenuBar().apply {
                        menus.add(Menu("File").apply {
                            items.add(MenuItem("Home").apply {
                                onAction = EventHandler { goIntoHomeDirectory() }
                            })
                            items.add(MenuItem("Next").apply {
                                onAction = EventHandler { goIntoDirectory() }
                            })
                            items.add(MenuItem("Previous").apply {
                                onAction = EventHandler { goIntoParentDirectory() }
                            })
                            items.add(MenuItem("Exit").apply{
                                onAction = EventHandler { Platform.exit() }
                            })
                        })
                        menus.add(Menu("Actions").apply {
                            items.add(MenuItem("Rename").apply{
                                onAction = renameAction
                            })
                            items.add(MenuItem("Move").apply {
                                onAction = moveAction
                            })
                            items.add(MenuItem("Delete").apply{
                                onAction = deleteAction})
                            isFocusTraversable = false
                        })
                        menus.add(Menu("Options").apply {
                            items.add(MenuItem("About").apply {
                                onAction = EventHandler { Alert(Alert.AlertType.CONFIRMATION).apply {
                                    with (dialogPane.scene.window as Stage) {
                                        icons.add(Image(resources.appIcon))
                                    }
                                    title = "File Explorer - About"
                                    headerText = null
                                    dialogPane.content = VBox().apply {
                                        children.add(Label("Victor Astinov"))
                                        children.add(Label("Created May 29, 2023"))
                                        children.add(Label("Designed for CS349"))
                                    }
                                    dialogPane.graphic = null
                                    dialogPane.prefHeight = 100.0
                                    dialogPane.prefWidth = 200.0
                                }.showAndWait() } })
                            isFocusTraversable = false
                        })
                        isFocusTraversable = false
                    },
                    ToolBar().apply {
                        items.add(Button("Home").apply {
                            isFocusTraversable = false
                            onAction = EventHandler{ goIntoHomeDirectory() }
                        })
                        items.add(Button("Next").apply {
                            isFocusTraversable = false
                            onAction = EventHandler{ goIntoDirectory() }
                            // val left = root.left as ListView<String>
                        })
                        items.add(Button("Previous").apply {
                            isFocusTraversable = false
                            onAction = EventHandler{ goIntoParentDirectory() }
                        })
                        items.add(Button("Delete").apply {
                            isFocusTraversable = false
                            onAction = deleteAction
                        })
                        items.add(Button("Rename").apply {
                            isFocusTraversable = false
                            onAction = renameAction
                        })
                        items.add(Button("Move").apply {
                            isFocusTraversable = false
                            onAction = moveAction
                        })

                    })
            isFocusTraversable = false
        }
        root.top = topPane
        root.left = createLeftPane(rootDirectory, files.getDirectoryContents(rootDirectory), root, processLeftPaneKeyEvent(), processMouseClick())
    }

    override fun start(primaryStage: Stage?) {

        // create the scene and show the stage
        with (primaryStage!!) {
            scene = Scene(root, 600.0, 400.0)
            primaryStage.isResizable = false
            title = "File Explorer"
            icons.add(Image(resources.appIcon))
            stageReference = primaryStage
            show()
        }
    }

}